package org.ukiuni.report.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/report")
public class ReportAction {

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public void saveReport(String body) {
	}

	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public void deleteTodos(String body) {
	}

}
