package com.company;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 Download Manager overall serves to coordinate communication with Tracker and Peers. It keeps ongoing download stats
 As well as the pieces which we currently have
 (Work in progress)
 */

public class DownloadManager {
    private byte[] bitfield;
    private PeerInfo peerClient;
    private ArrayList<Piece> pieces;

    public DownloadManager(PeerInfo peerClient, byte [] bitfield)
    {
        this.bitfield=bitfield;
        this.peerClient=peerClient;
    }

    public DownloadManager(PeerInfo peerClient)
    {
        this.peerClient=peerClient;
    }

    public DownloadManager(PeerInfo peerClient,ArrayList<Piece> pieces)
    {
        this.peerClient=peerClient;
        this.pieces=pieces;
    }

    public void addPiece(Piece piece)
    {
        this.pieces.add(piece);
        Comparator<Piece> pieceSorter = Comparator.comparing(Piece::getPieceIndex);
        this.pieces.sort(pieceSorter);
    }



    public byte[] getBitfield() {
        return bitfield;
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
}
