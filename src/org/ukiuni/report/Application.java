package org.ukiuni.report;

import java.util.HashSet;
import java.util.Set;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

public class Application extends javax.ws.rs.core.Application {
	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(MultiPartFeature.class);
		classes.add(LoggingFilter.class);
		return classes;
	}
}
