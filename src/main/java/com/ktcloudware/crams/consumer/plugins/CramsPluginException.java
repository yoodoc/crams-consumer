package com.ktcloudware.crams.consumer.plugins;

public class CramsPluginException extends Exception {
    private static final long serialVersionUID = -7031430568440580162L;

    public CramsPluginException(Exception e) {
        super(e);
    }

    public CramsPluginException(String string) {
        super(string);
    }

    public CramsPluginException(String string, Exception e) {
        super(string, e);
    }

    /**
	 * 
	 */

}
