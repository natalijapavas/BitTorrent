package com.company;
import com.company.Bencoding.BencodeDecoder;
import com.company.Bencoding.BencodeFormatException;
import com.company.Bencoding.BencodeValue;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/*Todo verovatno treba recovery code u slucaju da URL konekcija ne uspe */

public class Tracker {
    private MetaInfoFile metaInfoFile;
    private String peerId;

    public Tracker(MetaInfoFile metaInfoFile) {
        this.metaInfoFile=metaInfoFile;
        this.peerId=generatePeerId();
    }

    public String getInfoHash()
    {
        return new String(this.metaInfoFile.getInfoHash(),StandardCharsets.UTF_8);
    }


    //URL should look like: https://torrent.ubuntu.com/announce?info_hash=%9A%813%3C%1B%16%E4%A8%3C%10%F3%05%2C%15%90%AA%DF%5E.%20&peer_id=ABCDEFGHIJKLMNOPQRST&port=6881&uploaded=0&downloaded=0&left=727955456&event=started&numwant=100&no_peer_id=1&compact=1
    public BencodeValue sendHTTPAnnounceRequest()
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

    public String getPeerId() {
        return peerId;
    }


    private String generatePeerId() //mozda ne treba kao hex?
{
    Random randomGenerator=new Random(System.currentTimeMillis());
    byte[] bytes=new byte[20];
    randomGenerator.nextBytes(bytes);
    System.out.println("Velicina id- a je: "+bytes.length);
    return new String(bytes,StandardCharsets.UTF_8);
}


    //Subject to change, following info hash problem resolution
    private String generateQuery() throws BencodeFormatException, UnknownHostException, UnsupportedEncodingException {
        StringBuffer strBuffer=new StringBuffer(metaInfoFile.getAnnounce());
        int portNum=generatePortNumber();
        if(portNum==-1)
            return null;
        strBuffer.append('?');

        strBuffer.append(URLEncoder.encode("info_hash", "UTF-8"));
        strBuffer.append("=");
        String hash=new String(this.metaInfoFile.getInfoHash(), StandardCharsets.UTF_8);
        strBuffer.append(URLEncoder.encode(hash, "UTF-8"));

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("peer_id=", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode(this.peerId,"UTF-8"));

        strBuffer.append('&');
        strBuffer.append("ip");
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode(InetAddress.getLocalHost().getHostAddress(),"UTF-8"));

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("port", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode(new Integer(portNum).toString(), "UTF-8"));

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("downloaded", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode("0", "UTF-8")); //downloaded

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("left", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode(Integer.toString(this.metaInfoFile.getFileLength()), "UTF-8")); //left

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("event", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode("started", "UTF-8"));

        return strBuffer.toString();
    }
    /** Returns one of the standard port numbers for bittorrent protocol, or -1 if not available
     *
     * @return
     */
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
            System.out.println("Port " + port + " is not available");
            return false;
        } catch (IOException e) {
            System.out.println("Port " + port + " is available");
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
