/*******************************************************
 * MetaInfoFile - Stores relevant information in .Torrent File
 * Fields:
 * -announceUrl = Stores URL of the tracker\
 * -fileLength = Length of the file to be downloaded (Single file mode)
 * -pieceLength = Size of each piece of the fil
 *
 *
 * TODO: While writing tracker, consider how to change some of the getter methods
 */


package com.company;
import com.company.Bencoding.BencodeDecoder;
import com.company.Bencoding.BencodeValue;
import com.company.Bencoding.BencodeFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*TODO add announce-list getter */
/*TODO add methods for extracting fields in single and multiple file mode */
/*TODO add method for comparing SHA-1 values (when Tracker done)*/

public class MetaInfoFile {
    private Map<String, BencodeValue> fileContent;
    private char fileMode='n'; //s for single file, m for multiple, n for none

    public MetaInfoFile()
    {
    }

    public void readFileContent(InputStream inputStream) throws IOException {
        BencodeValue object=new BencodeDecoder(inputStream).decode();
        this.fileContent=object.getMap();
    }

    public void setFileMode() throws BencodeFormatException {
        Map<String,BencodeValue>info=this.getInfoMap();
        Set<String>keySet=info.keySet();
        if(keySet.contains("length") && keySet.contains("path"))
            this.fileMode='m';
        else if(keySet.contains("name")&& keySet.contains("length"))
        {
            this.fileMode='s';
        }
        else
        {
            this.fileMode='n';
        }
    }

    public char getFileMode()
    {
        return this.fileMode;
    }

    public String getAnnounce() throws BencodeFormatException
    {
        return this.fileContent.get("announce").getString();
    }

    public String getComment() throws BencodeFormatException
    {
        return this.fileContent.get("comment").getString();
    }

    public String getEncoding() throws BencodeFormatException
    {
        return this.fileContent.get("encoding").getString();
    }

    public Map<String,BencodeValue>getInfoMap() throws BencodeFormatException
    {
        return this.fileContent.get("info").getMap();
    }

    public BencodeValue getBencodedInfoMapValue(String key) throws BencodeFormatException {
        Map<String,BencodeValue>infoMap=this.getInfoMap();
        Set<String> keySet=infoMap.keySet();
        if(!keySet.contains(key))
        {
            return null;
        }
        return infoMap.get(key);
    }

    public int getPieceLength() throws BencodeFormatException {
        Map<String,BencodeValue>infoMap=this.getInfoMap();
        Set<String> keySet=infoMap.keySet();
        if(!keySet.contains("piece length"))
        {
            return 0;
        }
        return infoMap.get("piece length").getInt();
    }

    public String getPieces() throws BencodeFormatException
    {
        Map<String,BencodeValue>infoMap=this.getInfoMap();
        Set<String> keySet=infoMap.keySet();
        if(!keySet.contains("pieces"))
        {
            return "";
        }
        return infoMap.get("piece length").getString();
    }

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

    //Depending on tracker, this may or may not be changed
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






}


