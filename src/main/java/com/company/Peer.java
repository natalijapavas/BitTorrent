package com.company;

import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Arrays;

public class Peer {
    //add the stirng "BitTorrent protocol"
    private static byte[] BTChars = { 'B', 'i', 't', 'T', 'o', 'r', 'r', 'e', 'n', 't', ' ',
            'p', 'r', 'o', 't', 'o', 'c', 'o', 'l' };
    private static final byte BTCharsLen=0x13;
    private static final int HANDSHAKE_SIZE=68;
    private int downloaded = 0;
    private boolean[] bitfield; //our real bitfield, but in messages we get byte[] bitfield
    private byte[] bfield; //ili byte[] peerID;
    private PeerInfo peerInfo;
    private Socket socket;


    public Peer(boolean[] bitfield, byte[] bfield, PeerInfo peerInfo){
        this.bitfield = bitfield;
        this.bfield = bfield;
        this.peerInfo = peerInfo;
    }

    public Peer(PeerInfo peerInfo)
    {
        this.peerInfo=peerInfo;
    }

    //Generate handhake
    public static byte[] handshake(Tracker tracker){
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

    public boolean[] getBitfield() {
        return bitfield;
    }

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

    public void setBitfield(boolean[] bitfield) {
        this.bitfield = bitfield;
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
            this.bitfield = boolBitfield;
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
        DataOutputStream dos = (DataOutputStream) this.getPeerInfo().getOut();
        Message.encode(m,dos);

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
