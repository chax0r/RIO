package edu.uga.cs.restendpoint.service.impl;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.sparql.resultset.XMLOutput;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import edu.uga.cs.restendpoint.exceptions.BadRequestException;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;
import edu.uga.cs.restendpoint.service.api.SchemaInfoService;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceConstants;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceUtil;
import edu.uga.cs.restendpoint.exceptions.NotFoundException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        ExtendedIterator <OntClass>classItr =  model.listNamedClasses();
        Set<OntClass> classes = new HashSet<OntClass>();

        while( classItr.hasNext() ){
            OntClass ontClass = classItr.next();
            if( ontClass.getLocalName() != null )
                classes.add( ontClass );

        }

        Element root = new Element("Classes");
        Document doc = new Document( root );
        for( OntClass ontClass : classes){
            Element classElem = new Element("Class");
            classElem.setAttribute("name", ontClass.getLocalName());
            classElem.setAttribute("uri", ontClass.getURI());
            root.addContent(classElem);


            Element subClassRoot = new Element("SubClasses");
            ExtendedIterator<OntClass> subClassItr = ontClass.listSubClasses( true );
            while( subClassItr.hasNext() ){
                OntClass cls = subClassItr.next();
                if( cls.getLocalName() != null && cls.getURI() != null ){
                    Element subClassElem = new Element("Class");
                    subClassElem.setAttribute("name", cls.getLocalName());
                    subClassElem.setAttribute("uri", cls.getURI());
                    subClassRoot.addContent(subClassElem);
                }
            }
            Element superClassRoot = new Element("SuperClasses");
            ExtendedIterator<OntClass> superClassItr = ontClass.listSuperClasses(true);
            while( superClassItr.hasNext() ){
                OntClass cls = superClassItr.next();
                if( cls.getLocalName() != null && cls.getURI() !=null ){
                    Element superClassElem = new Element("Class");
                    superClassElem.setAttribute("name", cls.getLocalName());
                    superClassElem.setAttribute("uri", cls.getURI());
                    superClassRoot.addContent(superClassElem);
                }
            }
            Element instancesClassRoot = new Element("Instances");
            ExtendedIterator<? extends OntResource> instanceItr = ontClass.listInstances( true );
            while( instanceItr.hasNext() ){
                OntResource instance = instanceItr.next();
                if( instance.getLocalName()!=null && instance.getURI()!=null){
                    Element instanceElem = new Element("Instance");
                    instanceElem.setAttribute("name", instance.getLocalName());
                    instanceElem.setAttribute("uri", instance.getURI());
                     instancesClassRoot.addContent(instanceElem);
                }
            }

            classElem.addContent(subClassRoot);
            classElem.addContent(superClassRoot);
            classElem.addContent(instancesClassRoot);

        }
            //XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

        /*Set<String> classSet = new HashSet<String>();
        while( classItr.hasNext() ){
            OntClass o =  classItr.next();
            if( o.getURI() != null )
                classSet.add( o.getURI() );
        } */

        return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );
        //return RestOntInterfaceUtil.getJSON( classSet, new TypeToken<List<String>>() {}.getType() );
    }


    public String getClasses( String ontologyName, String classes, ServletContext context){
        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );

        String[] classesArray = classes.split(",");
        System.out.println("Value of classes: " + classes);

        if( classesArray == null || classesArray.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }
        Set<OntClass> ontClasses = new HashSet<OntClass>( classesArray.length );
        for( String className : classesArray){
                OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className, true );
                ontClasses.add( ontClass );
        }

        Element root = new Element( RestOntInterfaceConstants.CLASSES );
        Document doc = new Document( root );
        for( OntClass ontClass : ontClasses){
            Element classElem = new Element( RestOntInterfaceConstants.CLASS );
            classElem.setAttribute( RestOntInterfaceConstants.NAME, ontClass.getLocalName());
            classElem.setAttribute(RestOntInterfaceConstants.URI, ontClass.getURI());
            root.addContent(classElem);


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

            classElem.addContent(subClassRoot);
            classElem.addContent(superClassRoot);
            classElem.addContent(instancesClassRoot);
            classElem.addContent( propertiesRoot );

        }
            //XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

        /*Set<String> classSet = new HashSet<String>();
        while( classItr.hasNext() ){
            OntClass o =  classItr.next();
            if( o.getURI() != null )
                classSet.add( o.getURI() );
        } */

        return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );
    }


    public String createClasses( @PathParam("ontologyName") String ontologyName,
                          @PathParam("classes") String allClasses,
                          InputStream inputXML,
                          @Context ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper modelWrapper  = RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName);

        String[] classesArray = allClasses.split( RestOntInterfaceConstants.COMMA_DELIMITER );
        System.out.println("Value of classes: " + allClasses);

        if( classesArray == null || classesArray.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }

        SAXBuilder builder = new SAXBuilder();
        try {

            Document inputDoc = builder.build( inputXML );
            Element classesRoot = inputDoc.getRootElement();

            if( classesRoot == null ){
                String exp =   " The request body is not in correct format. ";
                RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new NotFoundException(exp) );
                throw new BadRequestException( exp );
            }

            List<Element> childElements = classesRoot.getChildren();
            for( Element classElement : childElements){

                validateNameAttribute(classElement, " The Class tag does not have name attribute. ");

                String classLocalName = classElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                if( RestOntInterfaceUtil.getClass( modelWrapper, classLocalName, false ) != null ){
                      String exp =   classLocalName + " already exists in the  " + modelWrapper.getOntologyName() + " ontology ";
                      RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                      throw new BadRequestException( exp );
                }

                OntClass newClass = RestOntInterfaceUtil.getClass( modelWrapper, classLocalName, true ) ;

                if( classElement.getChild( RestOntInterfaceConstants.SUPERCLASSES) != null){

                    List<Element> subClassElementList = classElement.getChild( RestOntInterfaceConstants.SUBCLASSES ).getChildren();
                    for( Element subClassElement : subClassElementList ){

                        validateNameAttribute(subClassElement, " The SubClass tag does not have name attribute. ");

                        String subClassName = subClassElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                        OntClass subClass = RestOntInterfaceUtil.getClass( modelWrapper, subClassName,false );
                        if( subClass == null ){
                            subClass = modelWrapper.getOntModel().createClass( modelWrapper.getURI() + "#" + subClassName );
                        }
                        newClass.addSubClass( subClass);
                    }
                }

                if( classElement.getChild( RestOntInterfaceConstants.SUPERCLASS) != null ){
                    List<Element> superClassElementList = classElement.getChild( RestOntInterfaceConstants.SUPERCLASSES ).getChildren();
                    for( Element superClassElement : superClassElementList ){

                        validateNameAttribute(superClassElement, " The SuperClass tag does not have name attribute. ");

                        String superClassName = superClassElement.getAttribute(RestOntInterfaceConstants.NAME).getValue();

                        OntClass superClass = RestOntInterfaceUtil.getClass( modelWrapper, superClassName, false );

                        if( superClass == null ){
                            superClass = modelWrapper.getOntModel().createClass( modelWrapper.getURI() + "#" + superClassName );
                        }
                        newClass.addSuperClass( superClass );
                    }
                }

                if( classElement.getChild( RestOntInterfaceConstants.INSTANCES) != null ){

                    List<Element> instanceElementList = classElement.getChild( RestOntInterfaceConstants.INSTANCES ).getChildren();
                    for( Element instanceElement : instanceElementList ){

                        validateNameAttribute(instanceElement, " The Instance tag does not have name attribute. ");

                        String instanceName = instanceElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                        Individual instance = RestOntInterfaceUtil.getIndividual( modelWrapper, instanceName, false);
                        if( instance == null ){

                            newClass.createIndividual( modelWrapper.getURI() + "#" + instanceName);

                        } /*else if( !instance.isIndividual() ){

                            String exp =   instanceName + " does not exist in the  " + modelWrapper.getOntologyName() + " ontology ";
                                 exp = exp + " and there is other individual with the same name present .";
                            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                            throw new BadRequestException( exp );

                         }*/else{

                            String exp =   instanceName + " already exists in the  " + modelWrapper.getOntologyName() + " ontology ";
                            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                            throw new BadRequestException( exp );

                        }
                    }
                }

             /*   List<Element> propertyElementList = classElement.getChild( RestOntInterfaceConstants.PROPERTIES ).getChildren();
                for( Element propertyElement : propertyElementList ){
                        String propertyName = propertyElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                        OntProperty property = modelWrapper.getOntModel().getOntProperty( modelWrapper.getURI() + "#" + propertyName);
                        if( property == null ){
                            newClass.
                        }
                }*/
            }
        } catch (JDOMException e) {
            RestOntInterfaceUtil.log(SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        } catch (IOException e) {
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        }

        return "";
    }

    public String updateClasses( @PathParam("ontologyName") String ontologyName,
                          @PathParam("classes") String allClasses,
                          InputStream inputXML,
                          @Context ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper modelWrapper  = RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName);

        String[] classesArray = allClasses.split( RestOntInterfaceConstants.COMMA_DELIMITER );
        System.out.println("Value of classes: " + allClasses);

        if( classesArray == null || classesArray.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }

        SAXBuilder builder = new SAXBuilder();
            try {

            Document inputDoc = builder.build( inputXML );
            Element classesRoot = inputDoc.getRootElement();

            if( classesRoot == null ){
                String exp =   " The request body is not in correct format. ";
                RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                throw new NotFoundException( exp );
            }

            List<Element> childElements = classesRoot.getChildren();
            for( Element classElement : childElements){

                validateNameAttribute(classElement, " The Class tag does not have name attribute. ");

                String classLocalName = classElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

               /* if( modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() +"#" + classLocalName ) == null ){
                      String exp =   classLocalName + " does not exists in the  " + ontologyName + " ontology ";
                      RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                      throw new BadRequestException( exp );
                }*/

                OntClass updateClass = RestOntInterfaceUtil.getClass( modelWrapper, classLocalName, true);

                if( classElement.getChild( RestOntInterfaceConstants.SUBCLASSES) != null ){

                    List<Element> subClassElementList = classElement.getChild( RestOntInterfaceConstants.SUBCLASSES ).getChildren();

                    updateSubClasses(modelWrapper, classLocalName, updateClass, subClassElementList);
                }

                if( classElement.getChild( RestOntInterfaceConstants.SUPERCLASSES ) != null ){

                    List<Element> superClassElementList = classElement.getChild( RestOntInterfaceConstants.SUPERCLASSES ).getChildren();

                    updateSuperClasses(ontologyName, modelWrapper, classLocalName, updateClass, superClassElementList);
                }

                if( classElement.getChild( RestOntInterfaceConstants.INSTANCES ) != null ){


                    List<Element> instanceElementList = classElement.getChild( RestOntInterfaceConstants.INSTANCES ).getChildren();
                    for( Element instancesElement : instanceElementList ){

                        validateNameAttribute( instancesElement, " The Instance tag does not have name attribute. ");

                        //Get the name of the instance that has to be update
                        String instanceName = instancesElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                        //Validate if it exist in the ontology
                        Individual individual = RestOntInterfaceUtil.getIndividual( modelWrapper, instanceName, false);

                        //If the individual does not exist in the ontology throw an exception : 400 BadRequest
                        if( individual == null ){

                            String exp =   instanceName + "  does not exists in the  " + modelWrapper.getOntologyName() + " ontology ";
                            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new NotFoundException( exp ));
                            throw new NotFoundException( exp );

                            //Validate if the individual has the relation instanceOf with the class that we are updating.
                        } else if ( ! individual.isIndividual()  || !individual.hasOntClass( updateClass )){

                            //If it is not an instance of the class throw exception: 400 BadRequest
                            String exp =   instanceName + " is  not  an instance of "+ classLocalName +
                                    " the  " + modelWrapper.getOntologyName() + " ontology ";
                            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                            throw new BadRequestException( exp );

                        }

                       //Check if there is an Update tag in the request body, if not throw exception: 400 BadRequest
                        if( instancesElement.getChild( RestOntInterfaceConstants.UPDATE ) == null ){

                            String exp = "The Instance tag doesn't have an Update tag";
                            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                            throw new BadRequestException( exp );

                        }

                        if( instancesElement.getChild( RestOntInterfaceConstants.UPDATE ).
                                getAttribute(RestOntInterfaceConstants.NAME) == null ){

                            String exp =   " The Update tag does not have name attribute. ";
                            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new NotFoundException(exp) );
                            throw new BadRequestException( exp );

                        }

                        //Check for name attribute in Update tag, if not there throw exception: 400 BadRequest
                        String updateInstanceName = instancesElement.getChild( RestOntInterfaceConstants.UPDATE ).
                                getAttribute(RestOntInterfaceConstants.NAME).getValue();

                        if( updateInstanceName == null ){

                            String exp = "The Update tag in SubClass tag doesn't have name attribute.";
                            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                            throw new BadRequestException( exp );

                        }

                        //Check if the class mentioned in update tag exists in the ontology, if not throw exception: 400 BadRequest
                        //OntClass updateSubClass = modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() + "#" + instanceName);
                        Individual updateInstance = RestOntInterfaceUtil.getIndividual( modelWrapper, updateInstanceName, false );
                        if( updateInstance != null ){
                            String exp = updateInstanceName + " is already present in  " + modelWrapper.getOntologyName() + "ontology.";
                            exp = exp + "You need to provide a non-existent instance.";
                        }

                        //If all above validations pass, remove the current individual and add the new one.
                        updateClass.dropIndividual( individual );
                        updateClass.createIndividual( modelWrapper.getURI() + "#" + updateInstanceName);

                    }
                }

             /*   List<Element> propertyElementList = classElement.getChild( RestOntInterfaceConstants.PROPERTIES ).getChildren();
                for( Element propertyElement : propertyElementList ){
                        String propertyName = propertyElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                        OntProperty property = modelWrapper.getOntModel().getOntProperty( modelWrapper.getURI() + "#" + propertyName);
                        if( property == null ){
                            updateClass.
                        }
                }*/



            }


        } catch (JDOMException e) {
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
                throw new BadRequestException( "There was a problem while parsing the request body.");
        } catch (IOException e) {
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
                throw new BadRequestException( "There was a problem while parsing the request body.");
        }

        return "";
    }


    public String getSubClassesOf( String ontologyName,
                                            String allClasses,
                                            ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );

        String[] classes = allClasses.split( RestOntInterfaceConstants.COMMA_DELIMITER );
        System.out.println("Value of classes: " + allClasses);

        if( classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }

        Set<OntClass> ontClasses = new HashSet<OntClass>( classes.length );
        for( String className : classes){
                OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className, true);
                ontClasses.add( ontClass );

        }
        Element root = new Element( RestOntInterfaceConstants.CLASSES );
        Document doc = new Document( root );
        for( OntClass ontClass : ontClasses){
            Element classElem = new Element( RestOntInterfaceConstants.CLASS );
            classElem.setAttribute( RestOntInterfaceConstants.NAME , ontClass.getLocalName());
            classElem.setAttribute( RestOntInterfaceConstants.URI, ontClass.getURI());
            root.addContent(classElem);

            Element subClassElemList = new Element( RestOntInterfaceConstants.SUBCLASSES );
            classElem.addContent( subClassElemList);

            ExtendedIterator<OntClass> subClassItr =
                    ontClass.listSubClasses( true ).filterDrop( new Filter<OntClass>() {
                @Override
                public boolean accept(OntClass o) {
                    return o.isAnon();
                }
            });

            while( subClassItr.hasNext() ){
                OntClass subClass = subClassItr.next();
                    Element subClassElem = new Element( RestOntInterfaceConstants.SUBCLASS );
                    subClassElem.setAttribute(RestOntInterfaceConstants.NAME , subClass.getLocalName() );
                    subClassElem.setAttribute( RestOntInterfaceConstants.URI, subClass.getURI() );
                    subClassElemList.addContent( subClassElem );
            }

        }
        return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );


        /*Map<String, Set<String>> output = new HashMap<String, Set<String>>();
        for( String className : classes){
            System.out.println(className);
            OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className);
            Set<String> subClasses = RestOntInterfaceUtil.getSubClasses(ontClass);
            output.put( className, subClasses);
        }
        return RestOntInterfaceUtil.getJSON(output, new TypeToken< Map<String,Set<String>> >() {}.getType());*/
    }

    public String createSubClassesOf( String ontologyName,
                                      String allClasses,
                                      InputStream inputXML,
                                      @Context ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper modelWrapper  = RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName);

        String[] classesArray = allClasses.split( RestOntInterfaceConstants.COMMA_DELIMITER );
        System.out.println("Value of classes: " + allClasses);

        if( classesArray == null || classesArray.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }

        SAXBuilder builder = new SAXBuilder();
        try {

            Document inputDoc = builder.build( inputXML );
            Element classesRoot = inputDoc.getRootElement();

            if( classesRoot == null ){
                String exp =   " The request body is not in correct format. ";
                RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                throw new NotFoundException( exp );
            }
            List<Element> childElements = classesRoot.getChildren();
            for( Element classElement : childElements){

                validateNameAttribute(classElement, "The Class tag does not have name attribute");

                String classLocalName = classElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();


                /*if( modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() +"#" + classLocalName ) == null ){
                      String exp =   classLocalName + " does not exists in the  " + ontologyName + " ontology ";
                      RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                      throw new BadRequestException( exp );
                } */

                OntClass ontClass = RestOntInterfaceUtil.getClass( modelWrapper, classLocalName, true);

                if( classElement.getChild( RestOntInterfaceConstants.SUBCLASSES ) != null ){

                    List<Element> subClassElementList = classElement.getChild( RestOntInterfaceConstants.SUBCLASSES ).getChildren();
                    for( Element subClassElement : subClassElementList ){

                        validateNameAttribute(subClassElement, "The SubClass tag does not have name attribute");

                        String subClassName = subClassElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                        OntClass subClass = modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() + "#" + subClassName);
                        if( subClass == null ){
                                subClass = modelWrapper.getOntModel().createClass( modelWrapper.getURI() + "#" + subClassName );
                        }
                        ontClass.addSubClass( subClass);
                    }
                }else{

                    String exp = " SubClasses tag was missing from the request body.";
                    RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                    throw new BadRequestException( exp );

                }
            }

        } catch (JDOMException e) {
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        } catch (IOException e) {
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        }

        return "";

    }

    public String updateSubClassesOf( String ontologyName,
                                      String allClasses,
                                      InputStream inputXML,
                                      ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper modelWrapper  = RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName);

        String[] classesArray = allClasses.split( RestOntInterfaceConstants.COMMA_DELIMITER );
        System.out.println("Value of classes: " + allClasses);

        if( classesArray == null || classesArray.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }

        SAXBuilder builder = new SAXBuilder();
        try {

            Document inputDoc = builder.build( inputXML );
            Element classesRoot = inputDoc.getRootElement();

            if( classesRoot == null ){
                String exp =   " The request body is not in correct format. ";
                RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                throw new NotFoundException( exp );
            }

            List<Element> childElements = classesRoot.getChildren();
            for( Element classElement : childElements){
                 String classLocalName = classElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

               /* if( modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() +"#" + classLocalName ) == null ){
                      String exp =   classLocalName + " does not exists in the  " + ontologyName + " ontology ";
                      RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                      throw new BadRequestException( exp );
                }*/

                OntClass updateClass = RestOntInterfaceUtil.getClass( modelWrapper, classLocalName, true);
                //modelWrapper.getOntModel().createClass( modelWrapper.getURI() + "#" + classLocalName );

                if( classElement.getChild( RestOntInterfaceConstants.SUBCLASSES ) != null ){

                    List<Element> subClassElementList = classElement.getChild( RestOntInterfaceConstants.SUBCLASSES ).getChildren();

                    updateSubClasses(modelWrapper, classLocalName, updateClass, subClassElementList);
                }else{

                    String exp = " SubClasses tag was missing from the request body.";
                    RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                    throw new BadRequestException( exp );

                }

            }
        } catch (JDOMException e) {
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        } catch (IOException e) {
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        }

        return "";

    }



    public String getSuperClassesOf(@PathParam("ontologyName") String ontologyName,
                                       @PathParam("classes") String allClasses,
                                       @Context ServletContext context) {
         OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );

        String[] classes = allClasses.split( RestOntInterfaceConstants.COMMA_DELIMITER );
        System.out.println("Value of classes: " + allClasses);

        if( classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
         }

        Set<OntClass> ontClasses = new HashSet<OntClass>( classes.length );
         for( String className : classes){
                 OntClass ontClass = RestOntInterfaceUtil.
                     getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className, true);
                 ontClasses.add( ontClass );
         }
         Element root = new Element( RestOntInterfaceConstants.CLASSES );
         Document doc = new Document( root );
         for( OntClass ontClass : ontClasses){
             Element classElem = new Element( RestOntInterfaceConstants.CLASS );
             classElem.setAttribute( RestOntInterfaceConstants.NAME, ontClass.getLocalName() );
             classElem.setAttribute( RestOntInterfaceConstants.URI, ontClass.getURI() );
             root.addContent(classElem);

             Element superClassElemList = new Element( RestOntInterfaceConstants.SUPERCLASSES );
             classElem.addContent( superClassElemList);

             ExtendedIterator<OntClass> subClassItr = ontClass.listSuperClasses(true);
             while( subClassItr.hasNext() ){
                 OntClass subClass = subClassItr.next();
                 if( subClass.getLocalName() != null && subClass.getURI() != null ){
                     Element subClassElem = new Element( RestOntInterfaceConstants.SUPERCLASS );
                     subClassElem.setAttribute( RestOntInterfaceConstants.NAME , subClass.getLocalName() );
                     subClassElem.setAttribute( RestOntInterfaceConstants.URI, subClass.getURI() );
                     superClassElemList.addContent(subClassElem);
                 }
             }

         }
         return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );

     /*   Map<String, Set<String>> output = new HashMap<String, Set<String>>();
        for( String className : classes){
            System.out.println(className);
            OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className);
            Set<String> superClasses = RestOntInterfaceUtil.getSuperClasses( ontClass );
            output.put( className, superClasses);
        }
        return RestOntInterfaceUtil.getJSON(output, new TypeToken< Map<String,List<String>> >() {}.getType());*/
    }
     public String createSuperClassesOf( String ontologyName,
                                      String allClasses,
                                      InputStream inputXML,
                                      @Context ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper modelWrapper  = RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName);

        String[] classesArray = allClasses.split( RestOntInterfaceConstants.COMMA_DELIMITER );
        System.out.println("Value of classes: " + allClasses);

        if( classesArray == null || classesArray.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }

        SAXBuilder builder = new SAXBuilder();
        try {

            Document inputDoc = builder.build( inputXML );
            Element classesRoot = inputDoc.getRootElement();

            if( classesRoot == null ){
                String exp =   " The request body is not in correct format. ";
                RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                throw new NotFoundException( exp );
            }
            List<Element> childElements = classesRoot.getChildren();
            for( Element classElement : childElements){

                String classLocalName = classElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                /*if( modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() +"#" + classLocalName ) == null ){
                      String exp =   classLocalName + " does not exists in the  " + ontologyName + " ontology ";
                      RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                      throw new BadRequestException( exp );
                } */

                OntClass ontClass = RestOntInterfaceUtil.getClass( modelWrapper, classLocalName, true);
                //modelWrapper.getOntModel().createClass( modelWrapper.getURI() + "#" + classLocalName );

                if( classElement.getChild( RestOntInterfaceConstants.SUPERCLASSES ) != null ){

                    List<Element> subClassElementList = classElement.getChild( RestOntInterfaceConstants.SUPERCLASSES ).getChildren();

                    for( Element subClassElement : subClassElementList ){

                        String subClassName = subClassElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                        OntClass subClass = modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() + "#" + subClassName);
                            if( subClass == null ){
                                subClass = modelWrapper.getOntModel().createClass( modelWrapper.getURI() + "#" + subClassName );
                            }
                         ontClass.addSubClass( subClass);
                    }
                }else{

                    String exp = " SuperClasses tag was missing from the request body.";
                    RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                    throw new BadRequestException( exp );

                }
            }

        } catch (JDOMException e) {
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        } catch (IOException e) {
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        }

        return "";

    }
     public String updateSuperClassesOf( String ontologyName,
                                      String allClasses,
                                      InputStream inputXML,
                                      ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper modelWrapper  = RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName);

        String[] classesArray = allClasses.split( RestOntInterfaceConstants.COMMA_DELIMITER );
        System.out.println("Value of classes: " + allClasses);

        if( classesArray == null || classesArray.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }

        SAXBuilder builder = new SAXBuilder();
        try {

            Document inputDoc = builder.build( inputXML );
            Element classesRoot = inputDoc.getRootElement();

            if( classesRoot == null ){
                String exp =   " The request body is not in correct format. ";
                RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                throw new BadRequestException( exp );
            }

            List<Element> childElements = classesRoot.getChildren();
            for( Element classElement : childElements){
                 String classLocalName = classElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

               /* if( RestOntInterfaceUtil.getClass( modelWrapper, classLocalName ) == null ){
                      String exp =   classLocalName + " does not exists in the  " + ontologyName + " ontology ";
                      RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                      throw new BadRequestException( exp );
                }*/

                OntClass updateClass = RestOntInterfaceUtil.getClass( modelWrapper, classLocalName, true );

                if( classElement.getChild( RestOntInterfaceConstants.SUPERCLASSES ) != null ){

                    List<Element> superClassElementList = classElement.getChild( RestOntInterfaceConstants.SUPERCLASSES ).getChildren();

                    updateSuperClasses(ontologyName, modelWrapper, classLocalName, updateClass, superClassElementList);

                }else{

                    String exp = " SuperClasses tag was missing from the request body.";
                    RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                    throw new BadRequestException( exp );

                }

            }
        } catch (JDOMException e) {
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        } catch (IOException e) {
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        }

        return "";

     }

    public String getPropertiesOfClasses( String ontologyName,
                                            String allClasses,
                                            ServletContext context){
        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        String[] classes = allClasses.split(",");
        System.out.println("Value of classes: " + allClasses);
        if(classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new BadRequestException( exp );
        }

        Element root = new Element( RestOntInterfaceConstants.CLASSES );
        Document doc = new Document( root );
        for( String className : classes) {

            OntClass ontClass = RestOntInterfaceUtil.
                     getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className, true);
            Element classElem = new Element( RestOntInterfaceConstants.CLASS );
            classElem.setAttribute( RestOntInterfaceConstants.NAME, ontClass.getLocalName() );
            classElem.setAttribute(RestOntInterfaceConstants.URI, ontClass.getURI());

            Element propertiesRoot = new Element( RestOntInterfaceConstants.PROPERTIES );
            ExtendedIterator<OntProperty> propItr = ontClass.listDeclaredProperties( false );
            while( propItr.hasNext() ){

                OntProperty prop = propItr.next();

                if(prop.getURI()!= null && prop.getLocalName() != null ){
                    Element propElement = new Element( RestOntInterfaceConstants.PROPERTY);
                    propElement.setAttribute( RestOntInterfaceConstants.NAME, prop.getLocalName() );
                    propElement.setAttribute( RestOntInterfaceConstants.URI, prop.getURI() );

                    if( prop.isDatatypeProperty() ){
                        propElement.setAttribute( RestOntInterfaceConstants.PROPERTY_TYPE, "data");

                    }else if( prop.isObjectProperty() ){
                        propElement.setAttribute( RestOntInterfaceConstants.PROPERTY_TYPE, "object");

                    }else if ( prop.isFunctionalProperty() ){
                        propElement.setAttribute( RestOntInterfaceConstants.PROPERTY_TYPE, "functional");
                    }else if( prop.isSymmetricProperty() ){
                        propElement.setAttribute( RestOntInterfaceConstants.PROPERTY_TYPE, "symmetric");
                    }else if( prop.isTransitiveProperty() ){
                        propElement.setAttribute( RestOntInterfaceConstants.PROPERTY_TYPE, "transitive");
                    }else if( prop.isInverseFunctionalProperty() ){
                        propElement.setAttribute( RestOntInterfaceConstants.PROPERTY_TYPE, "inverseFunctional");
                    }
                    propertiesRoot.addContent( propElement );
                }
            }
            classElem.addContent( propertiesRoot );
            root.addContent(classElem);
        }

        return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );

      /*  Map<String, Set<String>> output = new HashMap<String, Set<String>>();
        for( String className : classes){
            OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className);
            Set<String> properties = RestOntInterfaceUtil.getProperties(ontClass);
            output.put( className, properties);
        }
        return RestOntInterfaceUtil.getJSON(output, new TypeToken< Map<String,List<String>> >() {}.getType());*/
    }

    public String getProperties( String ontologyName, String allProperties, ServletContext context ){
       OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );

        String[] properties = allProperties.split(",");
        System.out.println("Value of classes: " + allProperties);

        if( properties == null || properties.length == 0){
            String exp =   " The properties mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new BadRequestException( exp );
        }

        Element root = new Element( RestOntInterfaceConstants.PROPERTIES );
        Document doc = new Document( root );
        for( String property : properties ){
            OntProperty ontProperty = RestOntInterfaceUtil.getProperty(
                    RestOntInterfaceUtil.getOntModel( ontologyModelStore, ontologyName), property, true );

            Element propElement = new Element( RestOntInterfaceConstants.PROPERTY );
            propElement.setAttribute( RestOntInterfaceConstants.NAME, ontProperty.getLocalName() ).
                    setAttribute( RestOntInterfaceConstants.URI, ontProperty.getURI() );

            if( ontProperty.isDatatypeProperty() ){
                propElement.setAttribute( RestOntInterfaceConstants.PROPERTY_TYPE, "data");

            }else if( ontProperty.isObjectProperty() ){
                propElement.setAttribute( RestOntInterfaceConstants.PROPERTY_TYPE, "object");

            }else if ( ontProperty.isFunctionalProperty() ){
                propElement.setAttribute( RestOntInterfaceConstants.PROPERTY_TYPE, "functional");

            }else if( ontProperty.isSymmetricProperty() ){
                propElement.setAttribute( RestOntInterfaceConstants.PROPERTY_TYPE, "symmetric");

            }else if( ontProperty.isTransitiveProperty() ){
                propElement.setAttribute( RestOntInterfaceConstants.PROPERTY_TYPE, "transitive");

            }else if( ontProperty.isInverseFunctionalProperty() ){
                propElement.setAttribute( RestOntInterfaceConstants.PROPERTY_TYPE, "inverseFunctional");

            }


            Element domainClassRootElem = new Element( RestOntInterfaceConstants.DOMAIN );
            ExtendedIterator<? extends  OntResource> domainItr = ontProperty.listDomain();
             while( domainItr.hasNext() ){
                 OntResource domClass = domainItr.next();
                if( domClass.getLocalName() != null && domClass.getURI() != null ){

                    domainClassRootElem.addContent( new Element(RestOntInterfaceConstants.CLASS) ).
                            setAttribute(RestOntInterfaceConstants.NAME, domClass.getLocalName()).
                            setAttribute(RestOntInterfaceConstants.URI, domClass.getURI());
                }
            }

            Element rangeClassRootElem = new Element( RestOntInterfaceConstants.RANGE );
            ExtendedIterator<? extends  OntResource> rangeItr = ontProperty.listDomain();
             while( rangeItr.hasNext() ){
                OntResource rangeClass = rangeItr.next();
                if( rangeClass.getLocalName() != null && rangeClass.getURI() != null ){

                    rangeClassRootElem.addContent(new Element(RestOntInterfaceConstants.CLASS)).
                            setAttribute(RestOntInterfaceConstants.NAME, rangeClass.getLocalName()).
                            setAttribute(RestOntInterfaceConstants.URI, rangeClass.getURI());
                }
            }

            Element declaringClassRootElem = new Element( RestOntInterfaceConstants.DECLARING_CLASSES);
            ExtendedIterator<? extends  OntClass> decItr = ontProperty.listDeclaringClasses( false );
            while( decItr.hasNext() ){
                OntClass decClass = decItr.next();
                if( decClass.getLocalName() != null && decClass.getURI() != null ){

                    Element decElem = new Element( RestOntInterfaceConstants.DECLARING_CLASS ) ;
                    decElem.setAttribute(RestOntInterfaceConstants.NAME, decClass.getLocalName()).
                            setAttribute(RestOntInterfaceConstants.URI, decClass.getURI());
                    declaringClassRootElem.addContent( decElem );

                }
            }

            Element subPropertiesRootElem = new Element( RestOntInterfaceConstants.SUBPROPERTIES );
            ExtendedIterator<? extends  OntProperty> subPropItr = ontProperty.listSubProperties( true );
            while( subPropItr.hasNext() ){
                OntProperty subProp = subPropItr.next();
                if( subProp.getLocalName() != null && subProp.getURI() != null ){

                    Element subPropElem = new Element(RestOntInterfaceConstants.SUBPROPERTY);
                    subPropElem.setAttribute(RestOntInterfaceConstants.NAME, subProp.getLocalName()).
                            setAttribute(RestOntInterfaceConstants.URI, subProp.getURI());
                    subPropertiesRootElem.addContent( subPropElem);

                }
            }

            Element superPropertiesRootElem = new Element( RestOntInterfaceConstants.SUPERPROPERTIES );
            ExtendedIterator<? extends  OntProperty> superPropItr = ontProperty.listSuperProperties(true);
            while( superPropItr.hasNext() ){
                OntProperty superProp = superPropItr.next();
                if( superProp.getLocalName() != null && superProp.getURI() != null ){

                    Element superElem = new Element(RestOntInterfaceConstants.SUPERPROPERTY);
                    superElem. setAttribute(RestOntInterfaceConstants.NAME, superProp.getLocalName()).
                            setAttribute(RestOntInterfaceConstants.URI, superProp.getURI());
                    superPropertiesRootElem.addContent( superElem );

                }
            }

            propElement.addContent( domainClassRootElem ).addContent( rangeClassRootElem ).
                    addContent( declaringClassRootElem ).addContent( subPropertiesRootElem ).
                    addContent(superPropertiesRootElem);
            root.addContent( propElement );

        }

        return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );
    }


    public String getRestrictionValuesForClass( String ontologyName,
                                                String className,
                                                ServletContext context){
        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntClass ontClass = RestOntInterfaceUtil.
                getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className, true);
        ExtendedIterator <OntClass> superClassItr = ontClass.listSuperClasses(true);
        Map<String, Set<String>> restrictionValueMap = new HashMap<String, Set<String>>();
        while( superClassItr.hasNext() ){
            OntClass c = superClassItr.next();

            if( c.isRestriction()){

                Restriction r = c.as(Restriction.class);
                Set<String> restrictionValues ;

                if( restrictionValueMap.containsKey(r.getOnProperty().getLocalName()) ){
                    restrictionValues = restrictionValueMap.get( r.getOnProperty().getLocalName() );
                }else{
                    restrictionValues = new HashSet<String>();
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


    public String getAllRestrictionsForClasses( String ontologyName,
                                                String allClasses,
                                                ServletContext context){
        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        String[] classes = allClasses.split(",");
        System.out.println("Value of classes: " + allClasses);
        System.out.println("Value of classes: " + allClasses);

        if(classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }
        Map<String, Set<String>> output = new HashMap<String, Set<String>>();
        for( String className : classes){
            System.out.println(className);
            OntClass ontClass = RestOntInterfaceUtil.
                getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className, true);
            ExtendedIterator <OntClass> superClassItr = ontClass.listSuperClasses(true);
            Set<String> restrictions = new HashSet<String>();
            while( superClassItr.hasNext() ){
                OntClass c = superClassItr.next();

                if( c.isRestriction() ){
                    Restriction r = c.as(Restriction.class);
                    restrictions.add( r.getOnProperty().getLocalName() );
                }
            }
            output.put(className, restrictions);
        }

        Type mapType = new TypeToken< Map<String, Set<String>> >() {}.getType();
        return  RestOntInterfaceUtil.getJSON( output, mapType );

    }


    public String getInstancesOf( String ontologyName,
                                            String allClasses,
                                            ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        String[] classes = allClasses.split(",");
        System.out.println("Value of classes: " + allClasses);

        if(classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }

        /* Set<OntClass> ontClasses = new HashSet<OntClass>( classes.length );
        for( String className : classes){
                OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className, true);
                ontClasses.add( ontClass );
        } */

        Element root = new Element( RestOntInterfaceConstants.CLASSES );
        Document doc = new Document( root );

        for( String className : classes ){

            OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className, true);

            Element classElem = new Element( RestOntInterfaceConstants.CLASS );
            classElem.setAttribute( RestOntInterfaceConstants.NAME, ontClass.getLocalName());
            classElem.setAttribute(RestOntInterfaceConstants.URI, ontClass.getURI());

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
            classElem.addContent( instancesClassRoot );
            root.addContent(classElem);

        }
        return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );

/*        Map<String, Set<String>> output = new HashMap<String, Set<String>>();
        for( String className : classes){
            System.out.println(className);
            OntClass ontClass = RestOntInterfaceUtil.
                getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className);
            Set<String> individuals = RestOntInterfaceUtil.getIndividuals(ontClass);
            output.put( className, individuals);
        }
        return  RestOntInterfaceUtil.getJSON( output, new TypeToken< Map<String,List<String>> >() {}.getType());*/
    }

     public String createInstancesOf( String ontologyName,
                                            String allClasses,
                                            InputStream inputXML,
                                            ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModelWrapper modelWrapper  = RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName);

        String[] classesArray = allClasses.split( RestOntInterfaceConstants.COMMA_DELIMITER );
        System.out.println("Value of classes: " + allClasses);

        if( classesArray == null || classesArray.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }

        SAXBuilder builder = new SAXBuilder();
        try {

            Document inputDoc = builder.build( inputXML );
            Element classesRoot = inputDoc.getRootElement();

            if( classesRoot == null ){
                String exp =   " The request body is not in correct format. ";
                RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                throw new NotFoundException( exp );
            }

            List<Element> childElements = classesRoot.getChildren();
            for( Element classElement : childElements){

                String classLocalName = classElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                if( modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() +"#" + classLocalName ) == null ){
                      String exp =   classLocalName + " does not exists in the  " + ontologyName + " ontology ";
                      RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                      throw new BadRequestException( exp );
                }

                OntClass ontClass = RestOntInterfaceUtil.getClass( modelWrapper, classLocalName, true);
               // modelWrapper.getOntModel().createClass( modelWrapper.getURI() + "#" + classLocalName );

                 if( classElement.getChild( RestOntInterfaceConstants.INSTANCES) == null ){
                    List<Element> instanceElementList = classElement.getChild( RestOntInterfaceConstants.INSTANCES ).getChildren();
                    for( Element instancesElement : instanceElementList ){

                        validateNameAttribute( instancesElement, "The Instance tag does not have name attribute.");

                        String instanceName = instancesElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                        Individual instance = RestOntInterfaceUtil.getIndividual( modelWrapper, instanceName, false);
                        //modelWrapper.getOntModel().getIndividual( modelWrapper.getURI() + "#" + instanceName);
                        if( instance == null ){

                            ontClass.createIndividual( modelWrapper.getURI() + "#" + instanceName);

                        } else if( !instance.isIndividual() ){

                            String exp =   instanceName + " does not exist in the  " + modelWrapper.getOntologyName() + " ontology ";
                                 exp = exp + " and there is other individual with the same name present .";
                            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                            throw new BadRequestException( exp );

                         }else{

                            String exp =   instanceName + " already exists in the  " + modelWrapper.getOntologyName() + " ontology ";
                            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                            throw new BadRequestException( exp );

                        }
                    }
                }


            }

        } catch (JDOMException e) {
            RestOntInterfaceUtil.log(SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        } catch (IOException e) {
            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        }

        return "";

     }

   public String updateInstancesOf( String ontologyName,
                                            String allClasses,
                                            InputStream inputXML,
                                            ServletContext context){

       OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
              OntModelWrapper modelWrapper  = RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName);

              String[] classesArray = allClasses.split( RestOntInterfaceConstants.COMMA_DELIMITER );
              System.out.println("Value of classes: " + allClasses);

              if( classesArray == null || classesArray.length == 0){
                  String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
                  RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                  throw new NotFoundException( exp );
              }

              SAXBuilder builder = new SAXBuilder();
              try {

                  Document inputDoc = builder.build( inputXML );
                  Element classesRoot = inputDoc.getRootElement();

                  if( classesRoot == null ){
                      String exp =   " The request body is not in correct format. ";
                      RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                      throw new NotFoundException( exp );
                  }

                  List<Element> childElements = classesRoot.getChildren();
                  for( Element classElement : childElements){

                      validateNameAttribute( classElement, "The Class tag does not have name attribute.");

                       String classLocalName = classElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                      /* if( modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() +"#" + classLocalName ) == null ){
                            String exp =   classLocalName + " does not exists in the  " + ontologyName + " ontology ";
                            RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                            throw new BadRequestException( exp );
                       }*/

                       OntClass updateClass = RestOntInterfaceUtil.getClass(modelWrapper, classLocalName, true);
                       //modelWrapper.getOntModel().createClass( modelWrapper.getURI() + "#" + classLocalName );


                       if( classElement.getChild( RestOntInterfaceConstants.INSTANCES ) != null ){

                            List<Element> instanceElementList = classElement.getChild( RestOntInterfaceConstants.INSTANCES ).getChildren();
                            for( Element instancesElement : instanceElementList ){

                                validateNameAttribute( instancesElement, "The Instance tag does not have name attribute.");

                                //Get the name of the instance that has to be update
                                 String instanceName = instancesElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                                //Validate if it exist in the ontology
                                Individual individual = RestOntInterfaceUtil.getIndividual(modelWrapper, instanceName, false);
                                //modelWrapper.getOntModel().getIndividual( modelWrapper.getURI() + "#" + instanceName);

                                //If the individual does not exist in the ontology throw an exception : 400 BadRequest
                                if( individual == null ){

                                    String exp =   instanceName + "  does not exists in the  " + modelWrapper.getOntologyName() + " ontology ";
                                    RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                                    throw new BadRequestException( exp );

                                    //Validate if the subClass has the relation subClassOf with the class that we are updating.
                                } else if ( ! individual.isIndividual()  || !individual.hasOntClass( updateClass )){

                                    //If it is not a subClass throw exception: 400 BadRequest
                                    String exp =   instanceName + " is  not  an instance of "+ classLocalName +
                                            " the  " + modelWrapper.getOntologyName() + " ontology ";
                                    RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                                    throw new BadRequestException( exp );

                                }

                               //Check if there is an Update tag in the request body, if not throw exception: 400 BadRequest
                                if( instancesElement.getChild( RestOntInterfaceConstants.UPDATE ) == null ){

                                    String exp = "The Instance tag doesn't have an Update tag";
                                    RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                                    throw new BadRequestException( exp );

                                }

                                validateNameAttribute( instancesElement.getChild( RestOntInterfaceConstants.UPDATE ),
                                        " The Update tag does not have name attribute");

                                //Check for name attribute in Update tag, if not there throw exception: 400 BadRequest
                                String updateInstanceName = instancesElement.getChild( RestOntInterfaceConstants.UPDATE ).
                                        getAttribute(RestOntInterfaceConstants.NAME).getValue();

                                if( updateInstanceName == null ){

                                    String exp = "The Update tag in SubClass tag doesn't have name attribute.";
                                    RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                                    throw new BadRequestException( exp );

                                }

                                //Check if the class mentioned in update tag exists in the ontology, if not throw exception: 400 BadRequest
                                //OntClass updateSubClass = modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() + "#" + instanceName);
                                Individual updateInstance = RestOntInterfaceUtil.getIndividual(modelWrapper, updateInstanceName, false);
                                //modelWrapper.getOntModel().getIndividual( modelWrapper.getURI() + "#" + updateInstanceName);
                                if( updateInstance != null ){
                                    String exp = updateInstanceName + " is already present in  " + modelWrapper.getOntologyName() + "ontology.";
                                    exp = exp + "You need to provide a non-existent instance.";
                                    throw  new BadRequestException( exp );
                                }

                                //If all above validations pass, remove the current individual and add the new one.
                                updateClass.dropIndividual( individual );
                                updateClass.createIndividual( modelWrapper.getURI() + "#" + updateInstanceName);
                            }

                       }

                  }

              } catch (JDOMException e) {
                 RestOntInterfaceUtil.log(SchemaInfoService.class.getName(), e);
                  throw new BadRequestException( "There was a problem while parsing the request body.");
              } catch (IOException e) {
                RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
                  throw new BadRequestException( "There was a problem while parsing the request body.");
              }

        return "";

   }



    public String getAllEnumeratedClasses( String ontologyName,
                                            ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModel model = RestOntInterfaceUtil.getOntModel( ontologyModelStore, ontologyName ).getOntModel();
        Set<String> enumClassSet = new HashSet<String>();
        ExtendedIterator <EnumeratedClass> enumItr =  model.listEnumeratedClasses();
        while( enumItr.hasNext() ){
            enumClassSet.add( enumItr.next().getURI() );
        }
        return  RestOntInterfaceUtil.getJSON( enumClassSet, new TypeToken<Set<String>>() {}.getType());

    }

    public String getAllEnumeratedClassInstances( String ontologyName,
                                                  ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModel model = RestOntInterfaceUtil.getOntModel( ontologyModelStore, ontologyName ).getOntModel();
        Map<String, Set<String>> enumInstanceMap = new HashMap<String, Set<String>>();
        ExtendedIterator <EnumeratedClass> enumItr =  model.listEnumeratedClasses();
        while( enumItr.hasNext() ){
            EnumeratedClass enumClass = enumItr.next();
            ExtendedIterator <? extends  OntResource> instanceItr = enumClass.listOneOf();
            Set<String> enumClassSet = new HashSet<String>();
            while( instanceItr.hasNext() ){
                enumClassSet.add( instanceItr.next().getURI() );
            }
            enumInstanceMap.put(enumClass.getURI(), enumClassSet );
        }
        return  RestOntInterfaceUtil.getJSON( enumInstanceMap, new TypeToken<Set<String>>() {}.getType());
    }

    public String getInstancesOfEnumeratedClass( String ontologyName,
                                                 String allClasses,
                                                 ServletContext context ){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        String[] classes = allClasses.split(",");
        System.out.println("Value of classes: " + allClasses);

        if(classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }
        Map<String, Set<String>> output = new HashMap<String, Set<String>>();
        for( String enumClass : classes){
            OntClass enumOntClass = RestOntInterfaceUtil.
                getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), enumClass, true);
            if( !enumOntClass.isEnumeratedClass() ){
                String exp =  enumClass + " is not an enumerated class in " + ontologyName + " ontology ";
                RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                throw new NotFoundException( exp );
            }
            Set<String> enumClassSet = new HashSet<String>();
            ExtendedIterator<? extends OntResource> enumItr =  enumOntClass.as(EnumeratedClass.class).listOneOf();
            while( enumItr.hasNext() ){
                if( enumItr.next().getURI() != null )
                    enumClassSet.add( enumItr.next().getURI() );
            }
            output.put( enumClass, enumClassSet);
        }
        return RestOntInterfaceUtil.getJSON(output, new TypeToken< Map<String,Set<String>> >() {}.getType());
    }

    public String getDomainOfProperties( String ontologyName,
                                         String allProperties,
                                         ServletContext context){
        OntModelWrapper ontModelWrapper = RestOntInterfaceUtil.getOntModel( (OntologyModelStore)context.getAttribute("ontologyModelStore"), ontologyName );
        String[] properties = allProperties.split(",");
        System.out.println("Value of properties: " + allProperties);

        if(properties == null || properties.length == 0){
            String exp =   " The properties mentioned in the URL are not formatted properly. Please send the properties , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }
        Map<String, Set<String>> output = new HashMap<String, Set<String>>();
        for( String property : properties){
            OntProperty ontProperty = RestOntInterfaceUtil.getProperty( ontModelWrapper, property, true);
            /*ontModelWrapper.getOntModel().getOntProperty( ontModelWrapper.getURI() + "#" + property);

            if( ontProperty == null){
                String exp =  property + " does not exist in " + ontologyName + " ontology ";
                RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
                throw new NotFoundException( exp );
            } */
            Set<String>domains = new HashSet<String>();
            ExtendedIterator<? extends OntResource> domItr  = ontProperty.listDomain();
            while( domItr.hasNext() ){
                OntResource domain = domItr.next();
                if( domain.getURI() != null )
                     domains.add( domain.getURI() );
            }
            output.put( property, domains);
        }
        return RestOntInterfaceUtil.getJSON(output, new TypeToken<Map<String, Set<String>>>() {
        }.getType());
    }

     public String deleteClasses( String ontologyName,
                                    String allClasses,
                                    ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );

        String[] classes = allClasses.split(",");
        System.out.println("Value of classes: " + allClasses);

        if( classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }

        for( String className : classes){
            System.out.println(className);
            OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className, true);
            ontClass.remove();
        }
        return "";
    }


    public String deleteSubClassesOf( String ontologyName,
                                    String allClasses,
                                    ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );

        String[] classes = allClasses.split(",");
        System.out.println("Value of classes: " + allClasses);

        if( classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }

        Set<OntClass> subClassSet = new HashSet<OntClass>();
        for( String className : classes){
            System.out.println(className);
            OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className, true);
            ExtendedIterator <OntClass> subClassItr = ontClass.listSubClasses( true );

             while( subClassItr.hasNext() ){
                    OntClass subClass = subClassItr.next();
                    if( subClass.getLocalName() != null )
                        subClassSet.add( subClass );
             }
        }

        deleteClasses( subClassSet );
        return "";
    }

    public String deleteSuperClassesOf( String ontologyName,
                                      String allClasses,
                                      ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );

        String[] classes = allClasses.split(",");
        System.out.println("Value of classes: " + allClasses);

        if( classes == null || classes.length == 0){
            String exp =   " The classes mentioned in the URL are not formatted properly. Please send the classes , separated ";
            RestOntInterfaceUtil.log( RestOntInterfaceUtil.class.getName(), new NotFoundException(exp) );
            throw new NotFoundException( exp );
        }

        Set<OntClass> subClassSet = new HashSet<OntClass>();
        for( String className : classes){
            System.out.println(className);
            OntClass ontClass = RestOntInterfaceUtil.
                    getClass(RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName), className, true);
            ExtendedIterator <OntClass> subClassItr = ontClass.listSuperClasses(true);

             while( subClassItr.hasNext() ){
                    OntClass subClass =  subClassItr.next();
                    if( subClass.getLocalName() != null )
                        subClassSet.add( subClass );
             }
        }

        deleteClasses( subClassSet );
        return "";
    }

   /* public abstract String outputClasses( Set<OntClass> ontClassList);

    public abstract String outputProperties( Set<OntProperty> ontPropertyList);
     */

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


    private void updateSuperClasses(String ontologyName, OntModelWrapper modelWrapper,
                                    String classLocalName, OntClass updateClass,
                                    List<Element> superClassElementList) {

        for( Element superClassElement : superClassElementList ){

            //Get the name of the superClass that has to be update
            String superClassName = superClassElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

            //Validate if it exist in the ontology
            OntClass superClass = modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() + "#" + superClassName);

            //If the superClass does not exist in the ontology throw an exception : 400 BadRequest
            if( superClass == null ){

                String exp =   superClassName + " does not exists in the  " + modelWrapper.getOntologyName() + " ontology ";
                RestOntInterfaceUtil.log(SchemaInfoService.class.getName(), new BadRequestException(exp));
                throw new BadRequestException( exp );

                //Validate if the superClass has the relation superClassOf with the class that we are updating.
            } else if ( ! updateClass.hasSuperClass(superClass) ){

                //If it is not a superClass throw exception: 400 BadRequest
                String exp =   superClassName + " is  not  listed as super-class of "+ classLocalName +
                        " the  " + ontologyName + " ontology ";
                RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                throw new BadRequestException( exp );

            }

            //Check if there is an Update tag in the request body, if not throw exception: 400 BadRequest
            if( superClassElement.getChild( RestOntInterfaceConstants.UPDATE ) == null ){

                String exp = "The SuperClass tag doesn't have an Update tag";
                RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                throw new BadRequestException( exp );

            }

            //Check for name attribute in Update tag, if not there throw exception: 400 BadRequest
            String updateSuperClassName = superClassElement.getChild( RestOntInterfaceConstants.UPDATE ).
                    getAttribute(RestOntInterfaceConstants.NAME).getValue();

            if( updateSuperClassName == null ){

                String exp = "The Update tag in SuperClass tag doesn't have name attribute.";
                RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                throw new BadRequestException( exp );

            }

            //Check if the class mentioned in update tag exists in the ontology, if not throw exception: 400 BadRequest
            OntClass updateSuperClass = modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() + "#" + superClassName);
            if( updateSuperClass == null ){
                String exp = updateSuperClassName + " is not present in the " + modelWrapper.getOntologyName() + "ontology.";
                exp = exp + "Please use the create class service to create this class.";
            }

            //If all above validations pass, remove the current subclass and add the new one.

            updateClass.removeSuperClass(superClass);
            updateClass.addSuperClass( updateSuperClass );


        }
    }

    private void updateSubClasses(OntModelWrapper modelWrapper, String classLocalName, OntClass updateClass, List<Element> subClassElementList) {
        for( Element subClassElement : subClassElementList ){

                //Get the name of the subClass that has to be update
                String subClassName = subClassElement.getAttribute( RestOntInterfaceConstants.NAME ).getValue();

                //Validate if it exist in the ontology
                OntClass subClass = modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() + "#" + subClassName);

                //If the subClass does not exist in the ontology throw an exception : 400 BadRequest
                if( subClass == null ){

                    String exp =   subClassName + " does not exists in the  " + modelWrapper.getOntologyName() + " ontology ";
                    RestOntInterfaceUtil.log(SchemaInfoService.class.getName(), new BadRequestException(exp));
                    throw new BadRequestException( exp );

                //Validate if the subClass has the relation subClassOf with the class that we are updating.
                } else if ( ! updateClass.hasSubClass( subClass ) ){

                    //If it is not a subClass throw exception: 400 BadRequest
                     String exp =   subClassName + " is  not  listed as sub-class of "+ classLocalName +
                             " the  " + modelWrapper.getOntologyName() + " ontology ";
                     RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                     throw new BadRequestException( exp );

                }

                //Check if there is an Update tag in the request body, if not throw exception: 400 BadRequest
                if( subClassElement.getChild( RestOntInterfaceConstants.UPDATE ) == null ){

                    String exp = "The SubClass tag doesn't have an Update tag";
                    RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                    throw new BadRequestException( exp );

                }

                //Check for name attribute in Update tag, if not there throw exception: 400 BadRequest
                String updateSubClassName = subClassElement.getChild( RestOntInterfaceConstants.UPDATE ).
                        getAttribute(RestOntInterfaceConstants.NAME).getValue();

                if( updateSubClassName == null ){

                    String exp = "The Update tag in SubClass tag doesn't have name attribute.";
                    RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), new BadRequestException( exp ));
                    throw new BadRequestException( exp );

                }

            //Check if the class mentioned in update tag exists in the ontology, if not throw exception: 400 BadRequest
                OntClass updateSubClass = modelWrapper.getOntModel().getOntClass( modelWrapper.getURI() + "#" + subClassName);
                if( updateSubClass == null ){
                    String exp = updateSubClassName + " is not present in the " + modelWrapper.getOntologyName() + "ontology.";
                           exp = exp + "Please use the create class service to create this class.";
                }

                //If all above validations pass, remove the current subclass and add the new one.

                updateClass.removeSubClass(subClass);
                updateClass.addSubClass(updateSubClass);
        }
    }

    private void validateNameAttribute(Element classElement, String exp) {
        if( classElement.getAttributeValue( RestOntInterfaceConstants.NAME ) == null ){

            RestOntInterfaceUtil.log(SchemaInfoService.class.getName(), new NotFoundException(exp));
            throw new BadRequestException( exp );

        }
    }


}
