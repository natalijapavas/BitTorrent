package com.company;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class Peer {
    DataInputStream in = null;
    DataOutputStream out = null;
    int downloaded = 0;
    boolean[] bitfield;
    byte[] bfield;
    PeerInfo peerInfo;
    int pieces;


    public Peer(boolean[] bitfield, byte[] bfield, PeerInfo peerInfo){
        this.bitfield = bitfield;
        this.bfield = bfield;
        this.peerInfo = peerInfo;
    }


    //Generate handhake

    public synchronized static byte[] handshake(Tracker track){
        byte[] handshake = new byte[68];
        int currPos = 0;

        handshake[currPos] = 0x13;

        //add the stirng "BitTorrent protocol"
        byte[] BTChars = { 'B', 'i', 't', 'T', 'o', 'r', 'r', 'e', 'n', 't', ' ',
                'p', 'r', 'o', 't', 'o', 'c', 'o', 'l' };
        System.arraycopy(BTChars, 0, handshake, currPos, BTChars.length);
        currPos += BTChars.length;

        //8 empty slots
        for(int i = 0; i < 8; currPos++, i++){
            handshake[currPos] = 0;
        }

        //info hash
        System.arraycopy(track.infohash, 0, handshake, currPos, track.infoHash.length);
        currPos += track.infoHash.length;

        //peer id
        System.arraycopy(track.peerId, 0, handshake, currPos,track.peerId.length);

        return handshake;
    }

    public boolean chackHandhshake(byte[] info, byte[] response) throws IOException {
        byte[] peerHash = new byte[20];
        System.arraycopy(response, 28, peerHash, 0, 20);

        if(!Arrays.equals(peerHash, info))
        {
            return false;
        }

        return true;
    }

    //connect to a peer
    public synchronized boolean createSocket(Tracker track,String ip, int port) throws IOException{
        Socket sock = null;
        System.out.println("create");
        try{
            sock = new Socket(ip,port);
            byte[] handshake = handshake(track);
            System.out.println(sock.isConnected());
            in = new DataInputStream(sock.getInputStream());
            System.out.println(in);

            if(chackHandhshake(track.infoHash, input == false))
                return false;
            out = new DataOutputStream((sock.getOutputStream()));
            out.write(handshake);
            peerInfo.handshake = true;
            out.flush();
            return true;
        } catch(IOException e){
            System.out.print("Couldn't complete handshake");
            return false;
        }
    }


}
