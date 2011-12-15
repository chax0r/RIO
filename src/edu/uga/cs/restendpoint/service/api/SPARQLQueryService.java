package edu.uga.cs.restendpoint.service.api;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/3/11
 * Time: 9:13 PM
 * Email: <kale@cs.uga.edu>
 */
@Path("/sparqlService")
public interface SPARQLQueryService {
    @POST
    @Path("{ontologyName}/select")
    public String executeQuery ( @PathParam("ontologyName") String ontologyName,
                                 @Context ServletContext context,
                                 String inputXML);

    @GET
    @Path("/resultSet/{identifier}")
    @Produces("application/xml")
    public String getResult( @PathParam("identifier") String identifier,  @Context ServletContext context);
}
