package edu.uga.cs.restendpoint.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.*;
/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 10/16/11
 * Time: 10:59 PM
 * Email: <kale@cs.uga.edu>
 */

@Path("/schemainfo")
public class SchemaInfoService {

    @GET
    @Path("index")
    @Produces("text/html")
    public void getInfo( @Context ServletContext servletContext,
                         @Context HttpServletRequest req,
                         @Context HttpServletResponse res ){

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
            dataModel = createAboutTopicDataModel("browser");
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
    }

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


    private Map<String, Object> createAboutTopicDataModel(String topicName)
    {
        Map<String, Object> dataModel = null;

        dataModel = new HashMap<String, Object>();


        dataModel.put( "topicname", topicName );

        return dataModel;

    }

    @GET
    @Path("{ontologyName}/classes")
    public String getAllClasses(@PathParam("ontologyName") String ontologyName,
                                 @Context ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper ontModelWrapper = ontologyModelStore.getOntologyModel( ontologyName );

        //TODO: Throw appropriate exception and return HTTP code.
        if( ontModelWrapper == null){
            System.out.println( ontologyName +  " is not loaded in the server" );
            return "";
        }
        OntModel model = ontModelWrapper.getOntModel();
        ExtendedIterator classItr =  model.listNamedClasses();
        List<String> classList = new ArrayList<String>();
        while( classItr.hasNext() ){
            OntClass o = (OntClass) classItr.next();
            if( o.getURI() != null )
                classList.add( o.getURI() );
        }

        Gson gsn = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gsn.toJson( classList, listType );
    }

    @GET
    @Path("{ontologyName}/{className}/subClasses")
    public String getAllSubClassesOfaClass( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("className") String className,
                                            @Context ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntClass ontClass = RestOntInterfaceUtil.getClass(ontologyName, className, ontologyModelStore);
        if(ontClass == null){
            System.out.println( className +  " does not exist in " + ontologyName + " ontology");
            return "";

        }

        List<String> subClasses = new ArrayList<String>();
        ExtendedIterator subClassItr = ontClass.listSubClasses( true );

        while( subClassItr.hasNext() ){
            OntClass subClass = (OntClass) subClassItr.next();
            if( subClass.getLocalName() != null )
                subClasses.add( subClass.getLocalName() );

        }
        Gson gsn = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gsn.toJson( subClasses, listType );
    }


    @GET
    @Path("{ontologyName}/{className}/superClasses")
    public String getAllSuperClassesOfaClass( @PathParam("ontologyName") String ontologyName,
                                              @PathParam("className") String className,
                                              @Context ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntClass ontClass = RestOntInterfaceUtil.getClass(ontologyName, className, ontologyModelStore);
        if(ontClass == null){
            System.out.println( className +  " does not exist in " + ontologyName + " ontology");
            return "";

        }

        List<String> superClasses = new ArrayList<String>();
        ExtendedIterator superClassItr = ontClass.listSuperClasses(true);

        while( superClassItr.hasNext() ){
            OntClass superClass = (OntClass) superClassItr.next();
            if( superClass.getLocalName() != null )
                superClasses.add( superClass.getLocalName() );

        }
        Gson gsn = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gsn.toJson( superClasses, listType );
    }

    @GET
    @Path("{ontologyName}/{className}/properties")
    public String getAllPropertiesOfaClass( @PathParam("ontologyName") String ontologyName,
                                            @PathParam("className") String className,
                                            @Context ServletContext context){
        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntClass ontClass = RestOntInterfaceUtil.getClass(ontologyName, className, ontologyModelStore);

        ExtendedIterator propItr = ontClass.listDeclaredProperties();
        List<String> properties = new ArrayList<String>();

        while( propItr.hasNext() ){
            OntProperty property = (OntProperty) propItr.next();
            properties.add( property.getLocalName() );
        }
        Gson gsn = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gsn.toJson( properties, listType );
    }

