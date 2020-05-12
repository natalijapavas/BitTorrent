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


    public Piece()
    {
        this.dataBlocks=new ArrayList<>(INITIAL_CAPACITY);
        this.size=0;
    }

    public Piece(ArrayList<DataBlock> dataBlocks)
    {
        this.dataBlocks=dataBlocks;
        this.size=dataBlocks.size();
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


}
