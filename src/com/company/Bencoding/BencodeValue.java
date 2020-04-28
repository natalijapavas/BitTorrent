package com.company.Bencoding;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** BencodeValue serves as a wrapper class to support all data types in Java which can be Bencoded.
 * Following data types are supported:
 * -String
 * -Bytes
 * -Numbers (i.e. int,long,float,double...)
 * -Lists
 * -Maps
 *  Other types are not supported by the specification.
 */
public class BencodeValue {

    private final Object value;
    private char valueType;

    //ByteArrayBuffer byteArrayBuffer=new ByteArrayBuffer()


    public BencodeValue(byte[] value) {
        this.value = value;
        this.valueType='b';
    }

    public BencodeValue(String value) throws UnsupportedEncodingException {
        this.value = value.getBytes(StandardCharsets.UTF_8);
        this.valueType='b';
    }

    public BencodeValue(String value, String enc)
            throws UnsupportedEncodingException {
        this.value = value.getBytes(enc);
        this.valueType='b';
    }

    public BencodeValue(int value) {
        this.value = new Integer(value);
        this.valueType='i';
    }

    public BencodeValue(long value) {
        this.value = new Long(value);
        this.valueType='i';
    }

    public BencodeValue(Number value) {
        this.value = value;
        this.valueType='i';
    }

    public BencodeValue(List<BencodeValue> value) {
        this.value = value;
        this.valueType='l';
    }

    public BencodeValue(Map<String, BencodeValue> value) {
        this.value = value;
        this.valueType='d';
    }


    public Object getValue() {
        return value;
    }

    public String getString() throws BencodeFormatException {
        return this.getString("UTF-8");
    }

    public char getValueType() {
        return valueType;
    }

    /**
     * Returns this BencodeValue as a String, interpreted with the specified
     * encoding.
     */
    public String getString(String encoding) throws BencodeFormatException {
        try {
            return new String(this.getBytes(), encoding);
        } catch (ClassCastException cce) {
            throw new BencodeFormatException(cce.toString());
        } catch (UnsupportedEncodingException uee) {
            throw new InternalError(uee.toString());
        }
    }

    /**
     * Returns this BencodeValue as a byte[].
     */
    public byte[] getBytes() throws BencodeFormatException {
        try {
            return (byte[])this.value;
        } catch (ClassCastException cce) {
            throw new BencodeFormatException(cce.toString());
        }
    }

    /**
     * Returns BencodeValue as a Number.
     */
    public Number getNumber() throws BencodeFormatException {
        try {
            return (Number)this.value;
        } catch (ClassCastException cce) {
            throw new BencodeFormatException(cce.toString());
        }
    }

    /**
     * Returns BencodeValue as short.
     */
    public short getShort() throws BencodeFormatException {
        return this.getNumber().shortValue();
    }

    /**
     * Returns BencodeValue as int
     */
    public int getInt() throws BencodeFormatException {
        return this.getNumber().intValue();
    }

    /**
     * Returns BecodeValue as long
     */
    public long getLong() throws BencodeFormatException {
        return this.getNumber().longValue();
    }

    /**
     * Returns a list of BencodeValues
     */

    public List<BencodeValue> getList() throws BencodeFormatException {
        if (this.value instanceof ArrayList) {
            return (ArrayList<BencodeValue>)this.value;
        } else {
            throw new BencodeFormatException("Excepted List<BencodeValue> !");
        }
    }

    /**
     * Returns a map of String:BencodeValue pairs
     */
    public Map<String, BencodeValue> getMap() throws BencodeFormatException {
        if (this.value instanceof HashMap) {
            return (LinkedHashMap<String, BencodeValue>)this.value;
        } else {
            throw new BencodeFormatException("Expected Map<String, BencodeValue> !");
        }
    }

    @Override
    public String toString() {
        try {
            if (value instanceof byte[]) {
                return this.getBytes().toString();
            } else if (value instanceof String) {
                return (String)value;
            } else if (value instanceof Number) {
                return this.getNumber().toString();
            } else if (value instanceof List) {
                StringBuilder stringBuilder = new StringBuilder("[ ");
                List <BencodeValue>valueList=this.getList();
                for(BencodeValue bencodeValue:valueList)
                    stringBuilder.append(bencodeValue.toString()+",");
                stringBuilder.append("]");
                return stringBuilder.toString();
            } else if (value instanceof Map) {
                StringBuilder stringBuilder = new StringBuilder("{ ");
                Map<String, BencodeValue> map = this.getMap();
                for (String keys : map.keySet()) {
                    stringBuilder.append(keys + ":" + map.get(keys).toString() + '\n');
                }
                stringBuilder.append('}' );
                return stringBuilder.toString();
            } else
                return "";
        }
        catch (BencodeFormatException e)
        {
            e.printStackTrace();
        }
        return "";
    }

}
