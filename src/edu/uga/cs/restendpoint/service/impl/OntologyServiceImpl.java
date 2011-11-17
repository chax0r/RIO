package edu.uga.cs.restendpoint.service.impl;

import com.hp.hpl.jena.ontology.OntModel;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;
import edu.uga.cs.restendpoint.service.api.OntologyService;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceConstants;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.servlet.ServletContext;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/17/11
 * Time: 12:05 AM
 * Email: <kale@cs.uga.edu>
 */
public class OntologyServiceImpl implements OntologyService{

    public String getAllOntologies(String ontologyName, ServletContext servletContext) {

        OntologyModelStore ontologyModelStore = (OntologyModelStore) servletContext.getAttribute( "ontologyModelStore" );

        Element root = new Element( RestOntInterfaceConstants.ONTOLOGIES );
        Document doc = new Document( root );
        for( Map.Entry<String, OntModelWrapper> ontModelWrapperEntry : ontologyModelStore.getOntModelSet().entrySet() ){

            Element ontologyElement = new Element(RestOntInterfaceConstants.ONTOLOGY );

            ontologyElement.setAttribute( RestOntInterfaceConstants.NAME, ontModelWrapperEntry.getValue().getOntologyName() ).
            setAttribute( RestOntInterfaceConstants.URI, ontModelWrapperEntry.getValue().getURI() );

            root.addContent( ontologyElement );
        }
        return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );
    }

    public String getOntology( String ontologyName, ServletContext servletContext) {

        OntologyModelStore ontologyModelStore = (OntologyModelStore) servletContext.getAttribute( "ontologyModelStore" );

        OntModelWrapper ontModelWrapper  = RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName);

        Element ontologyElement = new Element(RestOntInterfaceConstants.ONTOLOGY );
        ontologyElement.setAttribute( RestOntInterfaceConstants.NAME, ontModelWrapper.getOntologyName() ).
                setAttribute( RestOntInterfaceConstants.URI, ontModelWrapper.getURI() );

        Document doc = new Document( ontologyElement );
        return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );

    }

    public String addOntology(InputStream request, @Context ServletContext context) {
        return "";
    }
}
