package com.company;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/*
Certainly other methods need to be added, however we will do that as time goes one
 */


public class Piece {
    private final int INITIAL_CAPACITY=10;
    private ArrayList<DataBlock> dataBlocks;
    private int size;
    private int timesRequested;
    private int pieceIndex;


    public Piece(int pieceIndex)
    {
        this.dataBlocks=new ArrayList<>(INITIAL_CAPACITY);
        this.size=0;
        this.timesRequested=0;
        this.pieceIndex=pieceIndex;
    }

    public Piece(int pieceIndex,ArrayList<DataBlock> dataBlocks)
    {
        this.dataBlocks=dataBlocks;
        this.size=dataBlocks.size();
        this.timesRequested=0;
        this.pieceIndex=pieceIndex;
    }


    public void addBlock(DataBlock block, int position)
    {
        this.dataBlocks.add(position,block);
    }

    public boolean compareHashes(byte[] hash) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        for(DataBlock block:dataBlocks)
        {
            baos.write(block.getBlock());
        }
        MessageDigest digest=MessageDigest.getInstance("SHA-1");
        byte[] resultHash=digest.digest(baos.toByteArray());
        return Arrays.equals(resultHash,hash);
    }

    public boolean hasPiece()
    {
        boolean has=true; //We assume that piece is downloaded, if any of them is not, then it will change to false
        for(DataBlock block:this.dataBlocks)
        {
            has=has&&block.isDownloaded();
        }
        return has;
    }

    public boolean isRequestedPiece() //if any of the blocks are requested, then the whole Piece will still be considered requested
    {
        boolean isRequested=false;
        for(DataBlock block:this.dataBlocks)
        {
            isRequested=isRequested||block.isRequested();
        }
        return isRequested;
    }

    public void incrementTimesRequested()
    {
        this.timesRequested++;
    }

    public int getTimesRequested()
    {
        return this.timesRequested;
    }

    public int getPieceIndex() {
        return pieceIndex;
    }
}
