package edu.uga.cs.restendpoint.service.api;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.InputStream;

/**
 * Author: kale
 * Date: 11/3/11
 * Time: 9:12 PM
 * Email: <kale@cs.uga.edu>
 */
@Path("/schemaService")
public interface SchemaInfoService {

    @GET
    @Path("index.html")
    @Produces("text/html")
    String getInfo(@Context ServletContext servletContext,
                          @Context HttpServletRequest req,
                          @Context HttpServletResponse res);

    /**
     * This Method returns the names of all the classes present in the mentioned ontology
     * @param ontologyName : The ontology whose classes are requested
     * @param context
     * @return : Returns a JSON formatted string of all the classes present in the mentioned ontology
     */
    @GET
    @Path("{ontologyName}/allClasses")
    @Produces("application/xml")
    String getAllClasses(@PathParam("ontologyName") String ontologyName,
                                 @Context ServletContext context);

    @GET
    @Path("{ontologyName}/classes/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String getClasses( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
                                            @Context ServletContext context);

    @POST
    @Path("{ontologyName}/classes/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String createClasses( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
                                            InputStream request,
                                            @Context ServletContext context);
    @PUT
    @Path("{ontologyName}/classes/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String updateClasses( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
                                            InputStream request,
                                            @Context ServletContext context);
    @DELETE
    @Path("{ontologyName}/classes/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/json")
    public String deleteClasses( @PathParam("ontologyName") String ontologyName,
                                    @PathParam("classes") String allClasses,
                                    @Context ServletContext context);
    @GET
 //   @Path("{ontologyName}/classinfo/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Path("{ontologyName}/subClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String getSubClassesOf( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
                                            @Context ServletContext context);
    @POST
    @Path("{ontologyName}/subClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String createSubClassesOf( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
                                            InputStream request,
                                            @Context ServletContext context);

    @PUT
    @Path("{ontologyName}/subClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String updateSubClassesOf( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
                                            InputStream request,
                                            @Context ServletContext context);
    @DELETE
    @Path("{ontologyName}/subClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/json")
    public String deleteSubClassesOf( @PathParam("ontologyName") String ontologyName,
                                    @PathParam("classes") String allClasses,
                                    @Context ServletContext context);

    @GET
    @Path("{ontologyName}/superClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String getSuperClassesOf( @PathParam("ontologyName") String ontologyName,
                                              @PathParam("classes") String allClasses,
                                              @Context ServletContext context);
    @POST
    @Path("{ontologyName}/superClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String createSuperClassesOf( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
                                            InputStream request,
                                            @Context ServletContext context);

    @PUT
    @Path("{ontologyName}/superClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String updateSuperClassesOf( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
                                            InputStream request,
                                            @Context ServletContext context);
    @DELETE
    @Path("{ontologyName}/superClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/json")
    public String deleteSuperClassesOf( @PathParam("ontologyName") String ontologyName,
                                      @PathParam("classes") String allClasses,
                                      @Context ServletContext context);

      @GET
    @Path("{ontologyName}/instancesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/json")
    public String getInstancesOf(  @PathParam("ontologyName") String ontologyName,
                                             @PathParam("classes") String allClasses,
                                             @Context ServletContext context);
    @POST
    @Path("{ontologyName}/instancesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/json")
    public String createInstancesOf(  @PathParam("ontologyName") String ontologyName,
                                             @PathParam("classes") String allClasses,
                                             InputStream inputXML,
                                             @Context ServletContext context);

    @PUT
    @Path("{ontologyName}/instancesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/json")
    public String updateInstancesOf(  @PathParam("ontologyName") String ontologyName,
                                             @PathParam("classes") String allClasses,
                                             InputStream inputXML,
                                             @Context ServletContext context);

    @GET
    @Path("{ontologyName}/propertiesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String getPropertiesOfClasses( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
                                            @Context ServletContext context);

    @GET
    @Path("{ontologyName}/properties/{properties:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String getProperties( @PathParam("ontologyName") String ontologyName,
                          @PathParam("properties") String allProperties,
                          @Context ServletContext context );
    @GET
    @Path("{ontologyName}/restrictionValuesFor/{className}")
    //@Path("{ontologyName}/restrictionValuesFor/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/json")
    String getRestrictionValuesForClass( @PathParam("ontologyName") String ontologyName,
                                                @PathParam("className") String classes,
                                                @Context ServletContext context);
    @GET
    @Path("{ontologyName}/restrictionsFor/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/json")
    public String getAllRestrictionsForClasses( @PathParam("ontologyName") String ontologyName,
                                              @PathParam("classes") String classes,
                                              @Context ServletContext context);

    @GET
    @Path("{ontologyName}/enumeratedClasses")
    @Produces("application/json")
    public String getAllEnumeratedClasses( @PathParam("ontologyName") String ontologyName,
                                           @Context ServletContext context );
    @GET
    @Path("{ontologyName}/enumeratedClassInstances")
    @Produces("application/json")
    public String getAllEnumeratedClassInstances( @PathParam("ontologyName") String ontologyName,
                                                  @Context ServletContext context );
    @GET
    @Path("{ontologyName}/enumInstancesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/json")
    public String getInstancesOfEnumeratedClass( @PathParam("ontologyName") String ontologyName,
                                                 @PathParam("enumClass") String allClasses,
                                                 @Context ServletContext context );
    @GET
    @Path("{ontologyName}/domainOf/{properties:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/json")
    public String getDomainOfProperties( @PathParam("ontologyName") String ontologyName,
                                       @PathParam("properties") String properties,
                                       @Context ServletContext context);


}
