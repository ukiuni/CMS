package org.ukiuni.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Servlet Filter implementation class AngularJSRouteBuilderFromTemplates
 */
public class AngularJSRouteBuilderFromTemplates implements Filter {
	private File templatesDirectory;
	private File webRootDirectory;

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().endsWith(".html") || file.getName().endsWith(".htm");
			}
		};
		List<PathAndHtml> pathAndHtmls = new ArrayList<PathAndHtml>();
		int parentPathLength = webRootDirectory.getAbsolutePath().length();
		for (File templateFile : templatesDirectory.listFiles(filter)) {
			String fileName = templateFile.getName();
			String path = "/" + fileName.substring(0, fileName.lastIndexOf("."));
			if (path.endsWith("index")) {
				path = path.substring(0, path.length() - "index".length());
			} 
			String canonicalPath = templateFile.getAbsolutePath().substring(parentPathLength + 1).replace(File.pathSeparator, "/");
			pathAndHtmls.add(new PathAndHtml(path, canonicalPath));
		}

		response.setContentType("text/javascript");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		writer.write("function setupRoute($routeProvider){$routeProvider");
		for (PathAndHtml pathAndHtml : pathAndHtmls) {
			writer.write(".when(\'" + pathAndHtml.path + "\',{templateUrl : \'" + pathAndHtml.html + "?\' + new Date().getTime()})");
		}
		writer.write(".otherwise({redirectTo : \'/\'})};");
		writer.flush();
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		templatesDirectory = new File(config.getServletContext().getRealPath("template"));
		webRootDirectory = new File(config.getServletContext().getRealPath("/"));

	}

	private static class PathAndHtml {
		public PathAndHtml(String path, String html) {
			super();
			this.path = path;
			this.html = html;
		}

		public String path;
		public String html;
	}
}
