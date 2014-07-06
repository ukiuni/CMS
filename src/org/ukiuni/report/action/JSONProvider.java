package org.ukiuni.report.action;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import net.arnx.jsonic.JSON;

@SuppressWarnings("rawtypes")
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JSONProvider implements MessageBodyWriter {
	@Override
	public long getSize(Object arg0, Class arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
		try {
			return JSON.encode(arg0).getBytes("UTF-8").length;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isWriteable(Class arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return true;
	}

	@Override
	public void writeTo(Object arg0, Class arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap arg5, OutputStream arg6) throws IOException, WebApplicationException {
		JSON.encode(arg0, arg6);
	}

}
