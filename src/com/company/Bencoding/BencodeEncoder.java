package com.company.Bencoding;
import java.lang.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    private OutputStream outputStream;

    public BencodeEncoder(OutputStream out) {
        this.outputStream=out;

    }

    public void encode(Object encodedObject) throws IOException, IllegalArgumentException {
        if (encodedObject instanceof BencodeValue) {
            encodeBencodeValue((BencodeValue)encodedObject);
        }

        else if (encodedObject instanceof String) {
            encodeString((String)encodedObject);
        }
        else if (encodedObject instanceof byte[]) {
            encodeBytes((byte[])encodedObject);
        }
        else if (encodedObject instanceof Number) {
            encodeNumber((Number)encodedObject);
        }
        else if (encodedObject instanceof List) {
            encodeList((List<BencodeValue>)encodedObject);
        }
        else if (encodedObject instanceof Map) {
            encodeMap((LinkedHashMap<String,BencodeValue>) encodedObject);
        }
        else {
            throw new IllegalArgumentException("Cannot bencode: " +
                    encodedObject.getClass());
        }
    }

    public void encodeBencodeValue(BencodeValue bencodeValue) throws IOException {
        if(bencodeValue.getValueType()=='b')
                encodeBytes((byte[])(bencodeValue.getValue()));
        else if(bencodeValue.getValueType()=='i')
                encodeNumber((Number)(bencodeValue.getValue()));
        else if(bencodeValue.getValueType()=='l')
                encodeList((List<BencodeValue>)(bencodeValue.getValue()));
        else if(bencodeValue.getValueType()=='d')
                encodeMap((LinkedHashMap<String,BencodeValue>)(bencodeValue.getValue()));
        else
                throw new BencodeFormatException("Unknown Bencode format");
        }



    private static void encodeByteArray(byte[] bytes, OutputStream out) throws IOException {
        String l = Integer.toString(bytes.length);
        out.write(l.getBytes(StandardCharsets.UTF_8));
        out.write(':');
        out.write(bytes);
    }

    private void encodeBytes(byte[] encodedObject) throws IOException
    {
        encodeByteArray(encodedObject,this.outputStream);
    }

    private void encodeString(String encodedObject) throws IOException {
        byte[] bytes =encodedObject.getBytes(StandardCharsets.UTF_8);
        encodeByteArray(bytes,this.outputStream);
    }

    private void encodeNumber(Number encodedObject) throws IOException {
        this.outputStream.write('i');
        String s = encodedObject.toString();
        this.outputStream.write(s.getBytes("UTF-8"));
        this.outputStream.write('e');
    }

    private void encodeList(List<BencodeValue> list) throws IOException
    {
        this.outputStream.write('l');
        for (BencodeValue value : list) {
            new BencodeEncoder(this.outputStream).encode(value);
        }
        this.outputStream.write('e');
    }

    private void encodeMap(LinkedHashMap<String,BencodeValue> map) throws IOException {

        this.outputStream.write('d');
         //mozda staviti da bude LinkedHashMap?
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            BencodeValue value = map.get(key);
            new BencodeEncoder(this.outputStream).encode(value);
            new BencodeEncoder(this.outputStream).encode(value);
        }
        this.outputStream.write('e');
    }



    public void setOutputStream(OutputStream out) {
        this.outputStream = out;
    }

}
