package com.company;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Message {
    private static final byte keepAliveID = -1;
    public static final byte chokeID = 0;
    public static final byte unchokeID = 1;
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



    protected final byte id;
    protected final int length;
    public Message(final int length, final byte id){

        this.id = id;
        this.length = length;
    }
    public static final class Have extends Message {
        public final int i; //index

        public Have(final int index){
            super(5, haveID);
            this.i = index;
        }
    }
    public static final class Bitfield extends Message {
        public final byte[] bitfield;
        public Bitfield(final byte[] bitfield){
            super(bitfield.length +1, bitfieldID);
            this.bitfield = bitfield;
        }
    }

    public static final class Piece extends Message {
        public final int ind;
        public final int start;
        public final byte[] block;
        public Piece(final int ind, final int start, final byte[] block){
            super(block.length + 9, pieceID);
            this.ind = ind;
            this.start = start;
            this.block = block;
        }
    }

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
                System.out.println("Unchocke!");
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

}
