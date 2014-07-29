package org.ukiuni.report.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
		PUSH_STATE_LIST.addAll(Arrays.asList(contextPath + "/myPage", contextPath + "/editReport", contextPath + "/editProfile", contextPath + "/report", contextPath + "/login", contextPath + "/logout"));
	}
}
