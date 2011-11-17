package edu.uga.cs.restendpoint.utils;

import com.google.gson.Gson;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;
import edu.uga.cs.restendpoint.exceptions.NotFoundException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author: kale
 * Date: 10/16/11
 * Time: 10:59 PM
 * Email: <kale@cs.uga.edu>
 */
public class RestOntInterfaceUtil {

    public static OntClass getClass( OntModelWrapper ontModelWrapper, String className, boolean throwException ) {

        Map<String, String> namespaceMap = ontModelWrapper.getOntModel().getNsPrefixMap();

        OntClass ontClass = null;

        for( String namespace : namespaceMap.values() ){

            ontClass = ontModelWrapper.getOntModel().getOntClass( namespace + className );
            if( ontClass != null )
                break;

        }

        if(throwException && ontClass == null){
            String exp =  className +  " does not exist in " + ontModelWrapper.getOntologyName() + " ontology";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException( exp ) );
            throw new NotFoundException( exp );
        }
        return ontClass;
    }

    public static Individual getIndividual( OntModelWrapper ontModelWrapper, String indName, boolean throwException ) {

        Map<String, String> namespaceMap = ontModelWrapper.getOntModel().getNsPrefixMap();

        Individual ind = null;

        for( String namespace : namespaceMap.values() ){

            ind = ontModelWrapper.getOntModel().getIndividual( namespace + indName );
            if( ind != null )
                break;

        }

        if( throwException && ind == null){
            String exp =  indName +  " does not exist in " + ontModelWrapper.getOntologyName() + " ontology";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException( exp ) );
            throw new NotFoundException( exp );
        }
        return ind;
    }



    public static OntModelWrapper getOntModel(OntologyModelStore ontologyModelStore, String ontologyName) {

        OntModelWrapper ontModelWrapper = ontologyModelStore.getOntologyModel(ontologyName);

        //TODO: Throw appropriate exception and return HTTP code.
        if( ontModelWrapper == null){
            String exp = ontologyName +  " is not loaded in the server";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException( exp ));
            throw new NotFoundException( exp );
        }
        return ontModelWrapper;
    }

    public  static Set<String> getSubClasses(OntClass ontClass) {
        Set<String> subClasses = new HashSet<String>();
        ExtendedIterator <OntClass>subClassItr = ontClass.listSubClasses( true );

        while( subClassItr.hasNext() ){
            OntClass subClass = subClassItr.next();
            if( subClass.getLocalName() != null )
                subClasses.add( subClass.getLocalName() );

        }
        return subClasses;
    }


    public static Set<String> getSuperClasses(OntClass ontClass) {
        Set<String> superClasses = new HashSet<String>();
        ExtendedIterator <OntClass> superClassItr = ontClass.listSuperClasses(true);

        while( superClassItr.hasNext() ){
            OntClass superClass =  superClassItr.next();
            if( superClass.getLocalName() != null )
                superClasses.add( superClass.getLocalName() );

        }
        return superClasses;
    }



    public static Set<String> getProperties( OntClass ontClass) {
        ExtendedIterator <OntProperty> propItr = ontClass.listDeclaredProperties();
        Set<String> properties = new HashSet<String>();

        while( propItr.hasNext() ){
            OntProperty property =  propItr.next();
            properties.add( property.getLocalName() );
        }
        return properties;
    }


    public static Set<String> getIndividuals(OntClass ontClass) {
        ExtendedIterator <? extends OntResource> individualItr = ontClass.listInstances();
        Set<String> individuals = new HashSet<String>();

        while( individualItr.hasNext() ){
            Individual ind = individualItr.next().as( Individual.class );
            if( ind.getURI() != null ){
                individuals.add( ind.getURI() );
            }
        }
        return individuals;
    }

    public static String getJSON(Object strings, Type type) {
        Gson gsn = new Gson();
        return gsn.toJson( strings, type );
    }

    public static void log( String className){
            Logger.getLogger(className).log(Level.SEVERE, null);
    }

    public static void log( String className, Exception ex){
            Logger.getLogger(className).log(Level.SEVERE, null, ex);
    }
    public static void log( String className, String msg){
        Logger.getLogger(className).log(Level.INFO, msg);
    }

    public static OntProperty getProperty(OntModelWrapper ontModelWrapper , String property, boolean throwException) {

        Map<String, String> namespaceMap = ontModelWrapper.getOntModel().getNsPrefixMap();

        OntProperty ontProperty = null;

        for( String namespace : namespaceMap.values() ){

            ontProperty = ontModelWrapper.getOntModel().getOntProperty( namespace + property );
            if( ontProperty != null )
                break;

        }

        if( throwException && ontProperty == null){
            String exp =  property +  " does not exist in " + ontModelWrapper.getOntologyName() + " ontology";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException( exp ) );
            throw new NotFoundException( exp );
        }
        return ontProperty;
    }
}
