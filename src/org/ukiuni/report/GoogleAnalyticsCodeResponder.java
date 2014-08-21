package org.ukiuni.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Servlet Filter implementation class AngularJSRouteBuilderFromTemplates
 */
public class GoogleAnalyticsCodeResponder implements Filter {
	private static final String SYSTEM_KEY = "com.google.analytics.userId";

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		response.setContentType("text/javascript");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		String googleKey = System.getProperty(SYSTEM_KEY, null);
		if (null == googleKey) {
			googleKey = System.getenv(SYSTEM_KEY);
		}
		if (null != googleKey) {
			writer.write("(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){");
			writer.write("(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),");
			writer.write("m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)");
			writer.write("})(window,document,'script','//www.google-analytics.com/analytics.js','ga');");
			writer.write("ga('create', '" + googleKey + "', 'auto');");
			writer.write("function track(address){ga('send', 'pageview', address);}");
		}else{
			writer.write("function track(){}");
		}
		writer.flush();
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
	}
}
