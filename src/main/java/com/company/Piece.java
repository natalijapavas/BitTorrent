package com.company;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;


public class Piece {
    private final int INITIAL_CAPACITY=8;
    private ArrayList<DataBlock> dataBlocks;
    private int currentSize; //we will use this as an offset showing us how much of the piece we have downloaded
    private int pieceSize;
    private int pieceIndex;
    boolean isWritten;
    private int numberOfBlocks; //number of blocks of size 2^14, and the last one, which might be smaller
    boolean isDownloaded;

    public Piece(int pieceSize,int pieceIndex)
    {
        this.numberOfBlocks=calculateNumberOfBlocks(pieceSize);
        this.dataBlocks=new ArrayList<>(this.numberOfBlocks);
        this.currentSize=0;
        this.pieceSize=pieceSize;
        this.pieceIndex=pieceIndex;
        this.isDownloaded=false;
        this.isWritten=false;
        initializeDataBlocks();
    }

    public Piece(int pieceIndex)
    {
        this.dataBlocks=new ArrayList<>(INITIAL_CAPACITY);
        this.currentSize =0;
        this.pieceIndex=pieceIndex;
        this.isDownloaded=false;
        this.isWritten=false;
    }

    public Piece(int pieceIndex,ArrayList<DataBlock> dataBlocks)
    {
        this.dataBlocks=dataBlocks;
        //this.currentSize =dataBlocks.size();
        this.pieceIndex=pieceIndex;
    }


    public int getFirstNonRequestedBlockIndex()
    {
        return this.dataBlocks.stream().filter(p->!p.isRequested()).findFirst().get().getIndex();
    }

    private void initializeDataBlocks()
    {
        int offset=0;
        for (DataBlock block: dataBlocks) {
            block.setStart(offset);
            offset+=DataBlock.MAX_CAPACITY; //potencijalno overflow
        }
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public int getAllowedDownloadSize()
    {
        return Integer.min(1<<14,this.pieceSize-this.currentSize);
    }

    public  ArrayList<DataBlock> getDataBlocks(){
        return dataBlocks;
    }


    public void addBlock(DataBlock block, int position) {
        this.dataBlocks.add(position,block);
        this.currentSize+=block.getSize();
        if(this.currentSize==this.pieceSize)
            this.isDownloaded=true;
    }

   public int getBlockSize(int blockIndex) {
            return this.dataBlocks.get(blockIndex).getSize();
    }


    public boolean compareHashes(byte[] hash) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        for(DataBlock block:dataBlocks) {
            baos.write(block.getBlock());
        }
        MessageDigest digest=MessageDigest.getInstance("SHA-1");
        byte[] resultHash=digest.digest(baos.toByteArray());
        return Arrays.equals(resultHash,hash);
    }

    public boolean isDownloaded() {
       return this.isDownloaded;
    }

    public boolean isRequestedPiece() //if any of the blocks are requested, then the whole Piece will still be considered requested
    {
        boolean isRequested=false;
        for(DataBlock block:this.dataBlocks) {
            isRequested=isRequested||block.isRequested();
        }
        return isRequested;
    }

    public void setPieceSize(int pieceSize) {
        this.pieceSize = pieceSize;
    }

    public int getPieceIndex() {
        return pieceIndex;
    }

    private int calculateNumberOfBlocks(int pieceSize)
    {
        if(pieceSize%DataBlock.MAX_CAPACITY>0)
            return (pieceSize/DataBlock.MAX_CAPACITY)+1;
        return pieceSize/DataBlock.MAX_CAPACITY;
    }

}
