package com.company;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;


public class ManagerInfo {
    public TreeSet<Peer> currentPeerList; //za sinhronizovanje + treba ubiti konekcije koje mi ne koriste
    private ArrayList<Piece> pieces; //za sinhronizovanje
    public boolean[] clientBitfield;
    private AtomicInteger downloaded;
    private AtomicInteger uploaded;
    public int[] pieceRepeating; //za sinhronizovanje
    private boolean[] isRequested; //questionable if I need it
    public boolean isDownloadedFile;


    /*public ManagerInfo(Tracker tracker) {
        this.tracker = tracker;
        this.downloaded = new AtomicInteger(0);
        this.uploaded = new AtomicInteger(0);
    } */


    public ManagerInfo(TreeSet<Peer> currentPeerList,int pieceNumber,int pieceSize,int lastPieceSize) {
        this.currentPeerList=currentPeerList;
        this.pieces = new ArrayList<>(pieceNumber);
        //set piece size for each piece, last piece differs usually in size compared to others
        for(int i=0; i<pieceNumber-1; i++) {
            this.pieces.set(i,new Piece(i,pieceSize));
        }
        this.pieces.set(pieceNumber-1,new Piece(pieceNumber-1,lastPieceSize));
        this.clientBitfield = new boolean[pieceNumber];
        this.pieceRepeating = new int[pieceNumber];
        this.isRequested = new boolean[pieceNumber];
        this.downloaded = new AtomicInteger(0);
        this.uploaded = new AtomicInteger(0);
        this.isDownloadedFile=false;
    }


    public int getRarest(PeerInfo peerInfo){ //prebaciti u manager info
        this.pieceRepeating();
        int min = Integer.MAX_VALUE; //Javas integer infinity
        int minI = this.pieceRepeating.length;
        for(int i = 0; i < this.pieceRepeating.length;i++){
            //checking if we already have this piece
            if(this.clientBitfield[i] == false && peerInfo.getBitfield()[i] == true){
                //if we don't have the piece, we check how common it is
                //we want to find the rarest
                if(min > this.pieceRepeating[i]){
                    min = this.pieceRepeating[i];
                    minI = i;
                }
            }
        }
        if(minI==this.pieceRepeating.length)
            return -1;
        return this.pieceRepeating[minI];
    }


    //counting how many peers have the piece that we need - for each piece
    public void pieceRepeating() {
        for (Peer p : this.currentPeerList) {
            for (int i = 0; i < this.pieceRepeating.length; i++) {
                //we count instances of the pieces that we need
                if (p.getPeerInfo().getBitfield()[i]) {
                    this.pieceRepeating[i]++;
                }
            }
        }
    }


    public int getMissingBlockIndex(int pieceIndex) {
        return this.pieces.get(pieceIndex).getFirstNonRequestedBlockIndex();
    }


    public int getBlockStartingPosition(int pieceIndex) {
        return this.pieces.get(pieceIndex).getCurrentSize();
    }

      public int getBlockSizeToRequest(int pieceIndex,int blockIndex)
    {
        try {
            return this.pieces.get(pieceIndex).getBlockSize(blockIndex); //get blockSize
        }catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return -1;
        }
    }


    public synchronized int getDownloaded() {
        return this.downloaded.get();
    }

    public synchronized int getUploaded() {
        return this.uploaded.get();
    }

    public void incrementDownloaded(int downloaded) {
        this.downloaded.addAndGet(downloaded);
    }

    public void incrementUploaded(int downloaded) {
        this.uploaded.addAndGet(downloaded);
    }

    public synchronized void removePeer(Peer peer) { this.currentPeerList.remove(peer); }

    public synchronized void setPieces(ArrayList<Piece> pieces) { this.pieces = pieces; }

    public synchronized void addBlock(DataBlock block) {this.pieces.get(block.getIndex()).addBlock(block,block.getStart());}
}
