package edu.uga.cs.restendpoint.service.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;
import edu.uga.cs.restendpoint.service.api.SchemaInfoService;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.jboss.resteasy.spi.BadRequestException;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.*;
/**
 * Author: kale
 * Date: 10/16/11
 * Time: 10:59 PM
 * Email: <kale@cs.uga.edu>
 */

public class SchemaInfoServiceImpl implements SchemaInfoService {

    /**
     * Method to diplay all the ontologies loaded in the system
     * @param servletContext
     * @param req
     * @param res
     */
    public String getInfo( ServletContext servletContext,
                           HttpServletRequest req,
                           HttpServletResponse res){

        Configuration cfg = null;
        Template template = null;
        Map<String, Object> dataModel = null;
        String templateDir = "WEB-INF/templates";
        String templateName = "index.html";
        PrintWriter toClient = null;
        OntologyModelStore ontologyModelStore = (OntologyModelStore) servletContext.getAttribute( "ontologyModelStore" );

        try {

            // Initialize the FreeMarker configuration;
            // - Create a configuration instance
            //
            cfg = new Configuration();

            cfg.setServletContextForTemplateLoading( servletContext,
                    templateDir );

            // Load templates from the WEB-INF/templates directory of the Web
            // app.
            //
            template = cfg.getTemplate( templateName );

            // Build the data-model
            //
            toClient = res.getWriter();
            dataModel = new HashMap<String, Object>();
            getOntologyDataModel( ontologyModelStore, dataModel );

            // Process the template, using the values from the data-model
            // the instance of the template (with substituted values) will be
            // written to the parameter writer (servlet's output)
            //
            template.process( dataModel, toClient );

            toClient.flush();

        }
        catch( Exception e ) {
            e.printStackTrace( System.err );
        }
        return "";
    }

@GET
//@Path("{ontologyName}/classinfo/{classes:([aA-zZ]+/?[aA-zZ]+)+}")
@Path("{ontologyName}/classinfo/{classes:.+}")

//{classes:[.][,.]*}
    public String getClassInfo( @PathParam("ontologyName") String ontologyName, @PathParam("classes")List<PathSegment> regex){

    for (PathSegment pathSegment : regex) {
        System.out.println(pathSegment.getPath());
    }

/*
    MultivaluedMap m1 = id.getMatrixParameters();
    for(Map.Entry<String, List<String>> m: id.getMatrixParameters().entrySet()){

        System.out.println("Key: " + m.getKey());

        for(String k : m.getValue()){
            System.out.println("Values: " + k);
        }


    }*/

    return "";
}


