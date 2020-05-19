package com.company;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Manager extends Thread{

    private ArrayList<Peer> peers;
    private ServerSocket socket = null;
    private Tracker track;
    private File file;
    private LinkedBlockingDeque<PeerMessage> messages = null;
    private boolean isRunning = false;
    private PeerInfo peerClient;
    private ArrayList<Piece> pieces;

    public Manager(ArrayList<Peer> peers, Tracker track, File file){
        this.peers = peers;
        this.track = track;
        this.file = file;
        this.pieces=new ArrayList<>();
    }

    public Manager(PeerInfo peerClient,ArrayList<Peer> peers, Tracker track, File file){
        this.peers = peers;
        this.track = track;
        this.file = file;
        this.peerClient=peerClient;
        this.pieces=new ArrayList<>();
    }

    public void addPiece(Piece piece)
    {
        this.pieces.add(piece);
        Comparator<Piece> pieceSorter = Comparator.comparing(Piece::getPieceIndex);
        this.pieces.sort(pieceSorter);
    }

    public Piece getRarestPiece() //maybe it has to be array blocking?
    {
        Comparator<Piece> requestedSorter = Comparator.comparing(Piece::getTimesRequested).reversed();
        PriorityQueue<Piece> sortedPieces=new PriorityQueue<>(requestedSorter);
        for(Piece piece:this.pieces)
        {
            if(!piece.hasPiece())
                sortedPieces.add(piece);
        }
        return sortedPieces.peek();

    }


    //decoding messages recieved from the peers

    public Message parse() throws Exception {
        PeerMessage peerMessage;


        if((peerMessage = this.messages.take()) != null){
            int numOfPieces;
            int pieceLength = peerMessage.getPeer().getPeerInfo().getTracker().getMetaInfoFile().getPieceLength();
            int fileLength = peerMessage.getPeer().getPeerInfo().getTracker().getMetaInfoFile().getFileLength();
            if(fileLength%pieceLength != 0)
                numOfPieces = fileLength / pieceLength + 1;
            else
                numOfPieces = fileLength / pieceLength;
            switch (peerMessage.getMessage().id){
                case Message.keepAliveID:
                    return new Message(0,Message.keepAliveID);
                case Message.chokeID:
                    peerMessage.getPeer().getPeerInfo().setPeerChocking(true);
                    return null;
                case Message.unchokeID:
                    peerMessage.getPeer().getPeerInfo().setPeerChocking(false);
                    return null;
                case Message.interestedID:
                    peerMessage.getPeer().getPeerInfo().setPeerInterested(true);
                    return new Message(1,Message.unchokeID);
                case Message.uniterestedID:
                    peerMessage.getPeer().getPeerInfo().setPeerInterested(false);
                    return null;
                case Message.haveID:
                    boolean haspiece[] = peerMessage.getPeer().getPeerInfo().getHasPiece();
                    Message.Have have = (Message.Have) peerMessage.getMessage();
                    haspiece[have.getPieceIndex()] = true;
                    peerMessage.getPeer().getPeerInfo().setHasPiece(haspiece);
                    return null;
                    /*
                case Message.bitfieldID:
                    Message.Bitfield bitfieldMSG = (Message.Bitfield) peerMessage.getMessage();
                    byte[] bitfield = bitfieldMSG.getBitfield();
                    boolean[] haspiece1 = new boolean[numOfPieces];
                    byte b = 0;

                    for (int i = 0; i < numOfPieces; i++){
                        int j = i; //beacuse i must stay unchanged
                        int k = 0; //place of a curr piece
                        //we want to find a place of each peace
                        //8 bits -> 1 byte
                        k = (int) Math.ceil(j/8); //check math?
                        j = j % 8;
                        b = (byte) (peerMessage.getPeer().getPeerInfo().getPeerId()[k] << j); //left shift


                        if (b < 0){
                            haspiece1[i] = true;
                            peerMessage.getPeer().getPeerInfo().setHasPiece(haspiece1);
                        }
                        else {
                            haspiece1[i] = false;
                            peerMessage.getPeer().getPeerInfo().setHasPiece(haspiece1);
                        }
                    }
                    if(!peerMessage.getPeer().checkBitfield(bitfield)) //if they don't match, we should send interested, right?
                        return new Message(1,Message.interestedID);
                    else
                        return new Message(1, Message.uniterestedID);

                    */

                case Message.pieceID:
                    Message.Piece piece = (Message.Piece) peerMessage.getMessage(); //if a peer sends you a Piece message, he is sending you a block inside of a piece, so you still don't know if you have whole piece or now
                    if(piece.start > 0) //this means that peer has a piece
                        return new Message.Have(piece.ind);
                    else
                        return null;
                /*case Message.requestID:
                    Message.Request req = (Message.Request) peerMessage.getMessage();
                    try {
                        //we are trying to find a piece of index req.ind*pieceLength + req.start
                        peerMessage.getPeer().getPeerInfo().getOutputFile().seek(pieceLength * req.ind + req.start);

                        byte[] ourPiece = new byte[req.length];
                        //we are reading that piece
                        peerMessage.getPeer().getPeerInfo().getOutputFile().readFully(ourPiece);

                        //ADD HERE UPLOADING AND DOWNLOADING REQUESTED PIECES


                        return new Message.Piece(req.ind, req.start, ourPiece);
                    } catch(IOException e){
                        e.printStackTrace();
                        return null;
                    }*/

                case Message.cancelID:
                    return null;

                default:
                    throw new IllegalStateException("Unexpected value: " + peerMessage.getMessage().id);

            }
        }
        return null;

    }




}
