package com.company;

import java.io.File;
//Add to the class all relevent info that GUI will be using such as progress ba, download rate, torrent info etc.
public class Client {

    //in command line we get 2 args - torrent file and output file
    public static void main(String[] args){
        if(args.length != 2){
            System.out.println("Invalid number of arguments");
            return;
        }

        File torrentFile = new File((String) args[0]);
        File outputFile = new File((String) args[1]);

        if(!torrentFile.exists()){
            System.out.println("Torrent file doesn't exist");
        }

    }

    /*
    EvenQueue.invokeLater(new Runnable(){
        public void run(){
            try{
                boolean[] checkPieces;
                if(outputFile.exists()){

                //kako ovo bez file_length??
                    if(torrentInfo.file_length % torrentInfo.piece_length != 0) {
							checkPieces = new boolean[torrentInfo.file_length % torrentInfo.piece_length + 1];

						}
						else {
							checkPieces = new boolean[torrentInfo.file_length % torrentInfo.piece_length];
						}
				    boolean fullFile = true;
				    for(int i = 0; i < manager.getCurrBitfield().length;i++){
				        if(manager.getCurrBitfield()[i] == false){
				            fullFile = false;
				        }
				    }
				    if(fullFile){
				        manager.setIsDownloading() = false;
				        manager.fullFile = true;
				    }
				    else{
				        manager.seIsDownloading() = true;
				    }
                }
                else{
                    outpuyFile.createNewFile();

                }
                manager.init();
                manager.isRunning = true;
                manager.start();

            }catch(Exception e){
                e.printStackTrace():
            }

        }

    }
    */


}
