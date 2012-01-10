package edu.uga.cs.restendpoint.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;
import edu.uga.cs.restendpoint.service.api.NavigationalService;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceConstants;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceUtil;
import edu.uga.cs.restendpoint.exceptions.NotFoundException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.PathSegment;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Author: kale
 * Date: 10/16/11
 * Time: 12:10 AM
 * Email: <kale@cs.uga.edu>
 */
public  class NavigationalServiceImpl implements NavigationalService {

    public String navigateOntologyClasses( String ontologyName,
                                    List<PathSegment> associationsQuery,
                                    ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper modelWrapper = RestOntInterfaceUtil.getOntModel( ontologyModelStore, ontologyName);
        RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), "Path query: " + associationsQuery  );
        //String[] path = associationsQuery.split("/");
        Set<OntResource> initResources = new HashSet<OntResource>();
        OntClass ontClass = modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() + "#" + associationsQuery.get(0) );
        if( ontClass != null ){
            initResources.add( ontClass );
            associationsQuery = associationsQuery.subList(1, associationsQuery.size() );
        }else{
            OntProperty ontProperty = modelWrapper.getOntModel().getOntProperty( modelWrapper.getURI() + "#" + associationsQuery.get(0) );
            if( ontProperty != null ){

                ExtendedIterator <? extends OntClass> classItr = ontProperty.listDeclaringClasses(false);
                while( classItr.hasNext() ){
                    OntClass cls =  classItr.next();
                    if( cls.getURI() != null ){
                        initResources.add( cls );
                    }
                }

            }else{
                StringBuilder exp = new StringBuilder();
                exp.append( associationsQuery.get(0) ).append(" is neither a property or class in ").append(modelWrapper.getOntologyName());
                exp.append(" ontology ").append(" nor a supported keyword.");
                RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp.toString()) );
                throw new NotFoundException( exp.toString() );
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
                RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                throw new NotFoundException( exp );

            }

            initResources.add( initIndividual );
            associationsQuery = associationsQuery.subList(1, associationsQuery.size() );

        }else{
            //Treat path[0] as a property
              OntProperty initProperty = ontologyModelStore.getOntologyModel( ontologyName ).
                                                                getOntModel().getOntProperty( ontologyModelStore.
                                                                                              getOntologyModel(ontologyName).
                                                                                              getURI() + "#" + associationsQuery.get(0)  );
              if( initProperty == null ){
                  StringBuilder exp = new StringBuilder();
                  exp.append( associationsQuery.get(0) ).append(" is neither a property or instance in ").append(modelWrapper.getOntologyName());
                  exp.append(" ontology ").append(" nor a supported keyword.");
                  RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp.toString()) );
                  throw new NotFoundException( exp.toString() );
              }else{
                  //Get all the classes that declare this property.
                  //From the classes, get all the individuals
                 ExtendedIterator <? extends OntClass> classItr =  initProperty.listDeclaringClasses( false );
                 while ( classItr.hasNext() ){
                    ExtendedIterator <? extends OntResource> indItr =  classItr.next().listInstances( false );

                    while( indItr.hasNext() )
                        initResources.add( indItr.next() );
                 }
              }
        }
        return executePathQuery(  modelWrapper, associationsQuery, initResources, false);
    }


    public String executePathQuery( OntModelWrapper modelWrapper,
                                    List<PathSegment> path, Set<OntResource> interimResults, Boolean areClasses) {

      //  Set<OntClass> interimClasses = getInitialClasses( modelWrapper, path[0]);

        for( PathSegment association : path){

            if( RestOntInterfaceConstants.SUPERCLASS.compareToIgnoreCase( association.getPath() ) == 0 ){

                if( areClasses.equals( false )){
                    String exp =  "Cannot navigate using 'superclass' keyword when navigating through instances " ;
                    RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                    throw new NotFoundException( exp );
                }

                Set<OntResource> result = getAllSuperClass(interimResults);
                if( result == null || result.isEmpty()){
                    break;
                }
                interimResults.clear();
                interimResults = result;

            }else if( RestOntInterfaceConstants.SUBCLASS.compareToIgnoreCase( association.getPath() ) == 0 ){

                if( areClasses.equals( false )){
                    String exp =  "Cannot navigate using 'subclass' keyword when navigating through instances " ;
                    RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                    throw new NotFoundException( exp );
                }
                Set<OntResource> result = getAllSubClass( interimResults );
                if( result == null || result.isEmpty()){
                    break;
                }
                interimResults.clear();
                interimResults = result;


            }else if( RestOntInterfaceConstants.EQUIVALENT.compareToIgnoreCase( association.getPath() ) == 0 ){

                if( areClasses.equals( false )){
                    String exp =  "Cannot navigate using 'equivalent' keyword when navigating through instances " ;
                    RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                    throw new NotFoundException( exp );
                }
                Set<OntResource> result = getEquivalentClass( interimResults );
                if( result == null || result.isEmpty()){
                    break;
                }
                interimResults.clear();
                interimResults = result;


            }else if( RestOntInterfaceConstants.DISJOINT.compareToIgnoreCase( association.getPath() ) == 0 ){

                if( areClasses.equals( false )){
                    String exp =  "Cannot navigate using 'disjoint' keyword when navigating through instances " ;
                    RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                    throw new NotFoundException( exp );
                }
                Set<OntResource> result = getDisjointClass(interimResults);
                if( result == null || result.isEmpty()){
                    break;
                }
                interimResults.clear();
                interimResults = result;


            }else if( RestOntInterfaceConstants.INSTANCEOF.compareToIgnoreCase( association.getPath() ) == 0){

                if( areClasses.equals( false ) ){
                    String exp =  "Cannot navigate using 'instanceOf' keyword when navigating through instances " ;
                    RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                    throw new NotFoundException( exp );
                }
                areClasses = false;
                Set<OntResource> result = getIndividuals( interimResults);
                if( result == null || result.isEmpty()){
                    RestOntInterfaceUtil.log(RestOntInterfaceUtil.class.getName(), "Results of " + association + " were empty .........");
                    return convertToJson(interimResults);
                }
                interimResults.clear();
                interimResults = result;

            }else if( RestOntInterfaceConstants.CLASSOF.compareToIgnoreCase( association.getPath() ) == 0){
                if( areClasses.equals( true )){
                    String exp =  "Cannot navigate using 'classOf' keyword when navigating through classes " ;
                    RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                    throw new NotFoundException( exp );
                }
                areClasses = true;
                Set<OntResource> result = getClassesOf(interimResults);
                if( result == null || result.isEmpty()){
                    RestOntInterfaceUtil.log(RestOntInterfaceUtil.class.getName(), "Results of " + association + " were empty .........");
                    return convertToJson(interimResults);
                }
                interimResults.clear();
                interimResults = result;

            }else{
                    OntProperty ontProperty = modelWrapper.getOntModel().
                                                    getOntProperty(modelWrapper.getURI() +
                                                            "#" + association);

                    if( ontProperty == null ){
                        StringBuilder exp = new StringBuilder();
                        exp.append( association ).append(" is neither a property in ").append(modelWrapper.getOntologyName());
                        exp.append(" ontology ").append(" nor a supported keyword.");
                        RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp.toString()) );
                        throw new NotFoundException( exp.toString() );
                    }

                    Set<OntClass>ontClassSet = new HashSet<OntClass>();
                    if( areClasses ){
                        for( OntResource r : interimResults){
                                ontClassSet.add( r.as(OntClass.class) );
                        }
                    }else{

                        for( OntResource r : interimResults){
                            ExtendedIterator <OntClass> ontClsItr = r.as( Individual.class ).listOntClasses( true );
                            while ( ontClsItr.hasNext() )
                                ontClassSet.add( ontClsItr.next() );
                        }
                    }
                    Set<OntClass> result = processProperty( ontClassSet, ontProperty );
                    if( result == null || result.isEmpty()){
                        RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), "Results of " + association + " were empty ........." );
                        return convertToJson(interimResults);
                    }else{
                        interimResults.clear();
                        if( areClasses ){
                            for( OntClass cls : result){
                                interimResults.add( cls );
                            }
                        }else {
                            for( OntClass cls : result){
                                ExtendedIterator <? extends OntResource> indItr = cls.listInstances( true );
                                while ( indItr.hasNext() ){
                                    interimResults.add(indItr.next());
                                }

                            }
                        }
                    }
            }
        }

       // return convertToJson(interimResults);
       return convertToXML (interimResults );
    }

    private String convertToXML(Set<OntResource> interimResults) {

        if( interimResults == null || interimResults.isEmpty() ){
            return " No class or instance resulted after execution of the navigational query. ";
        }else if (interimResults.iterator().next().canAs( OntClass.class ) ) {
           return  convertOntClassToXML( interimResults );

        }else{
           return  convertIndividualsToXML (interimResults );
        }

    }

    private String convertIndividualsToXML(Set<OntResource> interimResults) {
        Element root = new Element( RestOntInterfaceConstants.INSTANCES );
        Document doc = new Document( root );

        for( OntResource res : interimResults ){
            if( res.getLocalName() != null & res.getURI() != null ){
                Element classElem = new Element( RestOntInterfaceConstants.INSTANCES );
                classElem.setAttribute( RestOntInterfaceConstants.NAME, res.getLocalName() ).
                        setAttribute( RestOntInterfaceConstants.URI, res.getURI() );
                root.addContent( classElem );
            }

        }

        return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );
    }

    private String convertOntClassToXML(Set< OntResource> interimResults) {

        Element root = new Element( RestOntInterfaceConstants.CLASSES );
        Document doc = new Document( root );

        for( OntResource res : interimResults ){
            if( res.getLocalName() != null & res.getURI() != null && res.isClass() ){

                OntClass ontClass = res.as( OntClass.class );

                Element classElem = new Element( RestOntInterfaceConstants.CLASS );

                classElem.setAttribute( RestOntInterfaceConstants.NAME, ontClass.getLocalName() ).
                        setAttribute( RestOntInterfaceConstants.URI, ontClass.getURI() );

            Element subClassRoot = new Element( RestOntInterfaceConstants.SUBCLASSES );
            ExtendedIterator<OntClass> subClassItr = ontClass.listSubClasses( true );
            while( subClassItr.hasNext() ){
                OntClass cls = subClassItr.next();
                if( cls.getLocalName() != null && cls.getURI() != null ){
                    Element subClassElem = new Element( RestOntInterfaceConstants.SUBCLASS );
                    subClassElem.setAttribute(RestOntInterfaceConstants.NAME, cls.getLocalName());
                    subClassElem.setAttribute(RestOntInterfaceConstants.URI, cls.getURI());
                    subClassRoot.addContent(subClassElem);
                }
            }

            Element superClassRoot = new Element( RestOntInterfaceConstants.SUPERCLASSES );
            ExtendedIterator<OntClass> superClassItr = ontClass.listSuperClasses(true);
            while( superClassItr.hasNext() ){
                OntClass cls = superClassItr.next();
                if( cls.getLocalName() != null && cls.getURI() !=null ){
                    Element superClassElem = new Element( RestOntInterfaceConstants.SUPERCLASS );
                    superClassElem.setAttribute(RestOntInterfaceConstants.NAME, cls.getLocalName());
                    superClassElem.setAttribute("uri", cls.getURI());
                    superClassRoot.addContent(superClassElem);
                }
            }

            Element instancesClassRoot = new Element( RestOntInterfaceConstants.INSTANCES );
            ExtendedIterator<? extends OntResource> instanceItr = ontClass.listInstances( true );
            while( instanceItr.hasNext() ){
                OntResource instance = instanceItr.next();
                if( instance.getLocalName()!=null && instance.getURI()!=null){
                    Element instanceElem = new Element( RestOntInterfaceConstants.INSTANCE );
                    instanceElem.setAttribute(RestOntInterfaceConstants.NAME, instance.getLocalName());
                    instanceElem.setAttribute(RestOntInterfaceConstants.URI, instance.getURI());
                    instancesClassRoot.addContent(instanceElem);
                }
            }

            Element propertiesRoot = new Element( RestOntInterfaceConstants.PROPERTIES );
            ExtendedIterator<OntProperty> propItr = ontClass.listDeclaredProperties( false );
            while( propItr.hasNext() ){
                OntProperty prop = propItr.next();
                if(prop.getURI()!= null && prop.getLocalName() != null ){
                    Element propElement = new Element( RestOntInterfaceConstants.PROPERTY);
                    propElement.setAttribute( RestOntInterfaceConstants.NAME, prop.getLocalName() );
                    propElement.setAttribute( RestOntInterfaceConstants.URI, prop.getURI() );
                    propertiesRoot.addContent( propElement );
                }
            }

            classElem.addContent( subClassRoot );
            classElem.addContent( superClassRoot );
            classElem.addContent( instancesClassRoot );
            classElem.addContent( propertiesRoot );

            root.addContent( classElem );

            }

        }

        return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );
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

                ExtendedIterator <? extends  OntClass> classItr = ontProperty.listDeclaringClasses(false);
                while( classItr.hasNext() ){
                    OntClass cls =  classItr.next();
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

            Set<OntClass> interim = find( cls, association );
            if( interim!=null && !interim.isEmpty() ){
                resultClasses.addAll( interim );

            }
        }
        return resultClasses;
    }

    //TODO: Not considering cardinality restrictions
    private Set<OntClass> find(OntClass cls, final OntProperty association) {

         Set<OntClass> resultClasses = new HashSet<OntClass>();
         ExtendedIterator <OntClass> superClassItr = cls.listSuperClasses( false ).filterKeep( new Filter<OntClass>() {
             @Override
             public boolean accept(OntClass o) {

                 if( o.isRestriction() ){

                     Restriction restriction = o.as( Restriction.class);
                     return  restriction.onProperty( association );
                 }
                 return false ;
             }
         });
            while( superClassItr.hasNext() ){

                OntClass c =  superClassItr.next();
                Restriction r = c.as(Restriction.class);

                   // if( r.getOnProperty().getURI().compareToIgnoreCase( association.getURI() ) == 0){
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
                    //}

            }
        return resultClasses;
    }

  private Set<OntResource> getClassesOf(Set<OntResource> interimResults) {

      Set<OntResource> classes = new HashSet<OntResource>();

      for( OntResource res : interimResults ){
          ExtendedIterator <OntClass> classItr = res.as( Individual.class ).listOntClasses( true );
          while ( classItr.hasNext() ){
              classes.add(  classItr.next().as(OntResource.class) );
          }
      }
      return classes;
  }
  private Set<OntResource> getIndividuals(Set<OntResource> interimResults) {

        Set<OntResource> individuals = new HashSet<OntResource>();

        for( OntResource res : interimResults ){
            ExtendedIterator <? extends OntResource> indItr = res.as( OntClass.class ).listInstances( true );
            while( indItr.hasNext() ){
                individuals.add( indItr.next() );
            }
        }
        return individuals;
  }

  private Set<OntResource> getDisjointClass( Set<OntResource> ontClassSet ) {

        Set<OntResource> ontClasses = new HashSet<OntResource>();
        for( OntResource ontClass : ontClassSet ){
            ExtendedIterator <OntClass> classItr = ontClass.as(OntClass.class).listDisjointWith();
            while( classItr.hasNext() ){
                ontClasses.add( classItr.next() );
            }
        }
        return ontClasses;
  }

  private Set<OntResource> getEquivalentClass( Set<OntResource> ontClassSet ) {

        Set<OntResource> ontClasses = new HashSet<OntResource>();
        for( OntResource ontClass : ontClassSet){
            ExtendedIterator <OntClass>classItr = ontClass.as( OntClass.class ).listEquivalentClasses();
            while( classItr.hasNext() ){
                ontClasses.add(classItr.next());
            }
        }
        return ontClasses;
  }

  private Set<OntResource> getAllSubClass( Set<OntResource> ontClassSet ) {

        Set<OntResource> ontClasses = new HashSet<OntResource>();
        for( OntResource resource : ontClassSet){

            ExtendedIterator <OntClass> subClassItr = resource.as(OntClass.class).listSubClasses(false);

            while( subClassItr.hasNext() ){
                ontClasses.add( subClassItr.next()  );
            }
        }
        return ontClasses;
  }

  private Set<OntResource> getAllSuperClass( Set<OntResource> ontClassSet ) {
        Set<OntResource> ontClasses = new HashSet<OntResource>();

        for( OntResource resource : ontClassSet ){

            ExtendedIterator <OntClass> superClassItr = resource.as(OntClass.class).listSuperClasses(false);

            while( superClassItr.hasNext() ){
                ontClasses.add( superClassItr.next() );
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
