package edu.uga.cs.restendpoint.service.api;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.FileInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/16/11
 * Time: 6:17 PM
 * Email: <kale@cs.uga.edu>
 */

@Path("/ontologyMgmtService")
public interface OntologyManagementService {

    @GET
    @Path("/display")
    @Produces("application/xml")
    String getAllOntologies(String ontologyName , @Context ServletContext servletContext );

    @GET
    @Path("/{ontologyName}")
    @Produces("application/xml")
    String getOntology( @PathParam("ontologyName") String ontologyName,
                        @Context HttpServletResponse httpResponse,
                        @Context ServletContext servletContext );

    @POST
    @Produces("application/xml")
    @Path("/{ontologyName}")
    String addOntology( @PathParam("ontologyName") String ontologyName,
                        FileInputStream request,
                        @Context ServletContext context);

    @DELETE
    @Path("/{ontologyName}")
    String removeOntology( @PathParam("ontologyName") String ontologyName,
                           @Context ServletContext context);

    @GET
    @Path("/valiadte")
    String validateOntology (@PathParam("ontologyName") String ontologyName, @Context ServletContext context);


}
