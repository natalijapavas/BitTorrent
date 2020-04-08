package com.company.Bencoding;



import java.io.IOException;


/**
 * Exception thrown when a B-encoded stream cannot be decoded.
 *
 * @author mpetazzoni
 */
public class BencodeFormatException extends IOException {

    public static final long serialVersionUID = -1;

    public BencodeFormatException(String message) {
        super(message);
    }
}