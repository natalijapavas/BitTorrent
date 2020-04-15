package com.company;
import com.company.Bencoding.BencodeDecoder;
import com.company.Bencoding.BencodeEncoder;
import com.company.Bencoding.BencodeFormatException;
import com.company.Bencoding.BencodeValue;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
/*Todo verovatno treba recovery code u slucaju da URL konekcija ne uspe */


public class TrackerTalker {
    private String infoHash;
    MetaInfoFile metaInfoFile;
    String peerId;
    //treba IP adresa

    public TrackerTalker() throws BencodeFormatException, NoSuchAlgorithmException {
        this.infoHash=generateInfoHash();
        this.peerId=generatePeerId();
    }

    public BencodeValue sendHTTPGetRequest()
    {

        BencodeValue response=null;
        try {
            String query=this.generateQuery(); //GET request parameters
            if(query==null)
                return null; //Add exceptions and recovery code maybe
            StringBuffer urlString=new StringBuffer(this.metaInfoFile.getAnnounce());
            urlString.append(query);
            URL url=new URL(urlString.toString());
            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            response=new BencodeDecoder(connection.getInputStream()).decode();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (BencodeFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
private String generatePeerId()
{
    Random randomGenerator=new Random();
    byte[] bytes=new byte[20];
    randomGenerator.nextBytes(bytes);
    return bytes.toString();
}

    //Da li bencode dict kao string? da li je utf-8, ako kod puca, onda je ovde verovatno problem
    //TODO this has to be url encoded
    private String generateInfoHash() throws NoSuchAlgorithmException, BencodeFormatException {
        MessageDigest messageDigest=MessageDigest.getInstance("SHA-1");
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        BencodeEncoder bencodeEncoder=new BencodeEncoder(this.metaInfoFile.getInfoMap(),baos);
        byte[] byteMesssage=messageDigest.digest(baos.toByteArray());
        return byteMesssage.toString(); //koji je default encoding?
    }

    private String generateQuery() throws BencodeFormatException, UnknownHostException, UnsupportedEncodingException { //proveriti da li treba da se pretvori u ascii
        StringBuffer strBuffer=new StringBuffer(metaInfoFile.getAnnounce());
        int portNum=generatePortNumber();
        if(portNum==-1)
            return null;
        strBuffer.append('?');
        strBuffer.append(URLEncoder.encode("info_hash", "UTF-8"));
        strBuffer.append("=");
        strBuffer.append(URLEncoder.encode(this.infoHash, "UTF-8"));
        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("peer_id=", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(strBuffer.append(this.peerId));
        strBuffer.append('&');
        strBuffer.append("ip");
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode(InetAddress.getLocalHost().getHostAddress(),"UTF-8"));
        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("port", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode(new Integer(portNum).toString(), "UTF-8"));
        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("downloaded", "UTF-8")); //downloaded
        strBuffer.append('=');
        //method for downloading
        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("left", "UTF-8"));
        strBuffer.append('=');
        //method left
        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("event", "UTF-8"));
        //add event method
        return strBuffer.toString();
    }

  private int generatePortNumber()
  {
      int MIN_PORT=6881;
      int MAX_PORT=6889;
      for(int i=MIN_PORT;i<=MAX_PORT; i++) {
          if(isAvailablePort(i))
              return i;
      }
      return -1;
  }


    private static boolean isAvailablePort(int port) {
        Socket s=null;
        try {
            s = new Socket("localhost", port);

            // If the code makes it this far without an exception it means
            // something is using the port and has responded.
            System.out.println("--------------Port " + port + " is not available");
            return false;
        } catch (IOException e) {
            System.out.println("--------------Port " + port + " is available");
            return true;
        } finally {
            if( s != null){
                try {
                    s.close();
                } catch (IOException e) {
                    throw new RuntimeException("You should handle this error." , e);
                }
            }
        }
    }


}
