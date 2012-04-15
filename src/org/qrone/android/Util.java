package org.qrone.android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;

public class Util {
	private static final String TAG = "Util";
	private static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

	public static Uri addImageAsApplication(ContentResolver cr, String name,
			long dateTaken, String directory, String filename, Bitmap source,
			byte[] jpegData) {

		//saveData(directory, filename, jpegData);

		String filePath = directory + "/" + filename;
		ContentValues values = new ContentValues(7);
		values.put(Images.Media.TITLE, name);
		values.put(Images.Media.DISPLAY_NAME, filename);
		values.put(Images.Media.DATE_TAKEN, dateTaken);
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		values.put(Images.Media.DATA, filePath);
		return cr.insert(IMAGE_URI, values);
	}

	public static boolean exsitsFile(String directory, String filename) {
		File file = new File(Environment.getExternalStorageDirectory() + "/" + directory, filename);
		return file.exists();
	}
	
	public static InputStream loadFile(String directory, String filename) {
		try {
			File file = new File(Environment.getExternalStorageDirectory() + "/" + directory, filename);
			if(file.exists()){
				return new FileInputStream(file);
			}
			
		} catch (IOException ex) {
			Log.w(TAG, ex);
		}
		
		return null;
	}

	public static OutputStream saveFile(String directory, String filename) {
		OutputStream outputStream = null;
		try {
			File dir = new File(Environment.getExternalStorageDirectory(), directory);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(Environment.getExternalStorageDirectory() + "/" + directory, filename);
			if (file.createNewFile()) {
				return new FileOutputStream(file);
			}

		} catch (FileNotFoundException ex) {
			Log.w(TAG, ex);
		} catch (IOException ex) {
			Log.w(TAG, ex);
		}
		return null;
	}
	

	public static byte[] read(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copy(in, out);
		in.close();
		out.close();
		return out.toByteArray();
	}

	public static String read(Reader r) throws IOException {
		StringWriter w = new StringWriter();
		copy(r, w);
		r.close();
		w.close();
		return w.toString();
	}
	

	public static void copy(Reader r, Writer w) throws IOException {
		if (r == null || w == null)
			throw new IOException();
		int l;
		char[] buf = new char[1024];
		while ((l = r.read(buf)) != -1) {
			w.write(buf,0,l);
		}
		w.flush();
		w.close();
		r.close();
	}

	public static void copy(InputStream in, OutputStream out)
			throws IOException {
		if (in == null || out == null)
			throw new IOException();
		int l;
		byte[] buf = new byte[1024];
		while ((l = in.read(buf)) != -1) {
			out.write(buf,0,l);
		}
		out.flush();
		out.close();
		in.close();
	}

	public static String convertStreamToString(InputStream in)
			throws IOException {
		return new String(read(in), "utf8");
	}
	
	public static String byteToString(byte[] b)
			throws IOException {
		return new String(b, "utf8");
	}
}
