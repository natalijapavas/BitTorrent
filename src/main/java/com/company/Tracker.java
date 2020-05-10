package com.company;
import com.company.Bencoding.BencodeDecoder;
import com.company.Bencoding.BencodeFormatException;
import com.company.Bencoding.BencodeValue;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/*Todo verovatno treba recovery code u slucaju da URL konekcija ne uspe */
/*Todo: Announce list extension
https://web.archive.org/web/20200225211151/http://www.bittorrent.org/beps/bep_0012.html
 */

public class Tracker {
    private MetaInfoFile metaInfoFile;
    private byte[] peerId;

    public Tracker(MetaInfoFile metaInfoFile) throws IOException {
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
            String urlString=this.generateAddress(); //GET request parameters
            if(urlString==null)
                return null; //Add exceptions and recovery code maybe

            System.out.println("Url string to which request is sent:"+ urlString);
            URL url=new URL(urlString.toString());
            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode=connection.getResponseCode();
            System.out.println("Response code is: "+responseCode);
            response=new BencodeDecoder(connection.getInputStream()).decode();
            if (response.getMap().keySet().contains((String) "failure reasone"))
            {
                System.out.println("Error occured");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (BencodeFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public byte[] getPeerId() {
        return peerId;
    }


    private byte[] generatePeerId() throws IOException //mozda ne treba kao hex?
    {
        final int ID_SIZE=20;
        final int RANDOM_ARRAY_SIZE=16;
    Random randomGenerator=new Random(System.currentTimeMillis());
    ByteArrayOutputStream byteBuffer=new ByteArrayOutputStream(ID_SIZE);
    byteBuffer.write('-');
    byteBuffer.write('N');
    byteBuffer.write('D');
    byteBuffer.write('-');
    byte[] randomBytes=new byte[RANDOM_ARRAY_SIZE];
    randomGenerator.nextBytes(randomBytes);
    byteBuffer.write(randomBytes);
    return byteBuffer.toByteArray();
    //return bytes;
    }


    //Subject to change, following info hash problem resolution
    private String generateAddress() throws BencodeFormatException, UnknownHostException, UnsupportedEncodingException {
        StringBuffer strBuffer=new StringBuffer(metaInfoFile.getAnnounce());
        int portNum=generatePortNumber();
        if(portNum==-1)
            return null;
        strBuffer.append('?');

        strBuffer.append(URLEncoder.encode("info_hash", "UTF-8"));
        strBuffer.append("=");
        //String hash=new String(this.metaInfoFile.getInfoHash(), StandardCharsets.UTF_8);
        //strBuffer.append(URLEncoder.encode(hash, "UTF-8"));
        strBuffer.append(urlEncode(this.metaInfoFile.getInfoHash()));

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("peer_id", "UTF-8"));
        strBuffer.append('=');
        System.out.println("Velicina peer id-a u generate Address je: "+this.peerId.length);
        System.out.println(this.peerId);
        strBuffer.append(urlEncode(this.peerId));

        strBuffer.append('&');
        strBuffer.append("ip");
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode(InetAddress.getLocalHost().getHostAddress(),"UTF-8"));

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("port", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode(new Integer(portNum).toString(), "UTF-8"));

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("uploaded", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode("0", "UTF-8")); //uploaded

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("downloaded", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode("0", "UTF-8")); //downloaded

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("left", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode(Integer.toString(this.metaInfoFile.getFileLength()), "UTF-8")); //left to download

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("compact", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode("0", "UTF-8"));

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("no_peer_id", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode("0", "UTF-8"));

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("event", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(URLEncoder.encode("started", "UTF-8"));
        return strBuffer.toString();
    }

    public MetaInfoFile getMetaInfoFile() {
        return metaInfoFile;
    }

    private static String urlEncode(byte[] binary) {
        if (binary == null) {
            return null;
        }
        try { // we use a base encoding that accepts all byte values
            return URLEncoder.encode(new String(binary, "ISO8859_1"), "ISO8859_1");
        } catch (UnsupportedEncodingException e) {
            // this cannot happen with ISO8859_1 = Latin1
            e.printStackTrace();
            return null;

        }
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
