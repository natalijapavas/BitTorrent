package com.company;


/**
This class will (at least for now) serve to hold a data block of given piece which was sent by a Peer
 (or which we will send to a Peer which has requested it).
 Class fields:
 -block: byte[]
 -index: int //uniquely defines piece to which this block belongs to
 -start: int //uniquely defines byte offset within the piece of the block


 **/


public class DataBlock {
    private byte[] block;
    private int index;
    private int start;


    public DataBlock(int index,int start,byte[] block)
    {
        this.index=index;
        this.start=start;
        this.block=block;
    }

    public byte[] getBlock() {
        return block;
    }

    public int getIndex() {
        return index;
    }

    public int getStart() {
        return start;
    }
}
