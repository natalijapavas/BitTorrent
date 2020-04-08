package com.company.Bencoding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class serves to perform encoding Java objects in Bencoded format.
 * Formats which can be Bencoded are:
 * -String
 * -Bytes
 * -Numbers (i.e. int,long,float,double...)
 * -Lists
 * -Maps
 * Other types are not supported by the specification.
 */
public class BencodeEncoder {
    private Object encodedObject;
    private OutputStream out;

    public BencodeEncoder(Object encodedObject, OutputStream out) {
        this.encodedObject = encodedObject;
        this.out = out;
    }

    public void encode() throws IOException, IllegalArgumentException {
        if (this.encodedObject instanceof BencodeValue) {
            this.encodedObject = ((BencodeValue) encodedObject).getValue();
        }

        if (this.encodedObject instanceof String) {
            this.encodeString();
        }
        else if (this.encodedObject instanceof byte[]) {
            encodeBytes();
        }
        else if (this.encodedObject instanceof Number) {
            encodeNumber();
        }
        else if (this.encodedObject instanceof List) {
            encodeList();
        }
        else if (this.encodedObject instanceof Map) {
            encodeMap();
        }
        else {
            throw new IllegalArgumentException("Cannot bencode: " +
                    this.encodedObject.getClass());
        }
    }

    private static void encodeByteArray(byte[] bytes, OutputStream out) throws IOException {

        String l = Integer.toString(bytes.length);
        out.write(l.getBytes(StandardCharsets.UTF_8));
        out.write(':');
        out.write(bytes);
    }

    private void encodeBytes() throws IOException
    {
        encodeByteArray((byte[])this.encodedObject,this.out);
    }

    private void encodeString() throws IOException {
        byte[] bytes = ((String)this.encodedObject).getBytes(StandardCharsets.UTF_8);
        encodeByteArray(bytes,this.out);
    }

    private void encodeNumber() throws IOException {
        this.out.write('i');
        String s = ((Number)this.encodedObject).toString();
        this.out.write(s.getBytes("UTF-8"));
        this.out.write('e');
    }

    private void encodeList() throws IOException
    {
        this.out.write('l');
        List<BencodeValue> list=(List<BencodeValue>)this.encodedObject;
        for (BencodeValue value : list) {
            new BencodeEncoder(value, this.out).encode();
        }
        this.out.write('e');
    }

    private void encodeMap() throws IOException {
        this.out.write('d');

        Map<String,Object> map=(Map<String, Object>)this.encodedObject;
        Set<String> s = map.keySet();
        List<String> l = new ArrayList<String>(s);
        Collections.sort(l);

        for (String key : l) {
            Object value = map.get(key);
            new BencodeEncoder(key,this.out).encode();
            new BencodeEncoder(value,this.out).encode();
        }

        this.out.write('e');
    }

    public void setEncodedObject(Object encodedObject)
    {
        this.encodedObject=encodedObject;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

//Currently serves as test function to see "nice" structure of file
    public String printBencodedValue(ByteArrayOutputStream baos) throws IOException {
        this.setOut(baos);
        this.encode();
        return new String( baos.toByteArray(), StandardCharsets.UTF_8);
    }
    /*public static ByteBuffer encode(Map<String, BencodeValue> m)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BencodeEncoder.encode(m, baos);
        baos.close();
        return ByteBuffer.wrap(baos.toByteArray());
    } */
}
