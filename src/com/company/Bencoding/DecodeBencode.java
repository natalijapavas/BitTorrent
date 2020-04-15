package com.company.Bencoding;

import java.io.*;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class DecodeBencode {
    //parses files data as bencode, then prints the data structure to standard output
    public static void main(String[] args) throws IOException{
        String path="/home/korisnik/Desktop/paper_reading";
        if(!submain(path)){
            System.err.println("Usage: java DecodeBencode Input.torrent");
            System.exit(1);
        }
    }

    private static boolean submain(String path) throws IOException{
        File file = new File(path);
        if(!file.isFile())
            return false;
        Object obj;
        try(InputStream in = new BufferedInputStream((new FileInputStream(file)))){
            obj = Bencode.encode(in);
        }
        printBencodeValue(obj,0);
        return true;
    }

    //recursively prints value/stucture to standard output
    private static void printBencodeValue(Object obj, int d){
        if(obj instanceof  Integer || obj instanceof  Long || obj instanceof BigInteger){
            System.out.println("Int: " + obj);
        }
        else if(obj instanceof byte[] || obj instanceof String){
            byte[] b = obj instanceof byte[] ? (byte[])obj : Bencode.byteStringToArray((String)obj);
            System.out.printf("ByteString (%d) " , b.length);
            try{
                String s = decodeUtf8(b);
                System.out.println("(text): " + s);
            } catch(IllegalArgumentException e){
                System.out.print("(binary): ");
                for(int i = 0; i < b.length; i++){
                    System.out.printf("%02X", b[i]);
                    if(i + 1 < b.length){
                        System.out.print("");
                        if(i == 30){
                            System.out.print("...");
                            break;
                        }
                    }
                }
                System.out.println();
            }
        }
        else if(obj instanceof List){
            System.out.println("List: ");
            List<?> list = (List<?>) obj;
            for(int i = 0 ; i < list.size(); i++){
                printIndent(d + 1);
                System.out.print(i + " = ");
                printBencodeValue(list.get(i) , d + 1 );
            }
        }

        else if(obj instanceof SortedMap){
            System.out.println("Dictionary: ");
            SortedMap<?,?> map = (SortedMap<?,?>) obj;
            for(Map.Entry<?,?> e :  map.entrySet()){
                printIndent(d + 1);
                System.out.print(e.getKey() + " = ");
                printBencodeValue(e.getValue(), d + 1);
            }
        }
        else
            throw new IllegalArgumentException("Unsupported type: " + obj.getClass().getName());
    }

    //DECODES array of bytes as a UTF-8 string
    private static String decodeUtf8(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < bytes.length; i++){
            int l = bytes[i] & 0xFF;
            if(l < 0b1000_0000){ //single byte ascii
                sb.append((char) l);
                continue;
            }
            int ones = Integer.numberOfLeadingZeros(l ^ 0xFF) - 24;
            if(ones < 2 || ones > 4)
                throw  new IllegalArgumentException("Invalid leading bute");
            if(bytes.length - i < ones)
                throw new IllegalArgumentException("Missing continuation Byte");
            int c = l & (0b0111_1111 >>> ones); //Accumulator
            for(int j = 1; j < ones; j++, i++){
                int b = bytes[i+1] & 0xFF;
                if((b & 0b1100_0000) != 0b1000_0000)
                    throw new IllegalArgumentException(("Invalid continuation byte"));
                c = (c << 6) | (b & 0b0011_1111);
            }
            if(c >>> Math.max(ones *5 - 4, 7) == 0)
                throw new IllegalArgumentException(("Over-long UTF-8 sequence"));
            else if(0xD800 <= c && c < 0xE000)
                throw new IllegalArgumentException("UTF-16 surrogate encountered");
            else if( c < 0x10000)
                sb.append((char) c);
            else if( c < 0x110000){
                sb.append((char)(0xD7C0 + (c >>> 10)));
                sb.append((char)(0xDC00 | (c & 0x3FF)));
            }
            else
                throw new IllegalArgumentException("Code point outside of UTF-16 range");

        }
        return sb.toString();
    }

    //prints indentations 
    private static void printIndent(int d){
        if(d < 0)
            throw new IllegalArgumentException("Negative depth");
        for(int i = 0; i < d; i++)
            System.out.print("   ");
    }
}
