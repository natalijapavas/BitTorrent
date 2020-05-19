package com.company;

import java.io.*;
import java.net.Socket;

public class PeerInfo {
    private int port;
    private String name;
    private String ip;

    //All connections start off as 'Not interested' and 'Choked'

    private boolean chockingPeer = true;
    private boolean peerChocking = true;


    private boolean interested = false;
    private boolean peerInterested = false;

    private byte[] peerId;

    private boolean handshake = false;

    private boolean[] hasPiece;

    private int currentPieceIndex = -1;
    private File outputFile;

    private Tracker track; //Tracker through which we obtained relevant data
    private int downloaded = 0;
    private int uploaded = 0;

    private boolean badPeer = false;

    protected OutputStream out;
    protected InputStream in;
    public PeerInfo(String name, int port, String ip, File outputFile, Tracker track){
        this.name = name;
        this.port = port;
        this.ip = ip;

        this.track = track;
        this.outputFile = outputFile;
    }

    public PeerInfo(String name, int port, String ip,Tracker track)
    {
        this.name = name;
        this.port = port;
        this.ip = ip;

        this.track = track;
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

    public int getPort()
    {
        return this.port;
    }

    public String getIp(){
        return ip;
    }

    public OutputStream getOut() {
        return out;
    }

    public InputStream getIn(){
        return in;
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

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getOutputFile(){ return outputFile;}

    public Tracker getTracker(){return track;}
}
