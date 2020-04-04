package BCoding;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.SortedMap;


/* Converting from bencode data structure to byte series*/
public final class Bencode {
    //labeling different value types
    public static void serailizer(Object obj, OutputStream out) throws IOException {
        //start with label('i',length,'l','d') end with 'e'

        //INTEGERS
        if (obj instanceof Integer || obj instanceof Long || obj instanceof BigInteger){
            out.write(("i" + obj.toString() + "e").getBytes(StandardCharsets.UTF_8));

        }

        //Byte list or Stirng
        else if(obj instanceof byte[] || obj instanceof String){

            byte[] b = obj instanceof byte[] ? (byte[])obj) : bStringToArray((String) obj);

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
            String key = null;
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

    private Object encodeValue(byte label) throws IOException{
        //we separate different value types to different functions depending on which label we put in serlizer

        if(label == 'i') //if its an integer we labeled it i
                return encodeInteger();

        else if( '0'  <= label && label <= '9') //if its a byte series we labelet it with its length
            return ecodeByteString(label);

        else if(label == 'd'){ //if its a dictionary we labeled it d
            return encodeDictionary();
        }

        else if(label == 'l'){ //if its a list we labeled it l
            return encodeList();
        }

        else throw new IllegalArgumentException("Unexpected type");
    }

    private byte readByte() throws IOException{
        int result = input.read();
        if(result == -1)
            throw new EOFException();
        return (byte) result; //return first byte
    }

    private Long encodeInteger() throws IOException{
        StringBuilder sb = new StringBuilder();

        while(1){
            byte b = readByte();
            if(b == 'e') //e (end) is the last byte
                break;
        }
    }

}
