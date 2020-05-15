package com.company;


/**

This class will (at least for now) serve to hold a data block of given piece which was sent by a Peer
 (or which we will send to a Peer which has requested it). Several data blocks will constitute a single Piece as
 described in the original specification.
 Class fields:
 -block: byte[]
 -index: int //uniquely defines piece to which this block belongs to
 -start: int //uniquely defines byte offset within the piece of the block

 **/


public class DataBlock {
    private final int MAX_CAPACITY=1<<14; //2^14
    private byte[] block;
    private int index;
    private int start;
    private int size;
    private boolean isRequested;
    private boolean isDownloaded;


    public DataBlock(int index,int start,byte[] block,int size)
    {
        this.index=index;
        this.start=start;
        this.block=block;
        this.size=size;
        this.isRequested=false;
        this.isDownloaded=false;
    }

    public DataBlock(int index,int start,byte[] block,int size,boolean isRequested)
    {
        this.index=index;
        this.start=start;
        this.block=block;
        this.size=size;
        this.isRequested=isRequested;
    }


    public boolean isRequested() {
        return isRequested;
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

    public int getSize() {
        return size;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setRequested(boolean requested) {
        isRequested = requested;
    }

    public void setDownloaded(boolean isDownloaded) { this.isDownloaded=isDownloaded; }

}
