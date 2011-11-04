package edu.uga.cs.restendpoint.service.api;

import javax.servlet.ServletContext;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/3/11
 * Time: 9:13 PM
 * Email: <kale@cs.uga.edu>
 */
@Path("/query")
public interface SPARQLQueryService {
    @POST
    @Path("{ontologyName}/select")
    @Produces("text/html")
    public String executeQuery ( @PathParam("ontologyName") String ontologyName,
                                 @Context ServletContext context,
                                 String queryString );
}
