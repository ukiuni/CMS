package org.ukiuni.report.action;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

@Path("/app")
public class ApplicationAction {
	private static int ROOT_URL_PATH_LENGTH = -1;// default

	@GET
	@Path("/rootURL.js")
	@Produces("application/javascript")
	public String urlRoot(@Context HttpServletRequest request) {
		String requestPath = request.getRequestURL().toString();
		if (0 > ROOT_URL_PATH_LENGTH) {
			ROOT_URL_PATH_LENGTH = (request.getServletPath() + "/app/rootURL.js").length() - 1;
		}
		String rootPath = requestPath.substring(0, requestPath.length() - ROOT_URL_PATH_LENGTH);
		return "document.write(\"<base href=\'" + rootPath + "\' />\");";
	}
}
