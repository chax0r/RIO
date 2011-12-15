package edu.uga.cs.restendpoint.service.api;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/16/11
 * Time: 6:17 PM
 * Email: <kale@cs.uga.edu>
 */

@Path("/ontMgmt")
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

    @PUT
    @Consumes("application/binary")
    @Path("/{ontologyName}")
    String addOntology( @PathParam("ontologyName") String ontologyName,
                        InputStream request,
                        @Context ServletContext context);

    @DELETE
    @Path("/{ontologyName}")
    String removeOntology( @PathParam("ontologyName") String ontologyName,
                           @Context ServletContext context);

    @GET
    @Path("/validate/{ontologyName}")
    String validateOntology (@PathParam("ontologyName") String ontologyName, @Context ServletContext context);


    @PUT
    @Path("/{ontologyName}/NS/{nameSpace}")
    String addNameSpace( @PathParam("ontologyName") String ontologyName, @PathParam("nameSpace") String nameSpace, @Context ServletContext context);

    @GET
    @Path("/{ontologyName}/NS")
    String getNameSpace( @PathParam("ontologyName") String ontologyName, @PathParam("nameSpace") String nameSpace,@Context ServletContext context);


}
