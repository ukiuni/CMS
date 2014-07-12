package org.ukiuni.report.action;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import net.arnx.jsonic.JSON;

@SuppressWarnings("rawtypes")
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class JSONReader implements MessageBodyReader {

	@Override
	public boolean isReadable(Class arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object readFrom(Class clazz, Type arg1, Annotation[] arg2, MediaType arg3, MultivaluedMap arg4, InputStream in) throws IOException, WebApplicationException {
		return JSON.decode(in, clazz);
	}
}
