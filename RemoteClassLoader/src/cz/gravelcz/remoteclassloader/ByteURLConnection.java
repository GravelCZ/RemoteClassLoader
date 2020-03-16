package cz.gravelcz.remoteclassloader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ByteURLConnection extends URLConnection {

	private byte[] data;
	private ByteArrayInputStream is;
	
	protected ByteURLConnection(URL url) {
		super(url);
	}
	
	public ByteURLConnection(URL url, byte[] data) {
		super(url);
		
		this.data = data;
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		if (is == null) {
			connect();
		}
		
		return is;
	}

	@Override
	public void connect() throws IOException {
		if (data == null) {
			throw new IOException("Data is null");
		}
		
		is = new ByteArrayInputStream(data);
	}

}
