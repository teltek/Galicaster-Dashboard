package org.galicaster.dashboard.endpoint;

import org.opencastproject.util.NotFoundException;

import org.galicaster.dashboard.DashboardService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("/")
public class DashboardRestService {
  private static final Logger logger = LoggerFactory.getLogger(DashboardRestService.class);
  private DashboardService service;

  protected void activate(ComponentContext cc) {
    logger.info("Galicaster REST Endpoint activated");
  }

  protected void deactivate(ComponentContext cc) {
    logger.info("Galicaster REST Endpoint deactivated");
  }

  /**
   * Callback from OSGi that is called when this service is activated.
   * 
   * @param cc
   *          OSGi component context
   */
  public void setService(DashboardService service) {
    this.service = service;
  }

  public void unsetService(DashboardService service) {
    this.service = null;
  }

  @GET
  @Path("agents/{name}")
  public Response isActive(@PathParam("name") String name) {
    if (service.isActive(name))
      return Response.noContent().build();
    else
      throw new WebApplicationException(Response.Status.NOT_FOUND);
  }

  @GET
  @Path("agents/{name}/snapshot.png")
  public Response getSnapshot(@PathParam("name") String name) throws IOException {
    try {
      File retFile = service.getSnapshot(name);
      return Response.ok(retFile).header("Content-Length", retFile.length())
              .header("Content-Type", "image/png").build();
    } catch (NotFoundException e) {
      throw new WebApplicationException(e, Response.Status.NOT_FOUND);
    } catch (Exception e) {
      logger.error("Received exception {}: {}", e.getClass().getCanonicalName(), e.getMessage());
      throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }
}