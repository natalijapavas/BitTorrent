package com.company;
import com.company.Bencoding.BencodeEncoder;
import com.company.Bencoding.BencodeValue;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;



public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        //Linkovi za Testiranje
        String path="/home/korisnik/Desktop/torrents/KNOPPIX_V7.7.1DVD-2016-10-22-EN.torrent";
        //String path="/home/korisnik/Desktop/torrents/VISB tablice.pdf.torrent";
        //String path="/home/korisnik/Desktop/torrents/learning_curve-highvariance.png.torrent";
        //String path="/home/korisnik/Desktop/torrents/Nacrtna geometrija - Zagorka Snajder.pdf.torrent";
        //String path="/home/korisnik/Desktop/torrents/Metodika-nastave-Zadaci-6-poena-1.docx.torrent";


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

        try (FileInputStream inputStream = new FileInputStream(torrentFile))
        {
            MetaInfoFile document=new MetaInfoFile();
            document.readFileContent(inputStream);


            System.out.println("Key set: ");
            for(String key:document.fileContent.keySet()) {
                System.out.println(key);
            }
            System.out.println("Info Key set: ");
            for(String key:document.getInfoMap().keySet()) {
                System.out.println(key);
            }
            System.out.println("BITNOOOO!: "+document.getFileLength());
            System.out.println("BITNOOOO!: "+document.getNumberOfPieces());
            System.out.println("Info hash is: "+document.getInfoHashHex());

            System.out.println("************************************Testing http tracker requests*************************************");
            Tracker tracker=new Tracker(document);
            BencodeValue bencodeHttpResponse=tracker.sendHTTPAnnounceRequest();
            Map<String,BencodeValue> bencodeMap=bencodeHttpResponse.getMap();
            System.out.println("Response type: "+bencodeHttpResponse.getValueType());
            System.out.println("*******************************Showing key set of httpResponse***************************************");

            for(Map.Entry e:bencodeMap.entrySet())
            {
                System.out.println((String)e.getKey());
            }
            System.out.println("********************************Extraction and connection to peers*********************************** ");

            List<BencodeValue> peerList=bencodeMap.get("peers").getList();
            PeerInfo testPeerInfo=new PeerInfo(null,0,null, null);
            for(BencodeValue value:peerList)
            {

                Map<String, BencodeValue> peer=value.getMap();
                System.out.println("New Peer: ");
                System.out.println("Peer id: "+peer.get("peer id").getString());
                System.out.println("Port: "+peer.get("port").getNumber());
                System.out.println("Ip: "+peer.get("ip").getString());
                testPeerInfo=new PeerInfo(peer.get("peer id").getString(), peer.get("port").getInt(),peer.get("ip").getString(),tracker);
                break;
            }
            Peer testPeer=new Peer(testPeerInfo);
            System.out.println("Socket is created: "+testPeer.createSocket());
            //testPeer.

            System.out.println("************************************Testing Message class *******************************************");

            byte[] block=new byte[10];
            for (int i = 0; i < block.length; i++) {
                block[i]=(byte) i;
            }


            final Message cancel =new Message.Cancel(2,3,4);
            File testFile=new File("/home/korisnik/Desktop/torrents/test.txt");
            FileOutputStream fileOutputStream=new FileOutputStream(testFile);
            DataOutputStream testOut=new DataOutputStream(fileOutputStream);
            Message.encode(cancel,testOut);
            testOut.close();
            fileOutputStream.close();
            FileInputStream fileInputStream=new FileInputStream(testFile);
            DataInputStream testIn=new DataInputStream(fileInputStream);
            Message resutlMsg=Message.decode(testIn);
            testIn.close();
            //System.out.println("Message id: "+resutlMsg.id);
            //Message.Request result=(Message.Request) resutlMsg;
            //System.out.println(result.len);
            //System.out.println(result.ind);
            //System.out.println(result.start);

            //System.out.println("Piece index: "+result.getPieceIndex());
            //System.out.println("Indicator: "+(result.ind));
            //System.out.println("Start: "+(result.start));
            //System.out.println("Length: "+(result.clength));
            /*for(int i=0; i<result.getBitfield().length; i++)
            {
                System.out.println("El :" +i+" "+result.getBitfield()[i]);
            }
            fileInputStream.close();
            testIn.close(); */

            /*System.out.println("**************************** Testing parallel tracker working ***************************");
            Tracker tracker=new Tracker(document);
            new Thread(tracker).start();
            while(true)
            {
                Thread.sleep(100000);
                System.out.println("Lokalna test petlja");
            } */

            System.out.println("**************************** Testing Example of file writing ***************************");
            Manager testManager=new Manager(tracker.generatePeerList(bencodeHttpResponse),tracker);
            Piece piece=new Piece(0);
            ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            byteArrayOutputStream.write(1);
            byteArrayOutputStream.write(2);
            byteArrayOutputStream.write(3);
            byteArrayOutputStream.write(4);
            byteArrayOutputStream.write(5);
            byte[] testBytes=byteArrayOutputStream.toByteArray();
            DataBlock dataBlock0=new DataBlock(0,0,testBytes,testBytes.length);
            piece.addBlock(dataBlock0,0);

            DataBlock dataBlock1=new DataBlock(0,1,testBytes,testBytes.length);
            piece.addBlock(dataBlock1,1);

            DataBlock dataBlock2=new DataBlock(0,2,testBytes,testBytes.length);
            piece.addBlock(dataBlock2,2);

            DataBlock dataBlock3=new DataBlock(0,3,testBytes,testBytes.length);
            piece.addBlock(dataBlock3,3);

            Piece piece1=new Piece(1);

            piece1.addBlock(dataBlock0,0);


            piece1.addBlock(dataBlock1,1);


            piece1.addBlock(dataBlock2,2);


            piece1.addBlock(dataBlock3,3);
            testManager.writePiece(piece);
            testManager.writePiece(piece1);

        }
        //catch (IOException | InterruptedException e) {
        //    e.printStackTrace();
        //}
        catch(IOException e) {
            e.printStackTrace();
        }






    }
}

