package com.company.Bencoding;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.EOFException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/** BencodeDecoder class does does the decoding of the bencoded data into usable form, which are Java objects to be
 * used for future tasks.
 * Class fields:
 * -InputsSream in
 * -int indicator, which determines next bencoded object to be decoded. Indicator can take following values:
 *  -- Zero if unknown.
 *     '0'..'9' for a byte[].
 *     'i' for an Number.
 *     'l' for a List.
 *     'd' for a Map.
 *     'e' specifies end of Number, List or Map (only used internally).
 *     -1  end of stream.
*/


public class BencodeDecoder {

    private final InputStream in;
    private int indicator = 0;

    public BencodeDecoder(InputStream in)
    {
        this.in = in;
    }


    //Ja bih ovo da prepravim da ipak ne radi kao staticki metod, stvara suvise overhead-a u pozivu
    /**
     Static method which serves to decode given input stream. Since it is public, it will be used whenever decoding
     is necessary. J
     */
    /*public static BencodeValue decode(InputStream in) throws IOException {
        return new BencodeDecoder(in).decode();
    } */

    /**
     * Decode a B-encoded byte buffer.
     *
     * <p>
     * Automatically instantiates a new BencodeDecoder for the provided buffer and
     * decodes its root member.
     * </p>
     *
     * @param data The {@link ByteBuffer} to read from.
     */
    public static BencodeValue bdecode(ByteBuffer data) throws IOException {
//        return BencodeDecoder.decode(new AutoCloseInputStream(
//                new ByteArrayInputStream(data.array())));
        return new BencodeDecoder(new ByteArrayInputStream(data.array())).decode();
    }

    /**
     * Returns next benocded object to be rad from the input stream.
     * Value of -1 signifies end of input.
     */
    private int getNextIndicator() throws IOException {
        if (this.indicator == 0) {
            this.indicator = in.read();
        }
        return this.indicator;
    }

    /**
     * Gets the next indicator and returns either null when the stream
     * has ended or b-decodes the rest of the stream and returns the
     * appropriate BencodeValue encoded object.
     */
    public BencodeValue decode() throws IOException	{
        if (this.getNextIndicator() == -1)
            return null;
        //depending on the possible value of indicator, we decode into an appropriate Object
        //Changed from switch because of efficiency reasons
        if (this.indicator >= '0' && this.indicator <= '9')
            return this.decodeBytes();
        else if (this.indicator == 'i')
            return this.decodeNumber();
        else if (this.indicator == 'l')
            return this.decodeList();
        else if (this.indicator == 'd')
            return this.decodeMap();
        else
            throw new BencodeFormatException
                    ("Unknown indicator '" + this.indicator + "'");
    }

    /**
     * Returns the next b-encoded value on the stream and makes sure it is a
     * byte array.
     */
    public BencodeValue decodeBytes() throws IOException {
        int c = this.getNextIndicator();
        int num = c - '0';
        if (num < 0 || num > 9)
            throw new BencodeFormatException("Number expected, not '"
                    + (char)c + "'");
        this.indicator = 0;

        c = this.read();
        int i = c - '0';
        while (i >= 0 && i <= 9) {
            // This can overflow!
            num = num*10 + i; //reconstruct a number digit by digit
            c = this.read();
            i = c - '0';
        }

        if (c != ':') {
            throw new BencodeFormatException("Colon expected, not '" +
                    (char)c + "'");
        }

        return new BencodeValue(read(num));
    }

    /**
     * Returns the next b-encoded value on the stream and makes sure it is a
     * number.
     */
    public BencodeValue decodeNumber() throws IOException {
        int c = this.getNextIndicator();
        if (c != 'i') {
            throw new BencodeFormatException("Expected 'i', not '" +
                    (char)c + "'");
        }
        this.indicator = 0;

        c = this.read();
        if (c == '0') {
            c = this.read();
            if (c == 'e')
                return new BencodeValue(BigInteger.ZERO);
            else
                throw new BencodeFormatException("'e' expected after zero," +
                        " not '" + (char)c + "'");
        }

        // We don't support more the 255 char big integers
        char[] chars = new char[256];
        int off = 0;

        if (c == '-') {
            c = this.read();
            if (c == '0')
                throw new BencodeFormatException("Negative zero not allowed");
            chars[off] = '-';
            off++;
        }

        if (c < '1' || c > '9')
            throw new BencodeFormatException("Invalid Integer start '"
                    + (char)c + "'");
        chars[off] = (char)c;
        off++;

        c = this.read();
        int i = c - '0';
        while (i >= 0 && i <= 9) {
            chars[off] = (char)c;
            off++;
            c = read();
            i = c - '0';
        }

        if (c != 'e')
            throw new BencodeFormatException("Integer should end with 'e'");

        String s = new String(chars, 0, off);
        return new BencodeValue(new BigInteger(s));
    }

    /**
     * Returns the next b-encoded value on the stream and makes sure it is a
     * list.
     */
    public BencodeValue decodeList() throws IOException {
        int c = this.getNextIndicator();
        if (c != 'l') {
            throw new BencodeFormatException("Expected 'l', not '" +
                    (char)c + "'");
        }
        this.indicator = 0;

        List<BencodeValue> result = new ArrayList<BencodeValue>();
        c = this.getNextIndicator();
        while (c != 'e') {
            result.add(this.decode());
            c = this.getNextIndicator();
        }
        this.indicator = 0;

        return new BencodeValue(result);
    }

    /**
     * Returns the next b-encoded value on the stream and makes sure it is a
     * map (dictionary).
     */
    public BencodeValue decodeMap() throws IOException {
        int c = this.getNextIndicator();
        if (c != 'd') {
            throw new BencodeFormatException("Expected 'd', not '" +
                    (char)c + "'");
        }
        this.indicator = 0;

        Map<String, BencodeValue> result = new HashMap<String, BencodeValue>();
        c = this.getNextIndicator();
        while (c != 'e') {
            // Dictionary keys are always strings.
            String key = this.decode().getString();

            BencodeValue value = this.decode();
            result.put(key, value);

            c = this.getNextIndicator();
        }
        this.indicator = 0;

        return new BencodeValue(result);
    }

    /**
     * Returns the next byte read from the InputStream (as int).
     *
     * @throws EOFException If InputStream.read() returned -1.
     */
    private int read() throws IOException {
        int c = this.in.read();
        if (c == -1)
            throw new EOFException();
        return c;
    }

    /**
     * Returns a byte[] containing length valid bytes starting at offset zero.
     *
     * @throws EOFException If InputStream.read() returned -1 before all
     * requested bytes could be read.  Note that the byte[] returned might be
     * bigger then requested but will only contain length valid bytes.  The
     * returned byte[] will be reused when this method is called again.
     */
    private byte[] read(int length) throws IOException {
        byte[] result = new byte[length];

        int read = 0;
        while (read < length)
        {
            int i = this.in.read(result, read, length - read);
            if (i == -1)
                throw new EOFException();
            read += i;
        }

        return result;
    }
}