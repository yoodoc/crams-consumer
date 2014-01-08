package com.ktcloudware.crams.consumer;

public class CramsException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 5305749296687488376L;

    public CramsException(String msg) {
        super(msg);
    }

    public CramsException(String msg, Exception e) {
       super(msg, e);
    }

}
