package org.ukiuni.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.ukiuni.report.PathAndHTMLResolver.PathAndHtml;

/**
 * Servlet implementation class ShowIndexHTMLServlet
 */
@WebFilter("/*")
public class ShowIndexHTMLFilter implements Filter {
	private static final List<String> PUSH_STATE_LIST = new ArrayList<String>();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ShowIndexHTMLFilter() {
		super();
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String requestURI = ((HttpServletRequest) request).getRequestURI();
		if (PUSH_STATE_LIST.contains(requestURI)) {
			request.getRequestDispatcher("index.html").forward(request, response);
			return;
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		String contextPath = config.getServletContext().getContextPath();
		if ("/".equals(contextPath)) {
			contextPath = "";
		}
		List<PathAndHtml> pathAndHtmls = PathAndHTMLResolver.instance().resolve(config.getServletContext());
		for (PathAndHtml pathAndHtml : pathAndHtmls) {
			PUSH_STATE_LIST.add(contextPath + pathAndHtml.path);
		}
	}
}
