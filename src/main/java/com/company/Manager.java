package com.company;

import com.company.Bencoding.BencodeFormatException;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;

public class Manager extends Thread{

    private ArrayList<Peer> peers;
    private ArrayList<Piece> pieces;
    private ServerSocket socket;
    private Tracker track;
    private File outputFile;
    private LinkedBlockingDeque<PeerMessage> messages = null;
    private boolean isRunning = false;
    private boolean[] currBitfield;
    private static boolean fullFile = false;
    private Path directoryPath;
    private boolean isDownloading;
    private int downloaded;
    private int[] pieceRepeating;

    public Manager(ArrayList<Peer> peers, Tracker track, File file){
        this.peers = peers;
        this.track = track;
        this.outputFile =file;
        this.downloaded=0;

        try {
            this.directoryPath=Paths.get(System.getProperty("user.home"),this.track.getMetaInfoFile().getName());
            if(!Files.exists(directoryPath)) { Files.createDirectory(directoryPath); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Manager(ArrayList<Peer> peers, Tracker track){
        this.peers = peers;
        this.track = track;
        this.downloaded=0;
        try {
            this.directoryPath=Paths.get(System.getProperty("user.home"),this.track.getMetaInfoFile().getName());
            if(!Files.exists(this.directoryPath)) { Files.createDirectory(this.directoryPath); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Manager(ArrayList<Peer> peers, Tracker track,String directoryPath){
        this.peers = peers;
        this.track = track;
        this.downloaded=0;
        try {
            this.directoryPath=Files.createDirectory(Paths.get(directoryPath,this.track.getMetaInfoFile().getName()));
            if(!Files.exists(this.directoryPath)) { Files.createDirectory(this.directoryPath); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void writePiece(Piece piece) throws IOException {
        /*RandomAccessFile f = new RandomAccessFile(this.outputFile,"rws");

        f.seek(piece.getPieceLength() * piece.getPieceIndex());
        f.write(data);
        f.close();
        */

        int max_num_of_digits=0;
        int index_num_of_digits=0;
        int index = piece.getPieceIndex();
        int maxIndex=this.track.getMetaInfoFile().getNumberOfPieces();

        /*for(int i = 0; i < piece.getPieceIndex(); i++){
            if(index > 10) {
                index = index / 10;
                num_of_digits++;
            }
            else
                break;
        } */
        while(maxIndex>0 || index>0)
        {
            maxIndex/=10;
            index/=10;
            max_num_of_digits++;
            index_num_of_digits++;
        }
        max_num_of_digits++; //increase by one to always have a leading zero in the name;

        char zeros[]=new char[max_num_of_digits-index_num_of_digits];
        Arrays.fill(zeros,'0');
        //StringBuilder zeros= new StringBuilder();
        //for(int i = 0; i < this.track.getMetaInfoFile().getNumberOfPieces()- num_of_digits; i++){
        //    zeros.insert(0, "0");
        //}
        StringBuilder fileNameBuilder=new StringBuilder();
        fileNameBuilder.append(zeros);
        fileNameBuilder.append(piece.getPieceIndex());
        fileNameBuilder.append("_piece");

        try{
            Path filePath=Paths.get(this.directoryPath.toString(),fileNameBuilder.toString());
            //String path = this.pathToPieces + zeros.toString() + piece.getPieceIndex() + "piece";
            if(Files.exists(filePath) && Files.isRegularFile(filePath))
            {
                System.out.println("File already exists");
                return;
            }
            /*File file = new File(filePath);
            boolean result = file.createNewFile();
            if (!result)
                System.out.println("File already exists");

            RandomAccessFile raf = new RandomAccessFile(path, "w"); */
            File outputPieceFile=new File(filePath.toString());
            FileOutputStream fileOutputStream=new FileOutputStream(outputPieceFile);
            piece.getDataBlocks().stream().map(DataBlock::getBlock).forEach(b -> {
                try {
                    fileOutputStream.write(b);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            /*for (int i = 0; i < piece.getDataBlocks().size(); i++) {
                byte[] data = piece.getDataBlocks().get(i).getBlock();
                raf.write(data);
            } */
            fileOutputStream.close();
            //raf.close();
            System.out.println("saved piece" + piece.getPieceIndex());
            downloaded += piece.getPieceLength();
        }
        catch (InvalidPathException e) { e.printStackTrace(); }
        catch(IOException e) { e.printStackTrace(); }
    }

    public void run(){
        while(this.isRunning){
            try{
                parse();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setIsDownloading(){
        isDownloading = false;
    }

    public int getRarest(Peer peer){
        pieceRepeating();
        int min = Integer.MAX_VALUE; //Javas integer infinity
        int minI = pieceRepeating.length;
        for(int i = 0; i < this.pieceRepeating.length;i++){
            //checking if we already have this piece
            if(this.currBitfield[i] == false && peer.getBitfield()[i] == true){
                //if we don't have the piece, we check how common it is
                //we want to find the rarest
                if(min > this.pieceRepeating[i]){
                    min = pieceRepeating[i];
                    minI = i;
                }
            }
        }
        if(minI==pieceRepeating.length)
            return -1;
        return pieceRepeating[minI];
    }

    //counting how many peers have the piece that we need - for each piece
    public void pieceRepeating(){
        for(Peer p: this.peers){
            for(int i = 0; i < this.pieceRepeating.length; i++){
                //we count instances of the pieces that we need
                if(p.getBitfield()[i]){
                    this.pieceRepeating[i]++;
                }
            }
        }
    }

    public boolean[] getCurrBitfield(){
        return currBitfield;
    }


    //decoding messages recieved from the peers
    public void parse() throws Exception {
        PeerMessage peerMessage;


        if((peerMessage = this.messages.take()) != null){
            int numOfPieces;
            int pieceLength = peerMessage.getPeer().getPeerInfo().getTracker().getMetaInfoFile().getPieceLength();
            int fileLength = peerMessage.getPeer().getPeerInfo().getTracker().getMetaInfoFile().getFileLength();

             /*pieceLength=this.track.getMetaInfoFile().getPieceLength();
             numOfPieces=this.track.getMetaInfoFile().getNumberOfPieces();

              */
            if(fileLength%pieceLength != 0)
                numOfPieces = fileLength / pieceLength + 1;
            else
                numOfPieces = fileLength / pieceLength;
            switch (peerMessage.getMessage().id){
                case Message.keepAliveID:
                    peerMessage.getPeer().sendMessage(Message.KEEP_ALIVE);
                    break;
                case Message.chokeID:
                    peerMessage.getPeer().getPeerInfo().setPeerChocking(true);
                    break;
                case Message.unchokeID:
                    peerMessage.getPeer().getPeerInfo().setPeerChocking(false);
                    break;
                case Message.interestedID:
                    peerMessage.getPeer().getPeerInfo().setPeerInterested(true);
                    peerMessage.getPeer().sendMessage(Message.UNCHOKE);
                case Message.uniterestedID:
                    peerMessage.getPeer().getPeerInfo().setPeerInterested(false);
                    break;
                case Message.haveID:
                    boolean haspiece[] = peerMessage.getPeer().getPeerInfo().getHasPiece();
                    Message.Have have = (Message.Have) peerMessage.getMessage();
                    haspiece[have.getPieceIndex()] = true;
                    peerMessage.getPeer().getPeerInfo().setHasPiece(haspiece);
                    break;
                case Message.bitfieldID:

                    Message.Bitfield BitMSG = (Message.Bitfield) peerMessage.getMessage();
                    byte[] bitfieldPayload = BitMSG.getBitfield();
                    //with this i should have updated bitfield in peer class
                    boolean[] bitfield = peerMessage.getPeer().bitfieldToBool(bitfieldPayload,numOfPieces);

                    for(int i = 0; i < peerMessage.getPeer().getBitfield().length; i++){
                        //we want a piece if we need it, someone has it, and we dont have it
                        //in this case we send a message INTERESTED
                        if(peerMessage.getPeer().getBitfield()[i] == true && this.currBitfield[i] == false){
                            peerMessage.getPeer().sendMessage(Message.INTERESTED);
                            peerMessage.getPeer().getPeerInfo().setInterested(true);
                            break;
                        }

                    }
                    //if we dont find a piece we need that we dont have, we dont send a message
                    break;

                case Message.pieceID:
                    Message.Piece piece = (Message.Piece) peerMessage.getMessage(); //if a peer sends you a Piece message, he is sending you a block inside of a piece, so you still don't know if you have whole piece or now
                    if(piece.start > 0) //this means that peer has a piece
                        peerMessage.getPeer().sendMessage(new Message.Have(piece.ind));
                    else
                        break;
                case Message.requestID:
                    /*
                    Message.Request req = (Message.Request) peerMessage.getMessage();
                    try {

                        //we are trying to find a piece of index req.ind*pieceLength + req.start
                        peerMessage.getPeer().getPeerInfo().getOutputFile().seek(pieceLength * req.ind + req.start);

                        byte[] ourPiece = new byte[req.length];
                        //we are reading that piece
                        peerMessage.getPeer().getPeerInfo().getOutputFile().readFully(ourPiece);

                        //ADD HERE UPLOADING AND DOWNLOADING REQUESTED PIECES

                        peerMessage.getPeer().sendMessage(new Message.Piece(req.ind,req.start,ourPiece));
                        break;
                    } catch(IOException e){
                        e.printStackTrace();
                        break;
                    }

                     */
                    
                    break;
                case Message.cancelID:
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + peerMessage.getMessage().id);

            }
        }

    }


    public boolean isFileComplete(){
        for (boolean b : this.currBitfield) {
            if (b == false)
                return false;
        }
        return true;
    }

    public synchronized void getMessage(PeerMessage message){
        if(message == null){
            return;
        }
        this.messages.add(message);
    }


    public byte[] readFile(int index,int offset,int length) throws IOException{
        RandomAccessFile file = new RandomAccessFile(this.outputFile,"r");
        byte[] data =  new byte[length];

        file.seek(this.track.getMetaInfoFile().getPieceLength() * index + offset);
        file.read(data);
        file.close();
        return data;
    }



}
