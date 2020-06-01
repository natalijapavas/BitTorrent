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
    private boolean[] bitfield;
    private boolean[] hasPiece;

    //private int currentPieceIndex = -1;
    private File outputFile;

    private Tracker track; //Tracker through which we obtained relevant data
    private int downloaded = 0;
    private int uploaded = 0;
    private int numberOfPieces;

    private boolean badPeer = false;

    protected DataOutputStream out;
    protected DataInputStream in;

    public PeerInfo(String name, int port, String ip, File outputFile, Tracker track) {
        this.name = name;
        this.port = port;
        this.ip = ip;

        this.track = track;
        this.outputFile = outputFile;
        this.badPeer=false;
    }

    public PeerInfo(String name, int port, String ip, Tracker track) {
        this.name = name;
        this.port = port;
        this.ip = ip;
        this.track = track;
    }

    public PeerInfo(String name, int port, String ip, Tracker track, int numberOfPieces) {
        this.name = name;
        this.port = port;
        this.ip = ip;
        this.track = track;
        this.numberOfPieces=numberOfPieces;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    public String getIp() {
        return ip;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isChockingPeer() {
        return chockingPeer;
    }

    public void setChockingPeer(boolean chockingPeer) {
        this.chockingPeer = chockingPeer;
    }

    public boolean isPeerChocking() {
        return peerChocking;
    }

    public void setPeerChocking(boolean peerChocking) {
        this.peerChocking = peerChocking;
    }

    public File getOputputFile() {
        return outputFile;
    }

    public int getNumberOfPieces() {
        return numberOfPieces;
    }

    public boolean isInterested() {
        return interested;
    }

    public void setInterested(boolean interested) {
        this.interested = interested;
    }

    public boolean isPeerInterested() {
        return peerInterested;
    }

    public void setPeerInterested(boolean peerInterested) {
        this.peerInterested = peerInterested;
    }

    public byte[] getPeerId() {
        return peerId;
    }

    public void setPeerId(byte[] peerId) {
        this.peerId = peerId;

    }

    public void setNewPiece(int pieceIndex)
    {
        this.hasPiece[pieceIndex]=true;
    }


    public boolean[] getHasPiece() {
        return hasPiece;
    }

    public void setHasPiece(boolean[] hasPiece) {
        this.hasPiece = hasPiece;
    }

    void setBadPeer() { //used in case of cancel msg
        badPeer=true;
    }

    public void setBitfield(boolean[] bitfield) {
        this.bitfield = bitfield;
    }

    public boolean[] getBitfield() {
        return bitfield;
    }

    public boolean isHandshake(){
        return handshake;
    }

    public void setHandshake(boolean handshake){
        this.handshake = handshake;
    }

    /*public int getCurrentPieceIndex(){
        return currentPieceIndex;
    } */

    /*public void setCurrentPieceIndex(int currentPieceIndex){
        this.currentPieceIndex = currentPieceIndex;
    } */

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getOutputFile(){ return outputFile;}

    public boolean isBadPeer() {
        return badPeer;
    }

    public Tracker getTracker(){return track;}
}
