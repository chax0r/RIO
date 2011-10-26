package edu.uga.cs.restendpoint.utils;

import com.google.gson.Gson;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;
import org.jboss.resteasy.spi.BadRequestException;

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

    public static OntClass getClass( OntModelWrapper ontModelWrapper, String className ) {

        System.out.println("Looking for " + ontModelWrapper.getURI() +"#"+className);
        OntClass ontClass = ontModelWrapper.getOntModel().getOntClass(ontModelWrapper.getURI() + "#" + className);

        if(ontClass == null){
            String exp =  className +  " does not exist in " + ontModelWrapper.getOntologyName() + " ontology";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new BadRequestException(exp) );
            throw new BadRequestException( exp );
        }
        return ontClass;
    }



    public static OntModelWrapper getOntModel(OntologyModelStore ontologyModelStore, String ontologyName) {

        OntModelWrapper ontModelWrapper = ontologyModelStore.getOntologyModel(ontologyName);

        //TODO: Throw appropriate exception and return HTTP code.
        if( ontModelWrapper == null){
            String exp = ontologyName +  " is not loaded in the server";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new BadRequestException(exp) );
            throw new BadRequestException( exp );
        }
        return ontModelWrapper;
    }

    public  static Set<String> getSubClasses(OntClass ontClass) {
        Set<String> subClasses = new HashSet<String>();
        ExtendedIterator subClassItr = ontClass.listSubClasses( true );

        while( subClassItr.hasNext() ){
            OntClass subClass = (OntClass) subClassItr.next();
            if( subClass.getLocalName() != null )
                subClasses.add( subClass.getLocalName() );

        }
        return subClasses;
    }


    public static Set<String> getSuperClasses(OntClass ontClass) {
        Set<String> superClasses = new HashSet<String>();
        ExtendedIterator superClassItr = ontClass.listSuperClasses(true);

        while( superClassItr.hasNext() ){
            OntClass superClass = (OntClass) superClassItr.next();
            if( superClass.getLocalName() != null )
                superClasses.add( superClass.getLocalName() );

        }
        return superClasses;
    }



    public static Set<String> getProperties( OntClass ontClass) {
        ExtendedIterator propItr = ontClass.listDeclaredProperties();
        Set<String> properties = new HashSet<String>();

        while( propItr.hasNext() ){
            OntProperty property = (OntProperty) propItr.next();
            properties.add( property.getLocalName() );
        }
        return properties;
    }


    public static Set<String> getIndividuals(OntClass ontClass) {
        ExtendedIterator individualItr = ontClass.listInstances();
        Set<String> individuals = new HashSet<String>();

        while( individualItr.hasNext() ){
            Individual ind = (Individual) individualItr.next();
            if( ind.getOntClass().getLocalName() != null ){
                individuals.add( ind.getOntClass().getLocalName() );
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
}
