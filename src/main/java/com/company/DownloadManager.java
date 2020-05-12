package com.company;

/**
 Download Manager overall serves to coordinate communication with Tracker and Peers. It keeps ongoing download stats
 As well as the pieces which we currently have
 (Work in progress)

 */

public class DownloadManager {
    private byte[] bitfield;


    public byte[] getBitfield() {
        return bitfield;
    }
}
