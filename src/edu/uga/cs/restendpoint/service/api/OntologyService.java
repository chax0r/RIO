package edu.uga.cs.restendpoint.service.api;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/16/11
 * Time: 6:17 PM
 * Email: <kale@cs.uga.edu>
 */

@Path("/ontologyService")
public interface OntologyService {

    @GET
    @Path("/allOntologies")
    @Produces("application/xml")
    String getAllOntologies(String ontologyName , @Context ServletContext servletContext );

    @GET
    @Path("{ontologyName}")
    @Produces("application/xml")
    String getOntology( @PathParam("ontologyName") String ontologyName, @Context ServletContext servletContext );

    @POST
    @Produces("application/xml")
    String addOntology( InputStream request,
                        @Context ServletContext context);

}
