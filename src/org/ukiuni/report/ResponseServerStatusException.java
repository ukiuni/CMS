package org.ukiuni.report;

@SuppressWarnings("serial")
public class ResponseServerStatusException extends RuntimeException {

	public final int statusCode;
	public final String message;

	public ResponseServerStatusException(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}
}
