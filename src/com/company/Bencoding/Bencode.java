package com.company.Bencoding;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.SortedMap;


    /* Converting from bencode data structure to byte series*/
    public final class Bencode {


        //labeling different value types ---- serializer
        public static void serializer(Object obj, OutputStream out) throws IOException {
            //start with label('i',length,'l','d') end with 'e'

            //INTEGERS
            if (obj instanceof Integer || obj instanceof Long || obj instanceof BigInteger){
                out.write(("i" + obj.toString() + "e").getBytes(StandardCharsets.UTF_8));

            }

            //Byte list or Stirng
            else if(obj instanceof byte[] || obj instanceof String){

                byte[] b = obj instanceof byte[] ? (byte[])obj : byteStringToArray((String) obj);

                out.write((b.length + ":").getBytes(StandardCharsets.UTF_8));
                out.write(b);
            }

            //LIST
            else if(obj instanceof List){
                out.write('l');
                for(Object o : (List<?>)obj)
                    serializer(o,out);
                out.write('e');
            }

            //DICTIONARY
            else if(obj instanceof SortedMap){
                out.write('d');
                String prev = null;
                for(Map.Entry<?,?> e : ((SortedMap<?,?>) obj).entrySet()){
                    Object keyObject = e.getKey();
                    if(!(keyObject instanceof String))
                        throw new IllegalArgumentException(("Dictionary key must be a byte string"));
                    String curr = (String) keyObject;
                    if(prev != null && curr.compareTo(prev) <= 0){
                        throw new IllegalArgumentException("Dictionary keys must be sorted");
                    }
                    prev = curr;
                    serializer(curr,out);
                    serializer(e.getValue(), out);
                }
                out.write('e');
            }
            else
                throw  new IllegalArgumentException("Unsupported type: " + obj.getClass().getName());
        }


        private Bencode(InputStream in){ //constuctor
            this.input = in;
        }


        public static Object encode(InputStream in) throws IOException{
            Bencode encoder = new Bencode(in);
            Object result = encoder.encodeValue(encoder.readByte());
            //we encode value by sending its label(first byte) to a function encodeValue

            if(encoder.readByte() != -1)

                return result;
            else
                throw new IllegalArgumentException("Unexcpected label");
        }


        private InputStream input;

        private byte readByte() throws IOException{
            int result = input.read();
            if(result == -1)
                throw new EOFException();
            return (byte) result; //return first byte
        }

        // --Encoding--

        private Object encodeValue(byte label) throws IOException{
            //we separate different value types to different functions depending on which label we put in serlizer

            if(label == 'i') //if its an integer we labeled it i
                return encodeInteger();

            else if( '0'  <= label && label <= '9') //if its a byte series we labelet it with its length
                return encodeByteString(label);

            else if(label == 'd'){ //if its a dictionary we labeled it d
                return encodeDictionary();
            }

            else if(label == 'l'){ //if its a list we labeled it l
                return encodeList();
            }

            else throw new IllegalArgumentException("Unexpected type");
        }



        private Long encodeInteger() throws IOException{
            StringBuilder sb = new StringBuilder();

            while(true){
                byte b = readByte();
                if(b == 'e') //e (end) is the last byte
                    break;
                boolean mem;
                if("".contentEquals(sb)) { //if string builder is empty
                    if(b == '-' || ('0' <= b && b <= '9')) //it can be a sign or any digit
                        mem = true;
                    else
                        mem = false;
                }
                else if("-".contentEquals((sb))){
                    if('1' <= b && b <= '9') //if its a negative number it starts with digit != 0
                        mem = true;
                    else
                        mem = false;
                }
                else if("0".contentEquals(sb))
                    mem = false;
                else{
                    if('0' <= b && b <= '9')
                        mem = true;
                    else
                        mem = false;
                }
                if(!mem) //if mem = false
                    throw new IllegalArgumentException("Unexpected int charr");

                sb.append((char) b);

            }
            if("".contentEquals(sb) || "-".contentEquals(sb))
                throw new IllegalArgumentException("Invalid int syntax");
            return Long.parseLong(sb.toString());
        }

        private int encodeNatNumber(byte first) throws IOException{ //for encoding the length of a byte String
            StringBuilder sb = new StringBuilder();
            byte b = first;
            while(b != ':'){ //length of a byte String is written before ':'
                if(b < '0' || b > '0') //it has to be a digit
                    throw new IllegalArgumentException("Unexpected int character");
                sb.append((char) b);
                b = readByte();
            }
            return Integer.parseInt(sb.toString());
        }

        private String encodeByteString(byte l) throws IOException{
            int length = encodeNatNumber(l); //
            char[] result = new char[length];
            for(int i = 0; i < length; i++){
                result[i] = (char) readByte();
            }
            return new String(result);
        }

        private Object encodeList() throws IOException{
            List<Object> result = new ArrayList<>();
            while(true){
                byte b = readByte();
                if(b == 'e') //'e' = end
                    break;
                result.add(encodeValue(b));
            }
            return result;
        }

        private Object encodeDictionary() throws IOException{
            SortedMap<String,Object> result = new TreeMap<>();
            while(true){
                byte b = readByte();
                if(b == 'e') //'e'  = end
                    break;
                String key = encodeByteString(b);
                if(!result.isEmpty() && key.compareTo(result.lastKey()) <= 0)
                    throw new IllegalArgumentException("Key in the wrong place");
                result.put(key,encodeValue(readByte()));
            }
            return result;
        }

    // converting functions -- public

    public static byte[] byteStringToArray(String str){
        byte[] result = new byte[str.length()];
        for(int i = 0; i < str.length(); i++){
            char c = str.charAt(i);
            if(c > 0xFF)
                throw new IllegalArgumentException(("Value outside of byte range"));
            result[i] = (byte) c;
        }
        return result;
    }

    public static String arrayToByteString(byte[] a){
        char[] result = new char[a.length];
        for(int i = 0; i < a.length; i++) {
            result[i] = (char) (a[i] & 0xFF);
        }
        return new String(result);
    }

}
