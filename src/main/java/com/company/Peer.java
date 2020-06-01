package com.company;
import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Arrays;

public class Peer implements Runnable{
    //add the stirng "BitTorrent protocol"
    private static byte[] BTChars = { 'B', 'i', 't', 'T', 'o', 'r', 'r', 'e', 'n', 't', ' ',
            'p', 'r', 'o', 't', 'o', 'c', 'o', 'l' };
    private static final byte BTCharsLen=0x13;
    private static final int HANDSHAKE_SIZE=68;
    private int downloaded = 0;
    private byte[] bfield; //ili byte[] peerID;
    private PeerInfo peerInfo;
    private ManagerInfo managerInfo;
    private Socket socket;


    public Peer(PeerInfo peerInfo)
    {
        this.peerInfo=peerInfo;
    }

    public Peer(PeerInfo peerInfo, ManagerInfo managerInfo) {
        this.peerInfo=peerInfo;
        this.managerInfo=managerInfo;
    }


    public void run() {
        try {
            if(!createSocket()) {
                System.out.println("Error in handshake sequence, terminating thread");
                return;
            }
            while(!peerInfo.isBadPeer()) {
                if(!peerInfo.isPeerChocking()) {
                    //we are assuming that we won't find any piece that we already don't have
                    //int missingPieceIndex=-1;
                    Message peerResponse = receiveMessage();
                    switch (peerResponse.id){
                        case Message.keepAliveID:
                            sendMessage(Message.KEEP_ALIVE);
                            break;
                        case Message.chokeID:
                            getPeerInfo().setPeerChocking(true);
                            break;
                        case Message.unchokeID:
                            getPeerInfo().setPeerChocking(false);
                            break;
                        case Message.interestedID:
                            getPeerInfo().setPeerInterested(true);
                            sendMessage(Message.UNCHOKE);
                            break;
                        case Message.uniterestedID:
                            getPeerInfo().setPeerInterested(false);
                            break;
                        case Message.haveID:
                            Message.Have have = (Message.Have) peerResponse;
                            int pieceIndex=have.getPieceIndex();
                            peerInfo.setNewPiece(pieceIndex);
                            if(peerInfo.isInterested()) { //if we are interested in peer
                                synchronized (managerInfo) {
                                    if(!managerInfo.clientBitfield[pieceIndex]) //i ne skida se
                                        sendMessage(Message.INTERESTED);
                                }
                            }
                            break;
                        case Message.bitfieldID:
                            System.out.println("Bitfield message");
                            Message.Bitfield BitMSG = (Message.Bitfield) peerResponse;
                            byte[] bitfieldPayload = BitMSG.getBitfield();
                            boolean[] bitfield =bitfieldToBool(bitfieldPayload,this.peerInfo.getNumberOfPieces());

                            synchronized (managerInfo) {
                                for (int i = 0; i < bitfield.length; i++) {
                                    //we want a piece if we have not requested it, someone has it, and we dont have it
                                    //in this case we send a message INTERESTED
                                    if (bitfield[i] == true && managerInfo.clientBitfield[i] == false) {
                                        sendMessage(Message.INTERESTED); //mozda ovde da saljemo request
                                        this.peerInfo.setInterested(true);
                                        break;
                                    }
                                }
                            }
                            sendMessage(Message.UNINTERESTED); //if we have everything this peer has, we can send uninterested
                            break;
                        case Message.pieceID:
                            Message.Piece pieceMsg = (Message.Piece) peerResponse;//if a peer sends you a Piece message, he is sending you a block inside of a piece, so you still don't know if you have whole piece or now
                            byte[] dataBytes=pieceMsg.getBlock();
                            DataBlock dataBlock=new DataBlock(pieceMsg.ind,pieceMsg.start,dataBytes,dataBytes.length);
                            synchronized (managerInfo) {
                                managerInfo.addBlock(dataBlock);
                                //mora da se proveri da li je skinut ceo piece, i tek ako jeste da se posalje have
                            }
                            break;
                        case Message.requestID:
                            break;
                            //NIJE TESTIRANO
                    /*
                    Message.Request req = (Message.Request) peerMessage.getMessage();
                    try {

                        //we are trying to find a piece of index req.ind*pieceLength + req.start
                        peerMessage.getPeer().getPeerInfo().getOutputFile().seek(pieceLength * req.ind + req.start);

                        byte[] ourPiece = new byte[req.length];
                        //we are reading that piece
                        peerMessage.getPeer().getPeerInfo().getOutputFile().readFully(ourPiece);

                        //ADD HERE UPLOADING AND DOWNLOADING REQUESTED PIECES

                        peerMessage.getPeer().sendMessage(new Message.Piece(req.ind,req.start,ourPiece));
                        break;
                    } catch(IOException e){
                        e.printStackTrace();
                        break;
                    }

                     */

                        case Message.cancelID:
                            this.peerInfo.getOut().close();
                            this.peerInfo.getIn().close();
                            socket.close();
                            this.peerInfo.setBadPeer();
                            synchronized (managerInfo.currentPeerList){
                                managerInfo.currentPeerList.remove(this);
                            }
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + peerResponse.id);
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestPiece() throws IOException {
        synchronized (managerInfo) {
            int pieceToRequest = managerInfo.getRarest(this.peerInfo); //getRarest mora da proverava da li smo requestovali piece
            int missingBlockIndex=managerInfo.getMissingBlockIndex(pieceToRequest);
            //prilikom inicijalizacije piece niza, mogu da podesim start u blocku
            int blockSize=managerInfo.getBlockSizeToRequest(pieceToRequest,missingBlockIndex);
            int blockStartingPosition=managerInfo.getBlockStartingPosition(pieceToRequest);
            this.sendMessage(new Message.Request(pieceToRequest,blockStartingPosition,blockSize));
        }
    }



    //Generate handhake
    public byte[] handshake(Tracker tracker){
        byte[] handshake = new byte[HANDSHAKE_SIZE];
        int currPos = 0;
        handshake[currPos] =BTCharsLen;
        ++currPos;
        System.arraycopy(BTChars, 0, handshake, currPos, BTCharsLen);
        currPos += BTCharsLen;
        System.out.println("Upisao torrent karaktere");
        //8 empty slots
        for(int i = 0; i < 8; currPos++, i++){
            handshake[currPos] = 0;
        }
        //info hash
        byte[] infoHash=tracker.getInfoHashBytes();
        byte[] peerId=tracker.getPeerId(); //change 1



        //public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        System.arraycopy(infoHash, 0, handshake, currPos, infoHash.length);
        currPos += infoHash.length;
        System.arraycopy(peerId, 0, handshake, currPos,peerId.length);

        return handshake;
    }

 /*   public int getRarest(){ //prebaciti u manager info
        managerInfo.pieceRepeating();
        int min = Integer.MAX_VALUE; //Javas integer infinity
        int minI = managerInfo.pieceRepeating.length;
        for(int i = 0; i < managerInfo.pieceRepeating.length;i++){
            //checking if we already have this piece
            if(managerInfo.clientBitfield[i] == false && peerInfo.getBitfield()[i] == true){
                //if we don't have the piece, we check how common it is
                //we want to find the rarest
                if(min > managerInfo.pieceRepeating[i]){
                    min = managerInfo.pieceRepeating[i];
                    minI = i;
                }
            }
        }
        if(minI==managerInfo.pieceRepeating.length)
            return -1;
        return managerInfo.pieceRepeating[minI];
    } */


    public int getDownloaded() {
        return downloaded;
    }


    public byte[] getBfield() {
        return bfield;
    }

    public PeerInfo getPeerInfo() {
        return peerInfo;
    }

    public void setBfield(byte[] bfield) {
        this.bfield = bfield;
    }


    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
    }

    public void setPeerInfo(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
    }


    public boolean checkBitfield(byte[] otherBitfield)
    {
        return Arrays.equals(this.bfield,otherBitfield);
    }

    //converting from byte[] bitfield to boolean[] bitfield and updating our bitfield
    public boolean[] bitfieldToBool(byte[] bitfield,int numPieces){
        if(bitfield == null)
            return null;
        else{
            this.bfield = bitfield;
            boolean[] boolBitfield = new boolean[numPieces];
            for(int i = 0; i < numPieces; i++){
                int byteI = i/8;
                int bitI = i%8;

                    //this will be true only if bitfield[byteI] is 1
                if(((bitfield[byteI] << bitI) & 0x80) == 0x80)
                    boolBitfield[i] = true;
                else
                    boolBitfield[i] = false;
            }
            this.peerInfo.setBitfield( boolBitfield);
            return boolBitfield;

        }

    }

    public boolean checkHandhshake(byte[] info, byte[] response) throws IOException {
        byte[] peerHash = new byte[20];
        System.arraycopy(response, 28, peerHash, 0, 20);
        return Arrays.equals(peerHash, info);
    }

    //connect to a peer
    public  boolean createSocket() throws IOException{
        try{
            this.socket = new Socket(this.peerInfo.getIp(),this.peerInfo.getPort());
            byte[] handshake = this.handshake(this.peerInfo.getTracker());
            System.out.println("Are we connected to Peer: "+ this.socket.isConnected());
            DataOutputStream out = new DataOutputStream((this.socket.getOutputStream()));
            out.write(handshake);
            System.out.println("Message written");
            DataInputStream in = new DataInputStream(this.socket.getInputStream());

            byte [] response = new byte[HANDSHAKE_SIZE];
            System.out.println("Preparing to read message");
            in.read(response);

            System.out.println("Peer message response: "+URLEncoder.encode(response.toString(),"UTF-8"));

            if(!checkHandhshake(this.peerInfo.getTracker().getInfoHashBytes(), response))
                return false;
            peerInfo.setHandshake(true);
            //out.flush();
            System.out.println("Socket created!");
            return true;
        } catch(IOException e){
            System.out.print("Couldn't complete handshake");
            e.printStackTrace();
            return false;
        }
    }

    //send message
    public synchronized void sendMessage(Message m) throws IOException{
        if(this.getPeerInfo().getOut() == null)
            throw new IOException();
        DataOutputStream dos = this.getPeerInfo().getOut();
        Message.encode(m,dos);

    }

    public synchronized Message receiveMessage() throws IOException{
        if(this.getPeerInfo().getIn() == null)
            throw new IOException();
        return Message.decode(this.getPeerInfo().in);
    }

    //choking
    public void choke(){
        try{
            this.sendMessage(Message.CHOKE);
        }catch (IOException e){
            e.printStackTrace();
        }
        this.getPeerInfo().setChockingPeer(true);
    }

    //unchoking
    public void unchoke(){
        try{
            this.sendMessage(Message.UNCHOKE);
        }catch(IOException e){
            e.printStackTrace();
        }
        this.getPeerInfo().setChockingPeer(false);
    }

    @Override
    public String toString() {
        return "Peer:name: "+this.peerInfo.getName()+", ip: "+this.peerInfo.getIp();
    }

}
