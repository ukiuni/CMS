package org.ukiuni.report.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class StreamUtil {
	public static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		for (int readed = in.read(buffer); readed > 0; readed = in.read(buffer)) {
			bout.write(buffer, 0, readed);
		}
		return bout.toByteArray();
	}

	public static String toString(InputStream in) {
		try {
			return new String(toByteArray(in));
		} catch (IOException e) {
			return null;
		}
	}

	public static InputStream inputQuietry(URL url) {
		try {
			return url.openConnection().getInputStream();
		} catch (IOException e) {
			return null;
		}
	}

	public static InputStream inputQuietry(URL url, String path) {
		try {
			return inputQuietry(new URL(url, path));
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
