package com.company;
import com.company.Bencoding.BencodeDecoder;
import com.company.Bencoding.BencodeFormatException;
import com.company.Bencoding.BencodeValue;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/* Todo verovatno treba recovery code u slucaju da URL konekcija ne uspe
 */

public class Tracker implements Runnable{
    private MetaInfoFile metaInfoFile;
    private byte[] peerId;
    private int port;
    private byte[] trackerID;
    private int downloaded;
    private int uploaded;
    private int announceInterval;
    private ArrayList<Peer> peerList;
    //This probably also needs to be added
    //private boolean isStarted;
    //private boolean isStopped;
    //private boolean isDownloading;


    public Tracker(MetaInfoFile metaInfoFile) throws IOException {
        this.metaInfoFile=metaInfoFile;
        this.peerId=generatePeerId();
        this.port=generatePortNumber();
        this.announceInterval=0;
        this.downloaded=0;
        this.uploaded=0;
        this.trackerID=null;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(announceInterval);
                BencodeValue httpResponse = this.sendHTTPAnnounceRequest();
                this.parseResponse(httpResponse);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    //Example URL: https://torrent.ubuntu.com/announce?info_hash=%9A%813%3C%1B%16%E4%A8%3C%10%F3%05%2C%15%90%AA%DF%5E.%20&peer_id=ABCDEFGHIJKLMNOPQRST&port=6881&uploaded=0&downloaded=0&left=727955456&event=started&numwant=100&no_peer_id=1&compact=1
    public BencodeValue sendHTTPAnnounceRequest()
    {
        BencodeValue response=null;
        try {
            String urlString=this.generateAddress(); //GET request parameters
            if(urlString==null)
                return null; //Add exceptions and recovery code maybe

            System.out.println("Url string to which request is sent:"+ urlString);
            URL url=new URL(urlString);
            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode=connection.getResponseCode();
            System.out.println("Response code is: "+responseCode);
            response=new BencodeDecoder(connection.getInputStream()).decode();
            if (response.getMap().keySet().contains((String) "failure response"))
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


    private byte[] generatePeerId() throws IOException
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
    }


    //Subject to change, following info hash problem resolution
    private String generateAddress() throws BencodeFormatException, UnknownHostException, UnsupportedEncodingException {
        StringBuffer strBuffer=new StringBuffer(metaInfoFile.getAnnounce());
        //int portNum=generatePortNumber(); //ovde treba recovery code
        if(this.port==-1)
            return null;
        strBuffer.append('?');

        strBuffer.append(URLEncoder.encode("info_hash", "UTF-8"));
        strBuffer.append("=");
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
        strBuffer.append(URLEncoder.encode(new Integer(this.port).toString(), "UTF-8"));

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("uploaded", "UTF-8"));
        strBuffer.append('=');
        //strBuffer.append(URLEncoder.encode("0", "UTF-8")); //uploaded
        strBuffer.append(URLEncoder.encode(new Integer(this.uploaded).toString(), "UTF-8")); //uploaded

        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("downloaded", "UTF-8"));
        strBuffer.append('=');
        //strBuffer.append(URLEncoder.encode("0", "UTF-8")); //downloaded
        strBuffer.append(URLEncoder.encode(new Integer(this.downloaded).toString(), "UTF-8")); //downloaded


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

        strBuffer=appendTrackerID(strBuffer);
        return strBuffer.toString();
    }


    public void parseResponse(BencodeValue bencodeHttpResponse)
    {
        try {
            this.peerList=generatePeerList(bencodeHttpResponse);
            //setTrackerID(bencodeHttpResponse)
            Map<String,BencodeValue> bencodeMap= bencodeHttpResponse.getMap();
            this.announceInterval=bencodeMap.get("interval").getInt();
            this.announceInterval*=1000;
        }
        catch (BencodeFormatException e) {
            e.printStackTrace();
        }


    }



    public byte[] getPeerId() {
        return peerId;
    }

    private boolean isOurTorrentClient(String peerID) { return peerID.contains("-ND-"); }


    private ArrayList<Peer> generatePeerList(BencodeValue bencodeHttpResponse)
    {
        ArrayList<Peer> peerList=new ArrayList<>();
            try {
                Map<String,BencodeValue> bencodeMap= bencodeHttpResponse.getMap();
                List<BencodeValue> bencodedPeerList=bencodeMap.get("peers").getList();
                for(BencodeValue value:bencodedPeerList)
                {

                    Map<String, BencodeValue> peer=value.getMap();
                    String peerId=peer.get("peer id").getString();
                    if(isOurTorrentClient(peerId))
                        continue;
                    Integer portNumber=peer.get("port").getInt();
                    String ip=peer.get("ip").getString();
                    PeerInfo peerInfo=new PeerInfo(peerId,portNumber,ip,this);
                    peerList.add(new Peer(peerInfo));
                }
                return peerList;
            }
            catch (BencodeFormatException e) {
                e.printStackTrace();
            }
        return null;
    }

    public String getInfoHash()
    {
        return new String(this.metaInfoFile.getInfoHash(),StandardCharsets.UTF_8);
    }

    public byte[] getInfoHashBytes(){return this.metaInfoFile.getInfoHash();}

    public int getPort() {
        return port;
    }

    public MetaInfoFile getMetaInfoFile() {
        return metaInfoFile;
    }

    public void incrementDownloaded(int downloaded)
    {
        this.downloaded+=downloaded;
    }

    public void incrementUploaded(int uploaded)
    {
        this.uploaded+=uploaded;
    }

    public int getDownloaded() {
        return downloaded;
    }

    public int getUploaded() {
        return uploaded;
    }

    private void setTrackerID(BencodeValue bencodeHttpResponse)
    {
        if(trackerID.length!=0) //if trackerId is alrady set, do not change it
            return;
        String trackerIdKey="trackerid";
        try {
            //setTrackerID(bencodeHttpResponse)
            Map<String,BencodeValue> bencodeMap= bencodeHttpResponse.getMap();
            if(bencodeMap.keySet().contains(trackerIdKey))
            {
                this.trackerID=bencodeMap.get(trackerIdKey).getBytes();
            }
        }
        catch (BencodeFormatException e) {
            e.printStackTrace();
        }
    }

    private StringBuffer appendTrackerID(StringBuffer strBuffer) throws UnsupportedEncodingException {
        if(this.trackerID.length==0)
            return  strBuffer;
        strBuffer.append('&');
        strBuffer.append(URLEncoder.encode("trackerid", "UTF-8"));
        strBuffer.append('=');
        strBuffer.append(urlEncode(this.trackerID));
        return strBuffer;
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
