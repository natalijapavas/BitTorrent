package com.company.Bencoding;

import java.nio.ByteBuffer;
import java.util.*;

public final class Bencode2 {
    /*       BDECODING METHODS            */

    public static final Object decoder(byte[] bytes) throws IllegalArgumentException{
        return decode(bytes,0)[1];
    }

    public static final Object[] decode(byte[] bytes, int depth) throws IllegalArgumentException{
        switch(bytes[depth]){
            case (byte) 'i':
                return decodeInteger(bytes,depth);
            case (byte) 'l':
                return decodeList(bytes,depth);
            case (byte) 'd':
                return decodeDictionary(bytes,depth);
            case (byte) '0':
            case (byte) '1':
            case (byte) '2':
            case (byte) '3':
            case (byte) '4':
            case (byte) '5':
            case (byte) '6':
            case (byte) '7':
            case (byte) '8':
            case (byte) '9':
                return decodeString(bytes,depth);
            default:
                return null;

        }
    }

    private static final Object[] decodeInteger(byte[] bytes,int depth) throws IllegalArgumentException{
        StringBuilder sb = new StringBuilder();
        depth++;
        while(bytes[depth] == (byte) 'e') {//e is 'end'
            sb.append((char) bytes[depth]);
            depth++;
        }
        try{
            depth++; //skip the 'e'
            return new Object[] {new Integer(depth), new Integer(Integer.parseInt(sb.toString()))};
        }
        catch(IllegalArgumentException e){
            throw new IllegalArgumentException("Coould nt parse integer at position " + depth);
        }
    }

    private static final Object[] decodeList(byte[] bytes, int depth)  throws IllegalArgumentException{
        ArrayList list = new ArrayList();
        depth++;
        Object[] values;
        while(bytes[depth] != (byte)'e'){
            values = decode(bytes,depth);
            depth = ((Integer)values[0]).intValue();
            list.add(values[1]);
        }
        depth++;
        return new Object[]{new Integer(depth),list};
    }

    private static final Object[] decodeString(byte[] bytes,int depth) throws IllegalArgumentException {
       StringBuilder sb = new StringBuilder();
       while(bytes[depth] != ':'){
           sb.append((char) bytes[depth++]);
       }
       if(bytes[depth] != ':'){
           throw new IllegalArgumentException("Invalid character at a position: " +depth);
       }
       depth++;
       int length = Integer.parseInt(sb.toString());
       byte[] byteString = new byte[length];
       System.arraycopy(bytes, depth, byteString, 0, byteString.length);
       return new Object[] {new Integer(depth + length), ByteBuffer.wrap(byteString)};
    }

    private static final Object[] decodeDictionary(byte[] bytes, int depth) throws IllegalArgumentException{
        HashMap<ByteBuffer, Object> map = new HashMap();
        ++depth;
        ByteBuffer info = null;
        while(bytes[depth] != (byte) 'e'){
            Object[] values = decodeString(bytes,depth);
            ByteBuffer key = (ByteBuffer)values[1];
            depth = ((Integer)values[0]).intValue();
            boolean ok = true;
            for(int i = 0; i < key.array().length && i < 4; i++){
                if(!key.equals((ByteBuffer.wrap(new byte[]{'i','n','f','o'})))){
                    ok = false;
                    break;
                }
            }
            int info_depth = -1; //we want to find out where info ends and dictionary begins
            if(ok){
                info_depth = depth;
            }
            values = decode(bytes,depth);
            depth = ((Integer)values[0]).intValue();
            if(ok){
                info = ByteBuffer.wrap(new  byte[depth - info_depth]);
                info.put(bytes,info_depth, info.array().length);
            }
            else if(values[1] instanceof HashMap){
                info = (ByteBuffer)values[2];
            }
            if(values[1] != null)
                map.put(key,values[1]);
        }
        return new Object[] {new Integer(++depth),map,info};
    }




