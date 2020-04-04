package BCoding;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.SortedMap;


/* Converting from bencode data structure to byte series*/
public final class Bencode {
    //labeling different value types
    public static void serailizer(Object obj, OutputStream out) throws IOException {
        if (obj instanceof Integer || obj instanceof Long || obj instanceof BigInteger){


        }
        else if(obj instanceof byte[] || obj instanceof String){

        }
        else if(obj instanceof List){

        }
        else if(obj instanceof SortedMap){

        }
        else
            throw  new IllegalArgumentException("Unsupported type: " + obj.getClass().getName());
    }
    public static Object encode(InputStream in) throws IOException{
        Bencode encoder = new Bencode(in);
        Object rezult = encoder.encodeValue(encoder.rByte());
    }
    private InputStream input;

    private Object encodeValue(byte label) throws IOException{
        //we separate different value types to different functions depending on which label we put in serlizer
        if(label == 'i')
                return encodeInteger();
        else if( '')
    }
    private byte rByte() throws IOException{
        int result = input.read();
        if(result == -1)
            throw new EOFException();
        return (byte) result;
    }
}
