package com.company;
import com.company.Bencoding.BencodeEncoder;
import com.company.Bencoding.BencodeValue;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


/*Reading torrent tests performed on local machine
/*TODO extract SHA-1 hashed parts from the file */
public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        //Linkovi za Testiranje
        //String path="/home/korisnik/Desktop/torrents/KNOPPIX_V7.7.1DVD-2016-10-22-EN.torrent";
        String path="/home/korisnik/Desktop/torrents/bunny.torrent";
        //String path="/home/korisnik/Desktop/torrents/alice.torrent";
        //String path="/home/korisnik/Desktop/torrents/sintel.torrent";
        //String path="/home/korisnik/Desktop/torrents/tears-of-steel.torrent";
        //String path="/home/korisnik/Desktop/torrents/cosmos-laundromat.torrent";

        File torrentFile = new File(path);
        System.out.println("Testiranje Bencode Encoder funkcija:");
        Integer int1=new Integer(1);
        int int2=2;
        Long long2=new Long(2000);
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        BencodeEncoder encoder=new BencodeEncoder(outputStream);
        encoder.encode(int1);
        System.out.println("Test 1:"+ outputStream.toString("UTF-8"));
        outputStream.flush();
        encoder.encode(new Integer(int2));
        System.out.println("Test 2:"+ outputStream.toString("UTF-8"));
        outputStream.flush();
        encoder.encode(long2);
        System.out.println("Test 3:"+ outputStream.toString("UTF-8"));
        outputStream.flush();
        String test4="Hello there";
        encoder.encode(test4);
        System.out.println("Test 4:"+ outputStream.toString("UTF-8"));
        outputStream.flush();
        System.out.println("Testing encoding on a list: ");
        BencodeValue ben1=new BencodeValue("String1");
        BencodeValue ben2=new BencodeValue("String2");
        BencodeValue ben3=new BencodeValue(new Integer(1));
        BencodeValue ben4=new BencodeValue("String3");
        ArrayList<BencodeValue> arrayList=new ArrayList<>(8);
        arrayList.add(ben1);
        arrayList.add(ben2);
        arrayList.add(ben3);
        arrayList.add(ben4);
        outputStream.reset();
        encoder.encode(arrayList);
        System.out.println("Test list:"+ outputStream.toString("UTF-8"));
        outputStream.close();
        System.out.println("***************************Bencode testing uspesan!****************************************\n");

        System.out.println("************************* Let the Hunger Games begin ****************************************");
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(torrentFile);
            MetaInfoFile document=new MetaInfoFile();
            document.readFileContent(inputStream);
            System.out.println("Key set: ");
            for(String key:document.getInfoMap().keySet())
            {
                System.out.println(key);
            }
            System.out.println("Printing contents of info dict: ");

            System.out.println("Info map bencoded");
            System.out.println("Info hash novi je: "+document.getInfoHashHex());
            Tracker tracker=new Tracker(document);
            System.out.println("************************************Testing http tracker requests***************************");
            BencodeValue bencodeVHttpResponse=tracker.sendHTTPAnnounceRequest();
            System.out.println("Response type: "+bencodeVHttpResponse.getValueType());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

