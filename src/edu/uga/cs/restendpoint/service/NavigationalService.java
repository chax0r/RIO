package edu.uga.cs.restendpoint.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import edu.uga.cs.restendpoint.utils.OntModelWrapper;
import edu.uga.cs.restendpoint.utils.OntologyModelStore;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 10/16/11
 * Time: 12:10 AM
 * Email: <kale@cs.uga.edu>
 */
@Path("/navigate")
public class NavigationalService {

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
    @Path("{ontologyName}/class/{className}/subClasses")
        public String getAllSubClassesOfaClass( @PathParam("ontologyName") String ontologyName,
                                                @PathParam("className") String className,
                                                 @Context ServletContext context){
        OntClass ontClass = getClass(ontologyName, className, context);
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
    @Path("{ontologyName}/class/{className}/superClasses")
        public String getAllSuperClassesOfaClass( @PathParam("ontologyName") String ontologyName,
                                                @PathParam("className") String className,
                                                 @Context ServletContext context){

        OntClass ontClass = getClass(ontologyName, className, context);
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
    @Path("{ontologyName}/class/{className}/properties")
        public String getAllPropertiesOfaClass( @PathParam("ontologyName") String ontologyName,
                                                @PathParam("className") String className,
                                                @Context ServletContext context){
        OntClass ontClass = getClass(ontologyName, className, context);

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
    @Path("{ontologyName}/class/{className}/restrictionsValues")
        public String getRestrictionValuesOnaClass( @PathParam("ontologyName") String ontologyName,
                                                @PathParam("className") String className,
                                                @Context ServletContext context){
        OntClass ontClass = getClass(ontologyName, className, context);
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
                        restrictionValues.add( allValuesFromRestriction.getAllValuesFrom().getLocalName() );
                    }

                    if( r.isSomeValuesFromRestriction() ){
                        SomeValuesFromRestriction someValuesFromRestriction = r.asSomeValuesFromRestriction();
                        restrictionValues.add(someValuesFromRestriction.getSomeValuesFrom().getLocalName());
                    }

                    if( r.isCardinalityRestriction() ){
                        CardinalityRestriction cardinalityRestriction = r.asCardinalityRestriction();
                        restrictionValues.add( Integer.toString( cardinalityRestriction.getCardinality() ) );
                    }


                }
        }

        Gson gsn = new Gson();
        Type mapType = new TypeToken< Map<String,List<String>> >() {}.getType();
        return gsn.toJson( restrictionValueMap, mapType );
    }


    @GET
    @Path("{ontologyName}/class/{className}/restrictions")
        public String getAllRestrictionsONaClass( @PathParam("ontologyName") String ontologyName,
                                                @PathParam("className") String className,
                                                @Context ServletContext context){
        OntClass ontClass = getClass(ontologyName, className, context);
        ExtendedIterator superClassItr = ontClass.listSuperClasses(true);
        List<String> restrictions = new ArrayList<String>();
        while( superClassItr.hasNext() ){
                OntClass c = (OntClass) superClassItr.next();

                if( c.isRestriction() ){
                    Restriction r = c.as(Restriction.class);
                    restrictions.add( r.getOnProperty().getLocalName() );
                }
        }

        Gson gsn = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gsn.toJson( restrictions, listType );
    }

    private OntClass getClass(String ontologyName, String className, ServletContext context) {
        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper ontModelWrapper = ontologyModelStore.getOntologyModel( ontologyName );

        //TODO: Throw appropriate exception and return HTTP code.
        if( ontModelWrapper == null){
               System.out.println( ontologyName +  " is not loaded in the server" );
               return null;
        }
        OntModel model = ontModelWrapper.getOntModel();
        OntClass ontClass = model.getOntClass( ontModelWrapper.getURI() + "#" + className);

        if(ontClass == null){
            System.out.println( className +  " does not exist in " + ontologyName + " ontology");
            return null;

        }
        return ontClass;
    }
}
