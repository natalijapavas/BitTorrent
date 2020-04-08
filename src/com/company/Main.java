package com.company;
import com.company.Bencoding.BencodeDecoder;
import com.company.Bencoding.BencodeValue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/*Reading torrent tests performed on local machine
/*TODO extract SHA-1 hashed parts from the file */
public class Main {
    public static void main(String[] args) {
        File torrentFile = new File("/home/korisnik/Downloads/sintel.torrent");
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(torrentFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BencodeDecoder reader = new BencodeDecoder(inputStream);
        try {
            Map<String, BencodeValue> document = reader.decodeMap().getMap();
            Map<String, BencodeValue> infoFile=document.get("info").getMap();

            for(String key:infoFile.keySet())
            {
                System.out.println(key+" "+document.get(key));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

