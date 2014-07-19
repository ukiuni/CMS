package org.ukiuni.report.action;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ukiuni.report.ResponseServerStatusException;

@Provider
public class ExceptionHandler implements ExceptionMapper<Throwable> {
	@Override
	public Response toResponse(Throwable exception) {
		if (exception instanceof ResponseServerStatusException) {
			ResponseServerStatusException e = (ResponseServerStatusException) exception;
			return Response.status(e.statusCode).encoding("UTF-8").entity(e.message).build();
		}
		if (exception instanceof ClientErrorException) {
			ClientErrorException e = (ClientErrorException) exception;
			return e.getResponse();
		}
		exception.printStackTrace();
		return Response.status(Status.BAD_REQUEST).encoding("UTF-8").entity(exception.getMessage()).build();
	}
}