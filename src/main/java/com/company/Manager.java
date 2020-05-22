package com.company;

import com.company.Bencoding.BencodeFormatException;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

public class Manager extends Thread{

    private ArrayList<Peer> peers;
    private ArrayList<Piece> pieces;
    private ServerSocket socket = null;
    private Tracker track;
    private File outputFile;
    private File outputDirectory; //Output directory to which intermediate pieces will be stored, and subsequently resulting file
    private LinkedBlockingDeque<PeerMessage> messages = null;
    private boolean isRunning = false;
    private boolean[] currBitfield;
    private static boolean fullFile = false;
    private Path pathToPieces;
    private int downloaded = 0;
    private boolean isDownloading;

    private int[] pieceRepeating;

    Manager(ArrayList<Peer> peers, Tracker track, File file){
        this.peers = peers;
        this.track = track;
        this.outputFile =file;

        /*

        //making a folder for pieces
        boolean success = new File("C:\\Pieces").mkdir();
        if(!success)
            System.out.println("Failed to create directory!");
        this.pathToPieces = Paths.get("C:\\Pieces");


         */
    }

    //may need datablock -> data in Piece?
    public void addPiece(Piece piece) throws IOException {
        /*RandomAccessFile f = new RandomAccessFile(this.outputFile,"rws");

        f.seek(piece.getPieceLength() * piece.getPieceIndex());
        f.write(data);
        f.close();
        */

        int num_of_digits=0;
        int index = piece.getPieceIndex();
        for(int i = 0; i < piece.getPieceIndex(); i++){
            if(index > 10) {
                index = index / 10;
                num_of_digits++;
            }
            else
                break;
        }
        String zeros = "";
        for(int i = 0; i < this.track.getMetaInfoFile().getNumberOfPieces()- num_of_digits; i++){
            zeros = "0" + zeros;
        }
        String path = this.pathToPieces + zeros + piece.getPieceIndex() + "piece";
        File file = new File(path,"w");
        boolean result = file.createNewFile();
        if(!result)
            System.out.println("File already exists");

        RandomAccessFile raf = new RandomAccessFile(path,"w");
        for(int i = 0; i < piece.getDataBlocks().size(); i++) {
            byte[] data = piece.getDataBlocks().get(i).getBlock();
            raf.write(data);
        }
        raf.close();
        System.out.println("saved piece" + piece.getPieceIndex());
        downloaded += piece.getPieceLength();
    }

    public void run(){
        while(this.isRunning == true){
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
                if(p.getBitfield()[i] == true){
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



    public boolean fileComplete(){
        for(int i = 0; i < this.currBitfield.length; i++){
            if(this.currBitfield[i] == false)
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
