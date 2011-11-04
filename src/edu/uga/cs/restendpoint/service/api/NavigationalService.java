package edu.uga.cs.restendpoint.service.api;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/3/11
 * Time: 9:12 PM
 * Email: <kale@cs.uga.edu>
 */
public interface NavigationalService {
     @GET
    //@Path("{ontologyName}/class/{className}/{associationsQuery:.+}")
    //@Path("{ontologyName}/{associationsQuery:([aA-zZ]+/?[aA-zZ]+)+}")
    //@Path("{ontologyName}/classes/{ass:([aA-zZ]+/?[aA-zZ]+)+}")
    @Path("{ontologyName}/classes/{query:.+}")
    @Produces("application/json")
    public String navigateOntologyClasses(@PathParam("ontologyName") String ontologyName,
                                   @PathParam("query") List<PathSegment> associationsQuery,
                                    @Context ServletContext context );
    @GET
    //@Path("{ontologyName}/class/{className}/{associationsQuery:.+}")
    //@Path("{ontologyName}/{associationsQuery:([aA-zZ]+/?[aA-zZ]+)+}")
    @Path("{ontologyName}/instances/{query:.+}")
    @Produces("application/json")
    public String navigateOntologyInstances (@PathParam("ontologyName") String ontologyName,
                                             @PathParam("query") List<PathSegment> associationsQuery,
                                             @Context ServletContext context );


}
