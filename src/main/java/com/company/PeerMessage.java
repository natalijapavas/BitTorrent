package com.company;

public class PeerMessage {
    private Message message;
    private Peer peer;

    PeerMessage(Peer peer, Message message){
        this.message = message;
        this.peer = peer;
    }

    public Message getMessage(){
        return this.message;
    }
    public Peer getPeer(){
        return this.peer;
    }
}
