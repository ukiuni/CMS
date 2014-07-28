package org.ukiuni.report.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
}
