package org.ukiuni.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.ukiuni.report.PathAndHTMLResolver.PathAndHtml;

/**
 * Servlet Filter implementation class AngularJSRouteBuilderFromTemplates
 */
public class AngularJSRouteBuilderFromTemplates implements Filter {
	private static List<PathAndHtml> PATH_AND_HTMLS;

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
		writer.write("function setupRoute($routeProvider){$routeProvider");
		for (PathAndHtml pathAndHtml : PATH_AND_HTMLS) {
			writer.write(".when(\'" + pathAndHtml.path + "\',{templateUrl : \'" + pathAndHtml.html + "?\' + new Date().getTime()})");
		}
		writer.write(".otherwise({redirectTo : \'/\'})};");
		writer.flush();
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		PATH_AND_HTMLS = PathAndHTMLResolver.instance().resolve(config.getServletContext());
	}
}