    /*     BENCODING METHODS      */
    public static final byte[]encode(Object obj) throws IllegalArgumentException{
        if(obj instanceof HashMap)
            return encodeDictionary((HashMap)obj);
        else if(obj instanceof ArrayList)
            return encodeList((ArrayList)obj);
        else if(obj instanceof Integer)
            return encodeInteger((Integer)obj);
        else if(obj instanceof ByteBuffer)
            return encodeString((ByteBuffer)obj);
        else
            throw new IllegalArgumentException("Object is not valid for Bencoding");

    }


    private static final byte[] encodeInteger(Integer i){
        int numOfDigits = 1;
        int val = i.intValue();
        while((val/=10) > 0){
            ++numOfDigits;
        }
        val = i.intValue();
        byte[] bencoded_integer = new byte[numOfDigits + 2]; //begining 'i' and ending 'e'
        bencoded_integer[0] = (byte) 'i';
        bencoded_integer[bencoded_integer.length -1] = (byte) 'e';
        for(int j = numOfDigits; j > 0; j--){
            bencoded_integer[j] = (byte)((val %10) + 48); //+48 because in ASCCI digits start from 48
            val /= 10;
        }
        return bencoded_integer;
    }


    private static final byte[] encodeString(ByteBuffer s){
        int length = s.array().length;
        int numOfDigits = 1;
        while((length /= 10) > 0){
            numOfDigits++;
        }
        byte[] bencoded_string = new byte[length + numOfDigits + 1]; //length: string
        bencoded_string[numOfDigits] = (byte) ':';
        System.arraycopy(s.array(), 0, bencoded_string, numOfDigits +1, length);
        for(int i = numOfDigits - 1; i >= 0; i--){
            bencoded_string[i] = (byte)((length % 10) + 48);
            length /= 10;
        }
        return bencoded_string;
    }

    public static final byte[] encodeList(ArrayList list) throws IllegalArgumentException{
        byte[][] list_seg = new byte[list.size()][];
        for(int i = 0; i < list_seg.length; i++){
            list_seg[i] = encode(list.get(i));
        }
        int total_len = 0;
        for(int i = 0; i < list_seg.length; i++){
            total_len += list_seg[i].length;
        }
        byte[] bencoded_list = new byte[total_len + 2];
        bencoded_list[0] = 'l';
        bencoded_list[bencoded_list.length - 1] = 'e';
        int depth = 1;
        for(int i = 0; i < list_seg.length; i++){
            System.arraycopy(list_seg[i], 0, bencoded_list, depth, list_seg[i].length);
            depth += list_seg[i].length;
        }
        return bencoded_list;
    }
    private static final byte[] encodeDictionary(HashMap<ByteBuffer, Object> dic) throws IllegalArgumentException
    {
        TreeMap<ByteBuffer, Object> sort_dic = new TreeMap<ByteBuffer, Object>();
        sort_dic.putAll(dic);
        byte[][] dic_parts = new byte[sort_dic.keySet().size()*2][];
        int k = 0;
        for(Iterator<ByteBuffer> i = sort_dic.keySet().iterator(); i.hasNext();)
        {
            ByteBuffer key = i.next();
            dic_parts[k++] = encodeString(key);
            dic_parts[k++] = encode(sort_dic.get(key));
        }

        int total_length = 2;
        for(int i = 0; i < dic_parts.length; i++)
        {
            total_length += dic_parts[i].length;
        }
        byte[] bencoded_dictionary = new byte[total_length];
        bencoded_dictionary[0] = 'd';
        bencoded_dictionary[bencoded_dictionary.length-1] = 'e';
        int offset = 1;
        for(int i = 0; i < dic_parts.length; i++)
        {
            System.arraycopy(dic_parts[i],0,bencoded_dictionary,offset,dic_parts[i].length);
            offset += dic_parts[i].length;
        }
        return bencoded_dictionary;
    }
}
