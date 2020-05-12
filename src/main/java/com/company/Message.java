package com.company;

import java.io.*;
import java.io.DataOutputStream;

public class Message {

    //ByteOutputStream out;
    private static final byte keepAliveID = -1;

    //chocke = peer will not send the file until peer unchokes
    private static final byte chokeID = 0;
    private static final byte unchokeID = 1;

    //interested = we would like to download from the peer
    private static final byte interestedID = 2;
    private static final byte uniterestedID = 3;
    private static final byte haveID = 4;
    private static final byte bitfieldID = 5;
    private static final byte requestID = 6;
    private static final byte pieceID = 7;
    private static final byte cancelID = 8;
    private static final byte portID = 9;

    private static final Message KEEP_ALIVE = new Message(0, (byte) 255);
    private static final Message CHOKE = new Message(1, chokeID);
    private static final Message UNCHOKE = new Message(1, unchokeID);
    private static final Message INTERESTED = new Message(1, interestedID);
    private static final Message UNINTERESTED = new Message(1, uniterestedID);

    //We have 4 different types of payload:
    //Interested,not interested,choked and unchoked don't have a payload
    //Have has a piece index which is a 4-byte payload -- smallPayload
    //Bitfield also has a variable-length payload - smallPayloadBitfield
    //Request has a payload that contains:
        //- index - 4-bytes
        //- begin - 4-byte offset (begining)
        //-length - 4-byte block length         -- bigPayloadRequest
    //Piece has a payload that contains:
        //-index - 4-bytes
        //-begin - 4-byte offset (begining)
        //-block - variable length              -- bigPayloadPiece



    protected final byte id;
    protected final int length;
    public Message(final int length, final byte id){


        this.id = id;
        this.length = length;

        //initializing our payloads
    }



    //encode payload:
    public void encodePayload(DataOutputStream dos) throws IOException{
        return;
    }

    //have: <len=0005><id=4><piece index>
    public static final class Have extends Message {
        public final int i; //index

        //have has a piece index
        public Have(final int index){
            super(5, haveID);
            this.i = index;
        }

        public int getPieceIndex(){
            return this.i;
        }

        public void encodePayload(DataOutputStream output) throws IOException {
            output.write(this.i);
        }
    }

    //bitfield: <len=0001+X><id=5><bitfield>
    public static final class Bitfield extends Message {
        private final byte[] bitfield;

        public Bitfield(final byte[] bitfield){
            super(bitfield.length +1, bitfieldID);
            this.bitfield = bitfield;
        }

        public byte[] getBitfield(){
            return this.bitfield;
        }

        public void encodePayload(DataOutputStream output) throws IOException {
            output.write(this.bitfield);

        }
    }

    //piece: <len=0009+X><id=7><index><begin><block>
    public static final class Piece extends Message {
        public final int ind;
        public final int start;
        private final byte[] block;

        //piece has a bigpayload
        public Piece(final int ind, final int start, final byte[] block){
            super(block.length + 9, pieceID);
            this.ind = ind;
            this.start = start;
            this.block = block;

        }
        public void encodePayload(DataOutputStream output) throws IOException {
            output.writeInt(this.ind);
            output.writeInt(this.start);
            output.write(this.block);
        }
    }

    //request: <len=0013><id=6><index><begin><length>
    public static final class Request extends Message {
        public final int ind;
        public final int start;
        public final int len ;

        public Request(final int ind,final int start,final int length){
            super(13,requestID);
            this.ind = ind;
            this.start = start;
            this.len = length;
        }

        public void encodePayload(DataOutputStream output) throws IOException {
            output.writeInt(this.ind);
            output.writeInt(this.start);
            output.writeInt(this.len);

        }
    }

    //cancel: <len=0013><id=8><index><begin><length>
    public static final class Cancel extends Message{
        private final int ind;
        private final int start;
        private final int clength;

        public Cancel(final int ind, final int start, final int length){
            super(13, cancelID);
            this.ind = ind;
            this.start = start;
            this.clength = length;

        }

        public void encodePayload(DataOutputStream output) throws IOException {
            output.writeInt(this.ind);
            output.writeInt(this.start);
            output.writeInt(this.clength);
        }

    }

    //port: <len=0003><id=9><listen-port>
    public static final class Port extends Message{
        private final int port;

        public Port(final int port){
            super(3,portID);
            this.port = port;
        }

        public void encodePayload(DataOutputStream output) throws IOException {
            output.writeInt(this.port);
        }
    }


