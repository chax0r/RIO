package edu.uga.cs.restendpoint.service.api;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;

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
    @Path("{ontologyName}/classes")
    String getAllClasses(@PathParam("ontologyName") String ontologyName,
                                 @Context ServletContext context);
    @GET
 //   @Path("{ontologyName}/classinfo/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    @Path("{ontologyName}/subClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    String getAllSubClassesOfaClass( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
                                            @Context ServletContext context);

    @GET
    @Path("{ontologyName}/superClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    String getAllSuperClassesOfaClass( @PathParam("ontologyName") String ontologyName,
                                              @PathParam("classes") String allClasses,
                                              @Context ServletContext context);
    @GET
    @Path("{ontologyName}/propertiesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    public String getAllPropertiesOfaClass( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("classes") String allClasses,
                                            @Context ServletContext context);
    @GET
    @Path("{ontologyName}/{className}/restrictionValues")
    public String getRestrictionValuesOnaClass( @PathParam("ontologyName") String ontologyName,
                                                @PathParam("className") String className,
                                                @Context ServletContext context);
    @GET
    @Path("{ontologyName}/{className}/restrictions")
    public String getAllRestrictionsONaClass( @PathParam("ontologyName") String ontologyName,
                                              @PathParam("className") String className,
                                              @Context ServletContext context);
    @GET
    @Path("{ontologyName}/{className}/individuals")
    public String getAllInstancesOfClass(  @PathParam("ontologyName") String ontologyName,
                                             @PathParam("className") String className,
                                             @Context ServletContext context);
    @GET
    @Path("{ontologyName}/enumeratedClasses")
    public String getAllEnumeratedClasses( @PathParam("ontologyName") String ontologyName,
                                           @Context ServletContext context );
    @GET
    @Path("{ontologyName}/enumeratedClassInstances")
    public String getAllEnumeratedClassInstances( @PathParam("ontologyName") String ontologyName,
                                                  @Context ServletContext context );
    @GET
    @Path("{ontologyName}/{enumClass}/enumIndividuals")
    public String getInstancesOfEnumeratedClass( @PathParam("ontologyName") String ontologyName,
                                                 @PathParam("enumClass") String enumClass,
                                                 @Context ServletContext context );
    @GET
    @Path("{ontologyName}/{propertyName}/domain")
    public String getDomainOfProperty( @PathParam("ontologyName") String ontologyName,
                                       @PathParam("propertyName") String propertyName,
                                       @Context ServletContext context);
    @DELETE
    @Path("{ontologyName}/subClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    public String deleteSubClasses( @PathParam("ontologyName") String ontologyName,
                                    @PathParam("classes") String allClasses,
                                    @Context ServletContext context);
    @DELETE
    @Path("{ontologyName}/superClassesOf/{classes:([aA-zZ]+,?[aA-zZ]+)+}")
    public String deleteSuperClasses( @PathParam("ontologyName") String ontologyName,
                                      @PathParam("classes") String allClasses,
                                      @Context ServletContext context);
}
