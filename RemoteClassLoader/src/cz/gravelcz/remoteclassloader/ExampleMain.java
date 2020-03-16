package cz.gravelcz.remoteclassloader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.net.ssl.HttpsURLConnection;

public class ExampleMain {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("You need to supply url");
			return;
		}
		
		//just downloading from an url
		HttpsURLConnection conn = (HttpsURLConnection) new URL(args[0]).openConnection();
		int code = conn.getResponseCode();
		if (code != 200) {
			System.out.println("Did not recieve 200");
			return;
		}
		
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		InputStream is = conn.getInputStream();
		byte[] data = new byte[1024 * 20];
		
		while (is.read(data) != -1) {
			b.write(data);
		}
		
		RemoteClassLoader loader = new RemoteClassLoader(b.toByteArray());
		
		//required so that the app we will run will use our loader
		Thread.currentThread().setContextClassLoader(loader);
		
		//finding main class from MANIFEST.MF
		Manifest mnfs = new Manifest(loader.getResourceAsStream("META-INF/MANIFEST.MF"));
		Attributes a = mnfs.getMainAttributes();
		String main = (String) a.getValue("Main-Class");
		
		// we get the class using out loader
		// the true makes it so we load static stuff as well
		Class<?> clazz = Class.forName(main, true, loader);
		//that will find main method
		Method mainMethod = clazz.getMethod("main", String[].class);
		//this will start the main method
		mainMethod.invoke(null, (Object) new String[] { "argument1", "argument2" });
		
	}
	
}
