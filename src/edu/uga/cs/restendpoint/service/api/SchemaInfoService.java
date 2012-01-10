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
@Path("/ontService")
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
     * @return : Returns a XML formatted string of all the classes present in the mentioned ontology
     */
    @GET
    @Path("{ontologyName}/allClasses")
    @Produces("application/xml")
    String getAllClasses(@PathParam("ontologyName") String ontologyName,
                                 @Context ServletContext context);

    @GET
    @Path("{ontologyName}/class/{className}")
    @Produces("application/xml")
    String getClass( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("className") String inputClassName,
                                            @Context ServletContext context);

    @PUT
    @Path("{ontologyName}/class/{className}")
    @Produces("application/xml")
    String createClass( @PathParam("ontologyName") String ontologyName,
                        @PathParam("className")    String inputClassName,
                                            InputStream request,
                                            @Context ServletContext context);
    @POST
    @Path("{ontologyName}/class/{className}")
    @Produces("application/xml")
    String updateClass( @PathParam("ontologyName") String ontologyName,
                        @PathParam("className") String inputClassName,
                                            InputStream request,
                                            @Context ServletContext context);
    @DELETE
    @Path("{ontologyName}/class/{className}")
    @Produces("application/xml")
    String deleteClass( @PathParam("ontologyName") String ontologyName,
                                    @PathParam("className") String inputClassName,
                                    @Context ServletContext context);
    @GET
    @Path("{ontologyName}/property/{propertyName}")
    @Produces("application/xml")
    String getProperty( @PathParam("ontologyName") String ontologyName,
                          @PathParam("propertyName") String inputPropertyName,
                          @Context ServletContext context );
    @PUT
    @Path("{ontologyName}/property/{propertyName}")
    @Produces("application/xml")
    String createProperty( @PathParam("ontologyName") String ontologyName,
                           @PathParam("propertyName") String inputPropertyName,
                                    InputStream inputXML,
                                    @Context ServletContext context);

    @POST
    @Path("{ontologyName}/property/{propertyName}")
    @Produces("application/xml")
    String updateProperty( @PathParam("ontologyName") String ontologyName,
                           @PathParam("propertyName") String propertyName,
                           InputStream inputXML,
                           @Context ServletContext servletContext);

    @GET
    @Path("{ontologyName}/instanceOf/{className}")
    @Produces("application/xml")
    String getInstancesOf(  @PathParam("ontologyName") String ontologyName,
                                             @PathParam("className") String inputClassName,
                                             @Context ServletContext context);
    @PUT
    @Path("{ontologyName}/instanceOf/{className}")
    @Produces("application/xml")
    String createInstancesOf(  @PathParam("ontologyName") String ontologyName,
                               @PathParam("className") String className,
                                      InputStream inputXML,
                                      @Context ServletContext context);


    @GET
    @Path("{ontologyName}/restrictionsFor/{className}")
    @Produces("application/xml")
    String getRestrictionsForClass( @PathParam("ontologyName") String ontologyName,
                                                @PathParam("className") String inputClassName,
                                                @Context ServletContext context);
    @PUT
    @Path("{ontologyName}/restrictionsFor/{className}")
    @Produces("application/xml")
    String createRestrictionsForClasses( @PathParam("ontologyName")String ontologyName,
                                         @PathParam("className") String inputClassName,
                                                InputStream inputXML,
                                                @Context ServletContext context);

    @DELETE
    @Path("{ontologyName}/restrictionOf/{className}/{propertyName}")
    @Produces("application/xml")
    String getRestrictionsOfClass( @PathParam("ontologyName") String ontologyName,
                                   @PathParam("className") String className,
                                   @PathParam("propertyName") String propertyName,
                                   @Context ServletContext context);
    @GET
    @Path("{ontologyName}/subClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String getSubClassesOf( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
                                            @Context ServletContext context);

    @GET
    @Path("{ontologyName}/superClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String getSuperClassesOf( @PathParam("ontologyName") String ontologyName,
                                              @PathParam("classes") String allClasses,
                                              @Context ServletContext context);
    @GET
    @Path("{ontologyName}/propertiesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Produces("application/xml")
    String getPropertiesOfClasses( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
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