    public static Message decode(DataInputStream dataIn) throws IOException {
        int length = dataIn.readInt();

        if(length == 0){
            System.out.println("Keep Alive");
            return KEEP_ALIVE;
        }

        byte id = dataIn.readByte();
        switch (id){
            case(chokeID):
                return CHOKE;

            case(unchokeID):
                System.out.println("Unchoke!");
                return UNCHOKE;

            case(interestedID):
                return INTERESTED;

            case(uniterestedID):
                return UNINTERESTED;

            case(haveID):
                int i = dataIn.readInt();
                return new Have(i);

            case(bitfieldID):
                byte[] bitfield = new byte[length - 1];
                dataIn.readFully(bitfield);
                return new Bitfield(bitfield);

            case(pieceID):
                int ind = dataIn.readInt();
                int start = dataIn.readInt();
                byte[] block  = new byte[length - 9];
                dataIn.readFully(block);
                return new Piece(ind,start,block);

            case(requestID):
                int index = dataIn.readInt();
                int start1 = dataIn.readInt();
                int len = dataIn.readInt();
                return new Request(index,start1,len);

        }
        return null;
    }

    public static void encode(final Message message, final OutputStream output) throws IOException{
        DataOutputStream dos = null;
        if(message != null){
            {
                dos = new DataOutputStream(output);
                dos.writeInt(message.length);
                if(message.length > 0){
                    dos.write(message.id);
                    message.encodePayload(dos);
                }
                dos.flush();
            }
        }
    }

    public Message parse(Message message, Peer peer) throws IOException{
        byte id;

        int numOfPieces;
        int pieceLength = peer.peerInfo.getTracker().getMetaInfoFile().getPieceLength();
        int fileLength = peer.peerInfo.getTracker().getMetaInfoFile().getFileLength();
        if(fileLength%pieceLength != 0)
            numOfPieces = fileLength / pieceLength + 1;
        else
            numOfPieces = fileLength / pieceLength;

        //keep-alive is a message with zero bytes - length prefix = 0
        if (message.length == 0)
            id = keepAliveID;
        else id = message.id;

        switch (id) {
            case (keepAliveID):
                return new Message(0, keepAliveID);

            case (chokeID):
                peer.peerInfo.setPeerChocking(true);
                return null;

            case (unchokeID):
                peer.peerInfo.setPeerChocking(false);
                return null;

            case (interestedID):
                peer.peerInfo.setPeerInterested(true);
                return new Message(1, unchokeID);

            case (uniterestedID):
                peer.peerInfo.setPeerInterested(false);
                return null;

            case (haveID):
                boolean haspiece[] = peer.peerInfo.getHasPiece();
                //potencijalna greska :D
                Have have = (Have) message;
                haspiece[have.getPieceIndex()] = true;
                peer.peerInfo.setHasPiece(haspiece);
                return null;

            case (bitfieldID):
                Bitfield bitfield = (Bitfield) message;
                peer.peerInfo.setPeerId(bitfield.getBitfield());

                boolean[] haspiece1 = new boolean[numOfPieces];
                byte b = 0;
                peer.peerInfo.setHasPiece(haspiece1);
                for (int i = 0; i < numOfPieces; i++){
                    int j = i; //beacuse i must stay unchanged
                    int k = 0; //place of a curr piece
                    //we want to find a place of each peace
                    //8 bits -> 1 byte
                    k = (int) Math.ceil(j/8); //check math?
                    j = j % 8;
                    b = (byte) (peer.peerInfo.getPeerId()[k] << j); //left shift


                    if (b < 0){
                        haspiece1[i] = true;
                        peer.peerInfo.setHasPiece(haspiece1);
                    }
                    else {
                        haspiece1[i] = false;
                        peer.peerInfo.setHasPiece(haspiece1);
                    }
                }
                if(peer.checkBitfield(peer.bitfield))
                    return new Message(1,interestedID);
                else
                    return new Message(1, uniterestedID);

            case (pieceID):
                Piece piece = (Piece) message;
                if(piece.start > 0) //this means that peer has a piece
                    return new Have(piece.ind);
                else
                    return null;
            case (requestID):
                Request req = (Request) message;
                //we are trying to find a piece of index req.ind*pieceLength + req.start
                peer.peerInfo.getThefile().seek(pieceLength*req.ind + req.start);

                byte[] ourPiece = new byte[req.length];
                //we are reading that piece
                peer.peerInfo.getThefile().readFully(ourPiece);

                //ADD HERE UPLOADING AND DOWNLOADING REQUESTED PIECES


                return new Piece(req.ind,req.start,ourPiece);

            case (cancelID):
                return null;

            default:
                throw new IllegalStateException("Unexpected value: " + id);
        }
        //return null;

    }



}
