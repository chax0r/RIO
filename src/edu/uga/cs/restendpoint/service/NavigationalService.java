package edu.uga.cs.restendpoint.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceConstants;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceUtil;
import org.jboss.resteasy.spi.BadRequestException;

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
    @Path("{ontologyName}/class/{className}/{associationsQuery:.+}")
    public String navigateOntology(@PathParam("ontologyName") String ontologyName,
                                   @PathParam("className") String className,
                                   @PathParam("associationQuery") String associationsQuery,
                                    @Context ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper modelWrapper = ontologyModelStore.getOntologyModel(ontologyName);
        OntClass ontClass = RestOntInterfaceUtil. getClass(ontologyName, className, ontologyModelStore);
        Set<OntClass> interimClasses = new HashSet<OntClass>();
        interimClasses.add( ontClass );
        String[] associations = associationsQuery.split("/");

        for( String association : associations){

            if( RestOntInterfaceConstants.SUPERCLASS.compareToIgnoreCase( association ) == 0 ){

                Set<OntClass> result = getAllSuperClass(interimClasses);
                if( result == null || result.isEmpty()){
                    break;
                }
                interimClasses.clear();
                interimClasses = result;

            }else if( RestOntInterfaceConstants.SUBCLASS.compareToIgnoreCase( association ) == 0 ){

                Set<OntClass> result = getAllSubClass(interimClasses);
                if( result == null || result.isEmpty()){
                    break;
                }
                interimClasses.clear();
                interimClasses = result;


            }else if( RestOntInterfaceConstants.EQUIVALENT.compareToIgnoreCase( association ) == 0 ){

                Set<OntClass> result = getEquivalentClass( interimClasses );
                if( result == null || result.isEmpty()){
                    break;
                }
                interimClasses.clear();
                interimClasses = result;


            }else if( RestOntInterfaceConstants.DISJOINT.compareToIgnoreCase( association ) == 0 ){

                Set<OntClass> result = getDisjointClass(interimClasses);
                if( result == null || result.isEmpty()){
                    break;
                }
                interimClasses.clear();
                interimClasses = result;


            }else if ( RestOntInterfaceConstants.COMPLEMENT.compareToIgnoreCase( association ) == 0 ) {

                Set<OntClass> result = getComplementOfClass(interimClasses);
                if( result == null || result.isEmpty()){
                    break;
                }
                interimClasses.clear();
                interimClasses = result;

            }else if ( RestOntInterfaceConstants.UNION.compareToIgnoreCase( association ) == 0 ) {

                Set<OntClass> result = getUnionClasses(interimClasses);
                if( result == null || result.isEmpty()){
                    break;
                }
                interimClasses.clear();
                interimClasses = result;

            }else if( RestOntInterfaceConstants.DISJOINT.compareToIgnoreCase( association ) == 0){
//                Set<OntClass> result = getUnionClasses( interimClasses );
                
            }else{
                    OntProperty ontProperty = modelWrapper.getOntModel().
                                                    getOntProperty(modelWrapper.getURI() +
                                                            "#" + association);

                    if( ontProperty == null ){
                        System.out.println( association + "is not present in " + ontologyName + " ontology");
                       // throw new BadRequestException();
                    }

                    Set<OntClass> result = processProperty( interimClasses, ontProperty );
                    if( result == null || result.isEmpty()){
                        break;
                    }
                    interimClasses.clear();
                    interimClasses = result;

            }
        }

        return convertToJson(interimClasses);
    }

    private Set<OntClass> getUnionClasses(Set<OntClass> ontClassSet) {
        Set<OntClass> ontClasses = new HashSet<OntClass>();
        for( OntClass cls : ontClassSet ){

        }

        return ontClasses ;
    }

    private Set<OntClass> getDisjointClass( Set<OntClass> ontClassSet ) {
        Set<OntClass> ontClasses = new HashSet<OntClass>();
        for( OntClass ontClass : ontClassSet ){
            ExtendedIterator classItr = ontClass.listDisjointWith();
            while( classItr.hasNext() ){
                ontClasses.add( (OntClass)classItr.next() );
            }
        }
        return ontClasses;
    }

    private Set<OntClass> getEquivalentClass( Set<OntClass> ontClassSet ) {

        Set<OntClass> ontClasses = new HashSet<OntClass>();
        for( OntClass ontClass : ontClassSet){
            ExtendedIterator classItr = ontClass.listEquivalentClasses();
            while( classItr.hasNext() ){
                ontClasses.add( (OntClass)classItr.next() );
            }
        }
        return ontClasses;
    }

    private Set<OntClass> getComplementOfClass(Set<OntClass> ontClassSet ) {
        Set<OntClass> ontClasses = new HashSet<OntClass>();
        for( OntClass ontClass : ontClassSet){

            if( ontClass.isComplementClass() ){

                ontClassSet.add(ontClass.asComplementClass().getOperand());
            }
        }
        return ontClasses;
    }

    private Set<OntClass> getAllSubClass( Set<OntClass> ontClassSet ) {

        Set<OntClass> ontClasses = new HashSet<OntClass>();
        for( OntClass ontClass : ontClassSet){

            ExtendedIterator subClassItr = ontClass.listSubClasses( false );

            while( subClassItr.hasNext() ){
                ontClasses.add( (OntClass) subClassItr.next() );
            }
        }
        return ontClasses;
    }

    private Set<OntClass> getAllSuperClass( Set<OntClass> ontClassSet ) {
        Set<OntClass> ontClasses = new HashSet<OntClass>();

        for( OntClass ontClass : ontClassSet ){
            ExtendedIterator superClassItr = ontClass.listSuperClasses( false );

            while( superClassItr.hasNext() ){
                ontClasses.add( (OntClass) superClassItr.next() );
            }
        }
        return ontClasses;
    }

    private String convertToJson(Set<OntClass> interimClasses) {
        Gson gsn = new Gson();
        Type setType = new TypeToken<Set<String>>() {}.getType();
        return gsn.toJson( interimClasses, setType );
    }

    private Set<OntClass> processProperty(Set<OntClass> ontClasses, OntProperty association) {

        Set<OntClass> resultClasses = new HashSet<OntClass>();



        for( OntClass cls : ontClasses ){

            //TODO: Do we need to check if its a domain/range
            // What info we will get if the given ontClass is a domain/range of the given association

            if( association.hasDomain( cls.asResource() ) ){

            }

            Set<OntClass> interim = find( cls, association );
            if( interim!=null && !interim.isEmpty() ){
                resultClasses.addAll( interim );

            }
        }
        return resultClasses;
    }

    private Set<OntClass> find(OntClass cls, OntProperty association) {

         Set<OntClass> resultClasses = new HashSet<OntClass>();
         ExtendedIterator superClassItr = cls.listSuperClasses( false );
            while( superClassItr.hasNext() ){
                OntClass c = (OntClass) superClassItr.next();

                if( c.isRestriction()){
                    Restriction r = c.as(Restriction.class);

                    if( r.getOnProperty().getURI().compareToIgnoreCase( association.getURI() ) == 0)
                        System.out.println("Restriction on : "+ r.getOnProperty().getLocalName() );

                        if(r.isAllValuesFromRestriction()){

                            AllValuesFromRestriction allValuesFromRestriction = r.asAllValuesFromRestriction();
                            Resource res = allValuesFromRestriction.getAllValuesFrom();
                            resultClasses.add(res.as(OntClass.class));

                        }else if( r.isSomeValuesFromRestriction() ){

                            SomeValuesFromRestriction someValuesFromRestriction = r.asSomeValuesFromRestriction();
                            Resource res = someValuesFromRestriction.getSomeValuesFrom();
                            resultClasses.add( res.as(OntClass.class) );

                        }else if( r.isHasValueRestriction() ){

                            HasValueRestriction hasValueRestriction = r.asHasValueRestriction();
                            if( hasValueRestriction.getHasValue() != null &&
                                    hasValueRestriction.getHasValue().isResource()){

                                Resource res = hasValueRestriction.getHasValue().asResource();
                                resultClasses.add( res.as(OntClass.class) );

                            }else{

                                System.out.println(" Has value restriction for " + association);

                            }
                        }
                    }

            }
        return resultClasses;
    }


}
