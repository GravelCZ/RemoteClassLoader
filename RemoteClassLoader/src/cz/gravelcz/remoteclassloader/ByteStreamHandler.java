package cz.gravelcz.remoteclassloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class ByteStreamHandler extends URLStreamHandler {

	private byte[] data;
	private String name;
	
	public ByteStreamHandler(byte[] data, String name) {
		this.data = data;
		this.name = name;
	}
	
	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		if (data == null || name == null) {
			throw new UnsupportedOperationException("byte array needs to be defined in constructor");
		}
		
		//Resource not match
		if (!url.getFile().endsWith(name)) {
			throw new UnsupportedOperationException("Resource URL and file name do not match.");
		}
		
		return new ByteURLConnection(url, data);
	}

}
