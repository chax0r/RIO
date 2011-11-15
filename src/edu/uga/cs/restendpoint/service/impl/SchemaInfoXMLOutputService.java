package edu.uga.cs.restendpoint.service.impl;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/5/11
 * Time: 10:14 PM
 * Email: <kale@cs.uga.edu>
 */
public class SchemaInfoXMLOutputService {
    public String outputClasses(Set<OntClass> ontClassList) {
        Element root = new Element("Classes");
        Document doc = new Document( root );

        for( OntClass ontClass : ontClassList ){
            Element classElem = new Element("Class");
            classElem.setAttribute("name", ontClass.getLocalName());
            classElem.setAttribute("uri", ontClass.getURI());
            root.addContent(classElem);

            /*------------------ Get all SuperClasses of the Class -----------------*/
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

            /*------------------ Get all SubClasses of the Class -----------------*/
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

            /*------------------ Get all Instances of the Class -----------------*/
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

            /*------------------ Get all Properties of the Class -----------------*/
            Element propertiesElem = new Element( "Properties" );
            ExtendedIterator<OntProperty> propItr = ontClass.listDeclaredProperties( true );
            while ( propItr.hasNext() ){
                OntProperty property = propItr.next();
                if( property.getLocalName()!= null && property.getURI() != null ){
                    Element propElem = new Element( "Property" );
                    propElem.setAttribute( "name", property.getLocalName() );
                    propElem.setAttribute( "uri", property.getURI() );
                }
            }

            classElem.addContent(subClassRoot);
            classElem.addContent(superClassRoot);
            classElem.addContent(instancesClassRoot);
            classElem.addContent( propertiesElem );
        }

            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
            return xmlOutputter.outputString( doc );

    }

    public String outputProperties(Set<OntProperty> ontPropertyList) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
