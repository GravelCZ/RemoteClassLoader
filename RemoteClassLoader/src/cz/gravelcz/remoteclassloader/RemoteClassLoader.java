package cz.gravelcz.remoteclassloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This is a classloader that can load classes from byte array
 * It prioritizes it's own loaded(stored in hashmap) classes and resources over the parent class loader
 * meaning if you want to load a class a.b.c this classloader will first check if the class exists
 * if not then it will ask the parent class loader otherwise it will load it.
 * this includes resources
 * 
 * @author GravelCZ
 *
 */
public class RemoteClassLoader extends ClassLoader {

	private Map<String, byte[]> byteData = new HashMap<>();
	
	public RemoteClassLoader(ClassLoader parent, byte[] data) throws Exception 
	{
		super(parent);
		loadDataFromBytes(data);
	}
	
	public RemoteClassLoader(byte[] data) throws IOException {
		super(ClassLoader.getSystemClassLoader());
		loadDataFromBytes(data);
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException 
	{
		if (byteData.isEmpty()) {
			return this.getParent().loadClass(name);
		}
		byte[] extractedBytes = byteData.get(name);
		if (extractedBytes == null) {
			return this.getParent().loadClass(name);
		}
		
		return defineClass(name, extractedBytes, 0, extractedBytes.length);
	}
	
	@Override
	public URL getResource(String name) {
		URL url = findResource(name);
		
		if (url == null) {
			url = super.getResource(name);
		}
		
		return url;
	}
	
	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		return (new Vector<URL>(Arrays.asList(findResource(name))).elements());
	}
	
	@Override
	protected URL findResource(String name)
	{
		byte[] extractedBytes = byteData.get(name);
		if (extractedBytes != null)
		{
			try {
				return new URL(null, "bytes:///" + name, new ByteStreamHandler(extractedBytes, name));
			} catch (MalformedURLException e) {}
		}
		return null;
	}
	
	private void loadDataFromBytes(byte[] data) 
	{
		try {
			ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data));
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null)
			{
				//exclude directories
				if (entry.getName().endsWith("/")) {
					zis.closeEntry();
					continue;
				}
				
				//reading the entry
				ByteArrayOutputStream  bos = new ByteArrayOutputStream();
				int count;
				byte[] buffer = new byte[1024 * 10];
				while ((count = zis.read(buffer)) != -1)
				{
					bos.write(buffer, 0, count);
				}
				
				String entryName = entry.getName();
				
				//checking if the thing loaded is a class and replacing / with . and .class with nothing
				//otherwise just use the normal path
				if (entry.getName().endsWith(".class")) {
					entryName = entry.getName().replaceAll("/", ".").replaceAll(".class", "");	
				}
				
				byteData.put(entryName, bos.toByteArray());	
				
				zis.closeEntry();
			}
			zis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
