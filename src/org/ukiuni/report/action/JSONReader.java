package org.ukiuni.report.action;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import net.arnx.jsonic.JSON;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("rawtypes")
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class JSONReader implements MessageBodyReader {
	private ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

	@Override
	public boolean isReadable(Class arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object readFrom(Class clazz, Type arg1, Annotation[] arg2, MediaType arg3, MultivaluedMap arg4, InputStream in) throws IOException, WebApplicationException {
		Object bean = JSON.decode(in, clazz);
		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<Object>> violations = validator.validate(bean);
		if (null != violations && !violations.isEmpty()) {
			throw new BadRequestException(StringUtils.join(violations, ", "));
		}
		return bean;
	}
}
