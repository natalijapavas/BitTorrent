package com.company;
import java.io.*;
import java.io.DataOutputStream;

public class Message {

    //ByteOutputStream out;
    public static final byte keepAliveID = -1;

    //choke = peer will not send the file until peer unchokes
    public static final byte chokeID = 0;
    public static final byte unchokeID = 1;

    //interested = we would like to download from the peer
    public static final byte interestedID = 2;
    public static final byte uniterestedID = 3;
    public static final byte haveID = 4;
    public static final byte bitfieldID = 5;
    public static final byte requestID = 6;
    public static final byte pieceID = 7;
    public static final byte cancelID = 8;
    public static final byte portID = 9;

    public static final Message KEEP_ALIVE = new Message(0, (byte) 255);
    public static final Message CHOKE = new Message(1, chokeID);
    public static final Message UNCHOKE = new Message(1, unchokeID);
    public static final Message INTERESTED = new Message(1, interestedID);
    public static final Message UNINTERESTED = new Message(1, uniterestedID);

    /** We have 4 different types of payload:
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
     */


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
            output.writeInt(this.i);
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

        public byte[] getBlock() {
            return block;
        }

        public int getStart() {
            return start;
        }

        public int getInd() {
            return ind;
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
        private final int ind; //piece index
        private final int start; //byteOffset within a piece
        private final int clength; //cancel length, due to conflict in names

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

    public int getId(){
        return this.id;
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
                dataIn.readFully(bitfield); //not sure if read fully will pass
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

    public static void encode(final Message message, final DataOutputStream output) throws IOException{
        if(message != null){
            {
                output.writeInt(message.length);
                if(message.length > 0){
                    output.write(message.id);
                    message.encodePayload(output);
                }
                output.flush();
            }
        }
    }




}
