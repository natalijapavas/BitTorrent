package com.company;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

public class PeerInfo {
    int port;
    String name;
    String ip;

    //All connections start off as 'Not interested' and 'Choked'

    private boolean chockingPeer = true;
    private boolean peerChocking = true;


    private boolean interested = false;
    private boolean peerInterested = false;

    private byte[] peerId;

    private DataInputStream in = null;
    private DataOutputStream out = null;

    private boolean handshake = false;

    private boolean[] hasPiece;

    private int currentPieceIndex = -1;
    private RandomAccessFile thefile;

    private Tracker track;
    private int downloaded = 0;
    private int uploaded = 0;

    private boolean badPeer = false;
    private boolean bad;
    private Socket userSocket;

    PeerInfo(String name, int port, String ip, RandomAccessFile thefile, Tracker track){
        this.name = name;
        this.port = port;
        this.ip = ip;
        this.bad = false;
        this.track = track;
        this.thefile = thefile;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setPort(int port){
        this.port = port;
    }

    public String getIp(){
        return ip;
    }

    public void setIp(String ip){
        this.ip = ip;
    }

    public boolean isChockingPeer(){
        return chockingPeer;
    }

    public void setChockingPeer(boolean chockingPeer){
        this.chockingPeer = chockingPeer;
    }

    public boolean isPeerChocking(){
        return peerChocking;
    }

    public void setPeerChocking(boolean peerChocking){
        this.peerChocking = peerChocking;
    }

    public boolean isInterested(){ return interested; }

    public void setInterested(boolean interested){ this.interested = interested;}

    public boolean isPeerInterested(){ return peerInterested; }

    public void setPeerInterested(boolean peerInterested){ this.peerInterested = peerInterested; }

    public byte[] getPeerId(){
        return peerId;
    }

    public void setPeerId(byte[] peerId){
        this.peerId = peerId;

    }

    public boolean[] getHasPiece(){
        return hasPiece;
    }

    public void setHasPiece(boolean[] hasPiece){
        this.hasPiece = hasPiece;
    }

    public Socket getUserSocket(){
        return userSocket;
    }

    public void setUserSocket(Socket userSocket){
        this.userSocket = userSocket;
    }

    public DataInputStream getInput(){
        return in;
    }

    public void setInput(DataInputStream in){
        this.in = in;
    }

    public DataOutputStream getOutput(){
        return out;
    }

    public void setOutput(DataOutputStream out){
        this.out = out;
    }

    public boolean isHandshake(){
        return handshake;
    }

    public void setHandshake(boolean handshake){
        this.handshake = handshake;
    }

    public int getCurrentPieceIndex(){
        return currentPieceIndex;
    }

    public void setCurrentPieceIndex(int currentPieceIndex){
        this.currentPieceIndex = currentPieceIndex;
    }

    public RandomAccessFile getThefile(){ return thefile;}

    public Tracker getTracker(){return track;}
}
