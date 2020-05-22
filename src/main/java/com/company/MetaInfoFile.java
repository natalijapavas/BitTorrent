/*****************************************************
 * MetaInfoFile - Stores relevant information kept in  .torrent File
 * Fields:
 * -fileContent = Stores decoded torrent file as a Map
 * -infoHash = Stores SHA1 hash of the VALUE of info key kept in .torrent file
 * -infoHashHex = Same as above, however it is stored as a HEX string (for visual and testing purposes)
 * Additionally:

 ****************************************************/

package com.company;
import com.company.Bencoding.BencodeDecoder;
import com.company.Bencoding.BencodeEncoder;
import com.company.Bencoding.BencodeValue;
import com.company.Bencoding.BencodeFormatException;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/*TODO Handling of multiple file mod to be done */


public class MetaInfoFile {
    public Map<String, BencodeValue> fileContent;
    private char fileMode='n'; //s for single file, m for multiple, n for none
    private byte[] infoHash;
    private String infoHashHex;
    private List<byte[]> pieceHashes;


    public MetaInfoFile(){}

    public void readFileContent(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        BencodeValue bencodeValue=new BencodeDecoder(inputStream).decode();
        this.fileContent=bencodeValue.getMap();
        this.calculateInfoHash2();
        this.parsePieceHashes(bencodeValue);
        //parsePieceHashes(object)
    }



    private void calculateInfoHash2() throws NoSuchAlgorithmException, IOException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        outputStream.write('d');
        BencodeEncoder encoder=new BencodeEncoder(outputStream);
        Map<String, BencodeValue> infoDictionary=this.getInfoMap();
        for(Map.Entry entry: infoDictionary.entrySet())
        {
            encoder.encode(entry.getKey());
            encoder.encode(entry.getValue());
        }
        outputStream.write('e');
        this.infoHash =sha1.digest(outputStream.toByteArray());
        this.infoHashHex=javax.xml.bind.DatatypeConverter.printHexBinary(infoHash);
        outputStream.close();
    }

    public String getInfoHashHex() {
        return infoHashHex;
    }

    public byte[] getInfoHash() {
        return infoHash;
    }

    public Map<String, BencodeValue> getFileContent() {
        return fileContent;
    }


    public void setFileMode() throws BencodeFormatException {
        Map<String, BencodeValue> info = this.getInfoMap();
        Set<String> keySet = info.keySet();
        if (keySet.contains("length") && keySet.contains("path"))
        {this.fileMode='m';}
        else if(keySet.contains("name")&& keySet.contains("length"))
        {this.fileMode='s';}
        else
        {this.fileMode='n';}
    }

    public char getFileMode() { return this.fileMode; }

    public String getAnnounce() throws BencodeFormatException { return this.fileContent.get("announce").getString(); }

    public String getComment() throws BencodeFormatException { return this.fileContent.get("comment").getString(); }

    public String getEncoding() throws BencodeFormatException { return this.fileContent.get("encoding").getString(); }

    public Map<String,BencodeValue>getInfoMap() throws BencodeFormatException { return this.fileContent.get("info").getMap(); }


    public int getPieceLength() throws BencodeFormatException {
        Map<String,BencodeValue>infoMap=this.getInfoMap();
        Set<String> keySet=infoMap.keySet();
        if(!keySet.contains("piece length")) { return 0; }

        return infoMap.get("piece length").getInt();
    }

    public byte[] getPieces() throws BencodeFormatException
    {
        Map<String,BencodeValue>infoMap=this.getInfoMap();
        Set<String> keySet=infoMap.keySet();
        if(!keySet.contains("pieces"))  { return null; }

        return infoMap.get("pieces").getBytes();
    }

    public int getNumberOfPieces() throws BencodeFormatException { return this.getPieces().length/20; }

    public String getName() throws BencodeFormatException {
        Map<String,BencodeValue>infoMap=this.getInfoMap();
        Set<String> keySet=infoMap.keySet();
        if(!keySet.contains("name"))
            return null;
        return infoMap.get("name").getString();
    }

    public int getFileLength() throws BencodeFormatException
    {
        if(this.fileMode!='s')
            return 0;
        Map<String,BencodeValue>infoMap=this.getInfoMap();
        Set<String> keySet=infoMap.keySet();
        if(!keySet.contains("length"))
            return 0;
        return infoMap.get("length").getInt();
    }

    public BencodeValue getFiles() throws BencodeFormatException
    {
        if(this.fileMode!='m')
            return null;
        Map<String,BencodeValue>infoMap=this.getInfoMap();
        Set<String> keySet=infoMap.keySet();
        if(!keySet.contains("files"))
            return null;
        return infoMap.get("files");
    }

    public byte[] getPieceHash(int index)
    {
        if(index<this.pieceHashes.size())
            return this.pieceHashes.get(index);
        return null;
    }

    private void parsePieceHashes(BencodeValue value) throws BencodeFormatException {
        final int HASH_SIZE=20;
        int offset;
        byte[] source=fileContent.get("pieces").getBytes();
        for(offset=0; offset<=source.length; offset+=20) {
            byte[] singlePieceHash = new byte[HASH_SIZE];
            System.arraycopy(source, offset, singlePieceHash, 0, HASH_SIZE);
            this.pieceHashes.add(singlePieceHash);
        }
    }

}