    @GET
    @Path("{ontologyName}/{className}/restrictionValues")
    public String getRestrictionValuesOnaClass( @PathParam("ontologyName") String ontologyName,
                                                @PathParam("className") String className,
                                                @Context ServletContext context){
        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntClass ontClass = RestOntInterfaceUtil.getClass(ontologyName, className, ontologyModelStore);
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


    @GET
    @Path("{ontologyName}/{className}/restrictions")
    public String getAllRestrictionsONaClass( @PathParam("ontologyName") String ontologyName,
                                              @PathParam("className") String className,
                                              @Context ServletContext context){
        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntClass ontClass =RestOntInterfaceUtil. getClass(ontologyName, className, ontologyModelStore);
        ExtendedIterator superClassItr = ontClass.listSuperClasses(true);
        Set<String> restrictions = new HashSet<String>();
        while( superClassItr.hasNext() ){
            OntClass c = (OntClass) superClassItr.next();

            if( c.isRestriction() ){
                Restriction r = c.as(Restriction.class);
                restrictions.add( r.getOnProperty().getLocalName() );
            }
        }

        Gson gsn = new Gson();
        Type setType = new TypeToken<Set<String>>() {}.getType();
        return gsn.toJson( restrictions, setType );
    }

    @GET
    @Path("{ontologyName}/{className}/individuals")
    public String getAllIndividualsOfClass(  @PathParam("ontologyName") String ontologyName,
                                             @PathParam("className") String className,
                                             @Context ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntClass ontClass = RestOntInterfaceUtil.getClass(ontologyName, className, ontologyModelStore);
        ExtendedIterator individualItr = ontClass.listInstances();
        Set<String> individuals = new HashSet<String>();

        while( individualItr.hasNext() ){
            Individual ind = (Individual) individualItr.next();
            if( ind.getOntClass().getLocalName() != null ){
                individuals.add( ind.getOntClass().getLocalName() );
            }
        }
        Gson gsn = new Gson();
        Type setType = new TypeToken<Set<String>>() {}.getType();
        return gsn.toJson( individuals, setType );
    }

    @GET
    @Path("{ontologyName}/enumeratedClasses")
    public String getAllEnumeratedClasses( @PathParam("ontologyName") String ontologyName,
                                           @Context ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModel model = ontologyModelStore.getOntologyModel( ontologyName ).getOntModel();
        if(model == null ){
            System.out.println(ontologyName + " is not loaded in the server");
            return "";
        }
        Set<String> enumClassSet = new HashSet<String>();
        ExtendedIterator enumItr =  model.listEnumeratedClasses();
        while( enumItr.hasNext() ){
            enumClassSet.add( ( (EnumeratedClass) enumItr.next() ).as( OntClass.class ).getLocalName() );
        }
        Gson gsn = new Gson();
        Type setType = new TypeToken<Set<String>>() {}.getType();
        return gsn.toJson( enumClassSet, setType );

    }

    @GET
    @Path("{ontologyName}/enumeratedClassInstances")
    public String getAllEnumeratedClassInstances( @PathParam("ontologyName") String ontologyName,
                                           @Context ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModel model = ontologyModelStore.getOntologyModel( ontologyName ).getOntModel();
        if(model == null ){
            System.out.println(ontologyName + " is not loaded in the server");
            return "";
        }
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

    @GET
    @Path("{ontologyName}/{enumClass}/enumIndividuals")
    public String getInstancesOfEnumeratedClass( @PathParam("ontologyName") String ontologyName,
                                                 @PathParam("enumClass") String enumClass,
                                                 @Context ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntClass enumOntClass = RestOntInterfaceUtil.getClass( ontologyName, enumClass, ontologyModelStore);
        if( !enumOntClass.isEnumeratedClass() ){
            System.out.println( enumClass + " is not an enumerated class");
            return "";
        }
        Set<String> enumClassSet = new HashSet<String>();
        ExtendedIterator enumItr =  enumOntClass.as(EnumeratedClass.class).listOneOf();
        while( enumItr.hasNext() ){
            enumClassSet.add( ( (EnumeratedClass) enumItr.next() ).as( OntClass.class ).getLocalName() );
        }
        Gson gsn = new Gson();
        Type setType = new TypeToken<Set<String>>() {}.getType();
        return gsn.toJson( enumClassSet, setType );

    }
}
