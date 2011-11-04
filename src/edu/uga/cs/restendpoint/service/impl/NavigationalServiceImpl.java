package edu.uga.cs.restendpoint.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;
import edu.uga.cs.restendpoint.service.api.NavigationalService;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceConstants;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceUtil;
import org.jboss.resteasy.spi.BadRequestException;
import sun.jvm.hotspot.oops.Array;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Author: kale
 * Date: 10/16/11
 * Time: 12:10 AM
 * Email: <kale@cs.uga.edu>
 */
@Path("/navigate")
public class NavigationalServiceImpl implements NavigationalService {

    public String navigateOntologyClasses( String ontologyName,
                                    List<PathSegment> associationsQuery,
                                    ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper modelWrapper = RestOntInterfaceUtil.getOntModel( ontologyModelStore, ontologyName);
        System.out.println("Path query: " + associationsQuery );
        //String[] path = associationsQuery.split("/");
        Set<OntResource> initResources = new HashSet<OntResource>();
        OntClass ontClass = modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() + "#" + associationsQuery.get(0) );
        if( ontClass != null ){
            initResources.add( ontClass.as(OntResource.class) );
            associationsQuery = associationsQuery.subList(1, associationsQuery.size() );
        }else{
            OntProperty ontProperty = modelWrapper.getOntModel().getOntProperty( modelWrapper.getURI() + "#" + associationsQuery.get(0) );
            if( ontProperty != null ){

                ExtendedIterator classItr = ontProperty.listDeclaringClasses(false);
                while( classItr.hasNext() ){
                    OntClass cls = (OntClass) classItr.next();
                    if( cls.getURI() != null ){
                        initResources.add( cls.as(OntResource.class) );
                    }
                }

            }else{
                //TODO: Throw exception
            }
        }
        return executePathQuery( modelWrapper, associationsQuery, initResources, true);

    }

    public String navigateOntologyInstances ( String ontologyName,
                                              List<PathSegment> associationsQuery,
                                              ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper modelWrapper = RestOntInterfaceUtil.getOntModel( ontologyModelStore, ontologyName);
        //String[] path = associationsQuery.split("/");
        //System.out.println("Path query: " + associationsQuery );


        Set<OntResource> initResources = new HashSet<OntResource>();
        Individual initIndividual = ontologyModelStore.getOntologyModel( ontologyName ).
                getOntModel().getIndividual( ontologyModelStore.
                                             getOntologyModel(ontologyName).
                                             getURI() + "#" + associationsQuery.get(0) );
        if( initIndividual != null ){

            if( !initIndividual.isIndividual() ){
                String exp =  associationsQuery.get(0) + "is not an instance in " + modelWrapper.getOntologyName() + " ontology";
                RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new BadRequestException(exp) );
                throw new BadRequestException( exp );

            }

            initResources.add( initIndividual.as(OntResource.class) );
            associationsQuery = associationsQuery.subList(1, associationsQuery.size() );

        }else{
            //Treat path[0] as a property
              OntProperty initProperty = ontologyModelStore.getOntologyModel( ontologyName ).
                                                                getOntModel().getOntProperty( ontologyModelStore.
                                                                                              getOntologyModel(ontologyName).
                                                                                              getURI() + "#" + associationsQuery.get(0)  );
              if( initProperty == null ){
                  //TODO: throw exception;
                  return "";
              }else{
                  //Get all the classes that declare this property.
                  //From the classes, get all the individuals
                 ExtendedIterator classItr =  initProperty.listDeclaringClasses( false );
                 while ( classItr.hasNext() ){
                    ExtendedIterator indItr =  ( (OntClass) classItr.next() ).listInstances( false );

                    while( indItr.hasNext() )
                        initResources.add( ( (Individual)indItr.next() ).as( OntResource.class) );
                 }
              }
        }
        return executePathQuery(  modelWrapper, associationsQuery, initResources, false);
    }


    public String executePathQuery( OntModelWrapper modelWrapper,
                                    List<PathSegment> path, Set<OntResource> interimResults, Boolean areClasses) {

      /*  for (String s : path) {
            System.out.println(s);
        }*/



      //  Set<OntClass> interimClasses = getInitialClasses( modelWrapper, path[0]);

        for( PathSegment association : path){

            if( RestOntInterfaceConstants.SUPERCLASS.compareToIgnoreCase( association.getPath() ) == 0 ){

                if( areClasses.equals( false )){
                    //TODO: throw exception
                    return "";
                }

                Set<OntResource> result = getAllSuperClass(interimResults);
                if( result == null || result.isEmpty()){
                    break;
                }
                interimResults.clear();
                interimResults = result;

            }else if( RestOntInterfaceConstants.SUBCLASS.compareToIgnoreCase( association.getPath() ) == 0 ){

                if( areClasses.equals( false )){
                    //TODO: throw exception
                    return "";
                }
                Set<OntResource> result = getAllSubClass( interimResults );
                if( result == null || result.isEmpty()){
                    break;
                }
                interimResults.clear();
                interimResults = result;


            }else if( RestOntInterfaceConstants.EQUIVALENT.compareToIgnoreCase( association.getPath() ) == 0 ){

                if( areClasses.equals( false )){
                    //TODO: throw exception
                    return "";
                }
                Set<OntResource> result = getEquivalentClass( interimResults );
                if( result == null || result.isEmpty()){
                    break;
                }
                interimResults.clear();
                interimResults = result;


            }else if( RestOntInterfaceConstants.DISJOINT.compareToIgnoreCase( association.getPath() ) == 0 ){

                if( areClasses.equals( false )){
                    //TODO: throw exception
                    return "";
                }
                Set<OntResource> result = getDisjointClass(interimResults);
                if( result == null || result.isEmpty()){
                    break;
                }
                interimResults.clear();
                interimResults = result;


            }else if( RestOntInterfaceConstants.INSTANCEOF.compareToIgnoreCase( association.getPath() ) == 0){

                if( areClasses.equals( false ) ){
                    //TODO: throw exception
                    return "";
                }
                areClasses = false;
                Set<OntResource> result = getIndividuals( interimResults);
                if( result == null || result.isEmpty()){
                    System.out.println("Results of " + association + " were empty .........");
                    return convertToJson(interimResults);
                }
                interimResults.clear();
                interimResults = result;

            }else if( RestOntInterfaceConstants.CLASSOF.compareToIgnoreCase( association.getPath() ) == 0){
                if( areClasses.equals( true )){
                    //TODO: throw exception
                    return "";
                }
                areClasses = true;
                Set<OntResource> result = getClassesOf(interimResults);
                if( result == null || result.isEmpty()){
                    System.out.println("Results of " + association + " were empty .........");
                    return convertToJson(interimResults);
                }
                interimResults.clear();
                interimResults = result;

            }else{
                    OntProperty ontProperty = modelWrapper.getOntModel().
                                                    getOntProperty(modelWrapper.getURI() +
                                                            "#" + association);

                    if( ontProperty == null ){
                        String exp =  association + "is not present in " + modelWrapper.getOntologyName() + " ontology";
                        RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new BadRequestException(exp) );
                        throw new BadRequestException( exp );
                    }

                    Set<OntClass>ontClassSet = new HashSet<OntClass>();
                    if( areClasses ){
                        for( OntResource r : interimResults){
                                ontClassSet.add( r.as(OntClass.class) );
                        }
                    }else{

                        for( OntResource r : interimResults){
                            ontClassSet.add( r.as(Individual.class).getOntClass() );
                        }
                    }
                    Set<OntClass> result = processProperty( ontClassSet, ontProperty );
                    if( result == null || result.isEmpty()){
                        System.out.println("Results of " + association + " were empty .........");
                        return convertToJson(interimResults);
                    }else{
                        interimResults.clear();
                        if( areClasses ){
                            for( OntClass cls : result){
                                interimResults.add( cls.as( OntResource.class) );
                            }
                        }else {
                            for( OntClass cls : result){
                                ExtendedIterator indItr = cls.listInstances( true );
                                while ( indItr.hasNext() ){
                                    interimResults.add( ( (Individual)indItr.next() ).as( OntResource.class ) );
                                }

                            }
                        }
                    }
            }
        }

        return convertToJson(interimResults);
    }



    /*
   private Set<OntClass> getIndividuals(Set<OntClass> ontClassSet) {
       Set<Int> ontClasses = new HashSet<OntClass>();
       for( OntClass ontClass : ontClassSet ){
           ExtendedIterator indItr = ontClass.listInstances(false);
           while( indItr.hasNext() ){
               ontClasses.add(  );
           }
       }
       return ontClasses;
   } */

    private Set<OntClass> getInitialClasses(OntModelWrapper modelWrapper, String association) {
        System.out.println("Checking if " + association + "is a class or a property");

        Set<OntClass> ontClassSet = new HashSet<OntClass>();
        OntClass ontClass = modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() + "#" + association );
        if( ontClass != null ){
            ontClassSet.add( ontClass );
        }else{
            OntProperty ontProperty = modelWrapper.getOntModel().getOntProperty( modelWrapper.getURI() + "#" + association );
            if( ontProperty != null ){

                ExtendedIterator classItr = ontProperty.listDeclaringClasses(false);
                while( classItr.hasNext() ){
                    OntClass cls = (OntClass) classItr.next();
                    if( cls.getURI() != null ){
                        ontClassSet.add( cls );
                    }
                }

            }else{
                //TODO: Throw exception
            }
        }
        return ontClassSet;
    }



    private Set<OntClass> processProperty(Set<OntClass> ontClassSet, OntProperty association) {

        Set<OntClass> resultClasses = new HashSet<OntClass>();



        for( OntClass cls : ontClassSet ){

            //TODO: Do we need to check if its a domain/range ?
            // What info we will get if the given ontClass is a domain/range of the given association

//            if( association.hasDomain( cls.asResource() ) ){

 //           }

            Set<OntClass> interim = find( cls, association );
            if( interim!=null && !interim.isEmpty() ){
                resultClasses.addAll( interim );

            }
        }
        return resultClasses;
    }

    //TODO: Not considering cardinality restrictions
    private Set<OntClass> find(OntClass cls, OntProperty association) {

         Set<OntClass> resultClasses = new HashSet<OntClass>();
         ExtendedIterator superClassItr = cls.listSuperClasses( false );
            while( superClassItr.hasNext() ){
                OntClass c = (OntClass) superClassItr.next();

                if( c.isRestriction()){
                    Restriction r = c.as(Restriction.class);

                    if( r.getOnProperty().getURI().compareToIgnoreCase( association.getURI() ) == 0){
                        System.out.println("Restriction on : "+ r.getOnProperty().getLocalName() );

                        if(r.isAllValuesFromRestriction()){

                            AllValuesFromRestriction allValuesFromRestriction = r.asAllValuesFromRestriction();
                            OntResource res = allValuesFromRestriction.getAllValuesFrom().as( OntResource.class );
                            if( res.isClass() )
                                resultClasses.add(res.asClass());

                        }else if( r.isSomeValuesFromRestriction() ){

                            SomeValuesFromRestriction someValuesFromRestriction = r.asSomeValuesFromRestriction();
                            OntResource res = someValuesFromRestriction.getSomeValuesFrom().as( OntResource.class );
                            if( res.isClass() )
                                resultClasses.add( res.asClass() );

                        }else if( r.isHasValueRestriction() ){

                            HasValueRestriction hasValueRestriction = r.asHasValueRestriction();
                            if( hasValueRestriction.getHasValue() != null &&
                                    hasValueRestriction.getHasValue().isResource()){

                               OntResource res = hasValueRestriction.getHasValue().asResource().as( OntResource.class );
                               if( res.isClass() )
                                 resultClasses.add( res.asClass() );

                            }else{

                                System.out.println(" Has value restriction for " + association);

                            }
                        }
                    }
                }

            }
        return resultClasses;
    }

  private Set<OntResource> getClassesOf(Set<OntResource> interimResults) {

      Set<OntResource> classes = new HashSet<OntResource>();

      for( OntResource res : interimResults ){
          ExtendedIterator classItr = res.as( Individual.class ).listOntClasses( true );
          while ( classItr.hasNext() ){
              classes.add( ( (OntClass)classItr.next() ).as(OntResource.class) );
          }
      }
      return classes;
  }
  private Set<OntResource> getIndividuals(Set<OntResource> interimResults) {

        Set<OntResource> individuals = new HashSet<OntResource>();

        for( OntResource res : interimResults ){
            ExtendedIterator indItr = res.as( OntClass.class ).listInstances( true );
            while( indItr.hasNext() ){
                individuals.add( ( (Individual)indItr.next() ).as(OntResource.class) );
            }
        }
        return individuals;
  }

  private Set<OntResource> getDisjointClass( Set<OntResource> ontClassSet ) {

        Set<OntResource> ontClasses = new HashSet<OntResource>();
        for( OntResource ontClass : ontClassSet ){
            ExtendedIterator classItr = ontClass.as(OntClass.class).listDisjointWith();
            while( classItr.hasNext() ){
                ontClasses.add( ( (OntClass)classItr.next() ).as(OntResource.class) );
            }
        }
        return ontClasses;
  }

  private Set<OntResource> getEquivalentClass( Set<OntResource> ontClassSet ) {

        Set<OntResource> ontClasses = new HashSet<OntResource>();
        for( OntResource ontClass : ontClassSet){
            ExtendedIterator classItr = ontClass.as( OntClass.class ).listEquivalentClasses();
            while( classItr.hasNext() ){
                ontClasses.add( ( (OntClass) classItr.next() ).as(OntResource.class) );
            }
        }
        return ontClasses;
  }

  private Set<OntResource> getAllSubClass( Set<OntResource> ontClassSet ) {

        Set<OntResource> ontClasses = new HashSet<OntResource>();
        for( OntResource ontClass : ontClassSet){

            ExtendedIterator subClassItr = ontClass.as(OntClass.class).listSubClasses(false);

            while( subClassItr.hasNext() ){
                ontClasses.add( ( (OntClass) subClassItr.next() ).as(OntResource.class) );
            }
        }
        return ontClasses;
  }

  private Set<OntResource> getAllSuperClass( Set<OntResource> ontClassSet ) {
        Set<OntResource> ontClasses = new HashSet<OntResource>();

        for( OntResource ontClass : ontClassSet ){

            ExtendedIterator superClassItr = ontClass.as(OntClass.class).listSuperClasses(false);

            while( superClassItr.hasNext() ){
                ontClasses.add( ((OntClass) superClassItr.next()).as(OntResource.class) );
            }
        }
        return ontClasses;
  }

  private String convertToJson(Set<OntResource> result) {
        Set<String>resourceSet = new HashSet<String>();
        for( OntResource r : result ){
            resourceSet.add( r.getURI() );
        }
        Gson gsn = new Gson();
        Type setType = new TypeToken<Set<String>>() {}.getType();
        return gsn.toJson( resourceSet, setType );
  }
}