    /**
     * This Method returns the names of all the classes present in the mentioned ontology
     * @param ontologyName : The ontology whose classes are requested
     * @param context
     * @return : Returns a JSON formatted string of all the classes present in the mentioned ontology
     */
    public String getAllClasses( String ontologyName,
                                 ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModel model  = RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName).getOntModel();
        ExtendedIterator classItr =  model.listNamedClasses();
        Set<String> classSet = new HashSet<String>();
        while( classItr.hasNext() ){
            OntClass o = (OntClass) classItr.next();
            if( o.getURI() != null )
                classSet.add( o.getURI() );
        }
        return RestOntInterfaceUtil.getJSON( classSet, new TypeToken<List<String>>() {}.getType() );
    }


    public String getAllSubClassesOfaClass( String ontologyName,
                                            String allClasses,
                                            ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );

        String[] classes = allClasses.split(",");
        System.out.println("Value of classes: " + allClasses);

        if( classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new BadRequestException(exp) );
            throw new BadRequestException( exp );
        }

        Map<String, Set<String>> output = new HashMap<String, Set<String>>();
        for( String className : classes){
            System.out.println(className);
            OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className);
            Set<String> subClasses = RestOntInterfaceUtil.getSubClasses(ontClass);
            output.put( className, subClasses);
        }
        return RestOntInterfaceUtil.getJSON(output, new TypeToken< Map<String,List<String>> >() {}.getType());
    }



    public String getAllSuperClassesOfaClass( String ontologyName,
                                              String allClasses,
                                              ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        String[] classes = allClasses.split(",");
        System.out.println("Value of classes: " + allClasses);

        if(classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new BadRequestException(exp) );
            throw new BadRequestException( exp );
        }
        Map<String, Set<String>> output = new HashMap<String, Set<String>>();
        for( String className : classes){
            System.out.println(className);
            OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className);
            Set<String> superClasses = RestOntInterfaceUtil.getSuperClasses(ontClass);
            output.put( className, superClasses);
        }
        return RestOntInterfaceUtil.getJSON(output, new TypeToken< Map<String,List<String>> >() {}.getType());
    }


    public String getAllPropertiesOfaClass( String ontologyName,
                                            String allClasses,
                                            ServletContext context){
        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        String[] classes = allClasses.split(",");
        System.out.println("Value of classes: " + allClasses);
        if(classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new BadRequestException(exp) );
            throw new BadRequestException( exp );
        }
        Map<String, Set<String>> output = new HashMap<String, Set<String>>();
        for( String className : classes){
            OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className);
            Set<String> properties = RestOntInterfaceUtil.getProperties(ontClass);
            output.put( className, properties);
        }
        return RestOntInterfaceUtil.getJSON(output, new TypeToken< Map<String,List<String>> >() {}.getType());
    }


    public String getRestrictionValuesOnaClass( String ontologyName,
                                                String className,
                                                ServletContext context){
        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntClass ontClass = RestOntInterfaceUtil.
                getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className);
        ExtendedIterator superClassItr = ontClass.listSuperClasses(true);
        Map<String, List<String>> restrictionValueMap = new HashMap<String, List<String>>();
        while( superClassItr.hasNext() ){
            OntClass c = (OntClass) superClassItr.next();

            if( c.isRestriction()){

                Restriction r = c.as(Restriction.class);
                List<String> restrictionValues ;

                if( restrictionValueMap.containsKey(r.getOnProperty().getLocalName()) ){
                    restrictionValues = restrictionValueMap.get( r.getOnProperty().getLocalName() );
                }else{
                    restrictionValues = new ArrayList<String>();
                    restrictionValueMap.put( r.getOnProperty().getLocalName(), restrictionValues);
                }

                if(r.isAllValuesFromRestriction()){

                    AllValuesFromRestriction allValuesFromRestriction = r.asAllValuesFromRestriction();
                    if( allValuesFromRestriction.getAllValuesFrom() != null )
                        restrictionValues.add( allValuesFromRestriction.getAllValuesFrom().getLocalName() );
                }else if( r.isSomeValuesFromRestriction() ){
                    SomeValuesFromRestriction someValuesFromRestriction = r.asSomeValuesFromRestriction();
                    if( someValuesFromRestriction.getSomeValuesFrom() != null )
                        restrictionValues.add(someValuesFromRestriction.getSomeValuesFrom().getLocalName());
                }else

                if( r.isHasValueRestriction() ){

                    HasValueRestriction hasValueRestriction = r.asHasValueRestriction();
                    if( hasValueRestriction.getHasValue() != null ){
                        if( hasValueRestriction.getHasValue().isLiteral() ){
                            restrictionValues.add( hasValueRestriction.getHasValue().asLiteral().getValue().toString() );
                        }else if ( hasValueRestriction.getHasValue().isResource() ) {
                            restrictionValues.add( hasValueRestriction.getHasValue().asResource().getLocalName() );
                        }
                    }
                }else if( r.isCardinalityRestriction() ){

                    CardinalityRestriction cardinalityRestriction = r.asCardinalityRestriction();
                    restrictionValues.add( Integer.toString( cardinalityRestriction.getCardinality() ) );

                }else if ( r.isMaxCardinalityRestriction() ){

                    MaxCardinalityRestriction maxCardinalityRestriction = r.asMaxCardinalityRestriction();
                    restrictionValues.add( Integer.toString( maxCardinalityRestriction.getMaxCardinality() ) );

                }else if ( r.isMinCardinalityRestriction() ){

                    MinCardinalityRestriction minCardinalityRestriction = r.asMinCardinalityRestriction();
                    restrictionValues.add( Integer.toString( minCardinalityRestriction.getMinCardinality() ) );

                }
            }
        }

        Gson gsn = new Gson();
        Type mapType = new TypeToken< Map<String,List<String>> >() {}.getType();
        return gsn.toJson( restrictionValueMap, mapType );
    }


    public String getAllRestrictionsONaClass( String ontologyName,
                                              String className,
                                              ServletContext context){
        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntClass ontClass = RestOntInterfaceUtil.
                getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className);
        ExtendedIterator superClassItr = ontClass.listSuperClasses(true);
        Set<String> restrictions = new HashSet<String>();
        while( superClassItr.hasNext() ){
            OntClass c = (OntClass) superClassItr.next();

            if( c.isRestriction() ){
                Restriction r = c.as(Restriction.class);
                restrictions.add( r.getOnProperty().getLocalName() );
            }
        }

        return  RestOntInterfaceUtil.getJSON(restrictions, new TypeToken<Set<String>>() {
        }.getType());
    }

    public String getAllInstancesOfClass(  String ontologyName,
                                           String className,
                                           ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntClass ontClass = RestOntInterfaceUtil.
                getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className);
        Set<String> individuals = RestOntInterfaceUtil.getIndividuals(ontClass);
        return  RestOntInterfaceUtil.getJSON( individuals, new TypeToken<Set<String>>() {}.getType());
    }


    public String getAllEnumeratedClasses( String ontologyName,
                                            ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModel model = RestOntInterfaceUtil.getOntModel( ontologyModelStore, ontologyName ).getOntModel();
        Set<String> enumClassSet = new HashSet<String>();
        ExtendedIterator enumItr =  model.listEnumeratedClasses();
        while( enumItr.hasNext() ){
            enumClassSet.add( ( (EnumeratedClass) enumItr.next() ).as( OntClass.class ).getLocalName() );
        }
        return  RestOntInterfaceUtil.getJSON( enumClassSet, new TypeToken<Set<String>>() {}.getType());

    }

    public String getAllEnumeratedClassInstances( String ontologyName,
                                                  ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModel model = RestOntInterfaceUtil.getOntModel( ontologyModelStore, ontologyName ).getOntModel();
        Map<String, Set<String>> enumInstanceMap = new HashMap<String, Set<String>>();
        ExtendedIterator enumItr =  model.listEnumeratedClasses();
        while( enumItr.hasNext() ){
            EnumeratedClass enumClass = (EnumeratedClass) enumItr.next();
            ExtendedIterator instanceItr = enumClass.listOneOf();
            Set<String> enumClassSet = new HashSet<String>();
            while( instanceItr.hasNext() ){
                enumClassSet.add(((OntResource) instanceItr.next()).getLocalName());
            }
            enumInstanceMap.put(enumClass.as( OntClass.class).getLocalName(), enumClassSet );
        }
        Gson gsn = new Gson();
        Type mapType = new TypeToken< Map<String,List<String>> >() {}.getType();
        return gsn.toJson( enumInstanceMap, mapType );
    }

    public String getInstancesOfEnumeratedClass( String ontologyName,
                                                 String enumClass,
                                                  ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntClass enumOntClass = RestOntInterfaceUtil.
                getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), enumClass);
        if( !enumOntClass.isEnumeratedClass() ){
            String exp =  enumClass + " is not an enumerated class in " + ontologyName + " ontology ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new BadRequestException(exp) );
            throw new BadRequestException( exp );
        }
        Set<String> enumClassSet = new HashSet<String>();
        ExtendedIterator enumItr =  enumOntClass.as(EnumeratedClass.class).listOneOf();
        while( enumItr.hasNext() ){
            enumClassSet.add( ( (EnumeratedClass) enumItr.next() ).as( OntClass.class ).getLocalName() );
        }
        return RestOntInterfaceUtil.getJSON( enumClassSet, new TypeToken<Set<String>>() {}.getType());
    }

    public String getDomainOfProperty( String ontologyName,
                                       String propertyName,
                                        ServletContext context){
        OntModelWrapper ontModelWrapper = RestOntInterfaceUtil.getOntModel( (OntologyModelStore)context.getAttribute("ontologyModelStore"), ontologyName );
        OntProperty ontProperty = ontModelWrapper.getOntModel().getOntProperty( ontModelWrapper.getURI() + "#" + propertyName);

        if( ontProperty == null){
            String exp =  propertyName + " does not exist in " + ontologyName + " ontology ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new BadRequestException(exp) );
            throw new BadRequestException( exp );
        }

        String domain = ontProperty.getDomain().getURI();
        Gson gsn = new Gson();
        return gsn.toJson( domain );
    }


    public String deleteSubClasses( String ontologyName,
                                    String allClasses,
                                    ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );

        String[] classes = allClasses.split(",");
        System.out.println("Value of classes: " + allClasses);

        if( classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new BadRequestException(exp) );
            throw new BadRequestException( exp );
        }

        Set<OntClass> subClassSet = new HashSet<OntClass>();
        for( String className : classes){
            System.out.println(className);
            OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className);
            ExtendedIterator subClassItr = ontClass.listSubClasses( true );

             while( subClassItr.hasNext() ){
                    OntClass subClass = (OntClass) subClassItr.next();
                    if( subClass.getLocalName() != null )
                        subClassSet.add( subClass );
             }
        }

        deleteClasses( subClassSet );
        return "";
    }

    public String deleteSuperClasses( String ontologyName,
                                      String allClasses,
                                      ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );

        String[] classes = allClasses.split(",");
        System.out.println("Value of classes: " + allClasses);

        if( classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new BadRequestException(exp) );
            throw new BadRequestException( exp );
        }

        Set<OntClass> subClassSet = new HashSet<OntClass>();
        for( String className : classes){
            System.out.println(className);
            OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className);
            ExtendedIterator subClassItr = ontClass.listSuperClasses(true);

             while( subClassItr.hasNext() ){
                    OntClass subClass = (OntClass) subClassItr.next();
                    if( subClass.getLocalName() != null )
                        subClassSet.add( subClass );
             }
        }

        deleteClasses( subClassSet );
        return "";
    }

    private void deleteClasses(Set<OntClass> subClassSet) {

        for (OntClass ontClass : subClassSet) {
            ontClass.remove();
        }
    }


    /**
     * Method to create Freemarker Datamodel for displaying.
     * @param ontologyModelStore : the ontologyModelStore object reference
     * @param dataModel: DataModel that has to be written to the outputStream
     */
    private void  getOntologyDataModel( OntologyModelStore ontologyModelStore, Map<String, Object> dataModel ) {

        LinkedList< LinkedList<String> > ontologyInfo = new LinkedList<LinkedList<String>>();
        dataModel.put("ontologyInfo", ontologyInfo);
        Map<String, OntModelWrapper> ontModelWrapperMap = ontologyModelStore.getOntModelSet();

        for( Map.Entry<String, OntModelWrapper> e : ontModelWrapperMap.entrySet() ){
                LinkedList< String > ontInfo = new LinkedList<String>();
                                     ontInfo.add( 0, e.getValue().getOntologyName() );
                                     ontInfo.add( 1, e.getValue().getURI() );
                ontologyInfo.add(ontInfo);

        }
    }
}
