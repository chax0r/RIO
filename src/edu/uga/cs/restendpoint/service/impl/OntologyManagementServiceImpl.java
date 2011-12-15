package edu.uga.cs.restendpoint.service.impl;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;
import edu.uga.cs.restendpoint.exceptions.BadRequestException;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;
import edu.uga.cs.restendpoint.service.api.OntologyManagementService;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceConstants;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/17/11
 * Time: 12:05 AM
 * Email: <kale@cs.uga.edu>
 */
public class OntologyManagementServiceImpl implements OntologyManagementService {

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

    public String getOntology( String ontologyName, HttpServletResponse httpResponse, ServletContext servletContext) {

        OntologyModelStore ontologyModelStore = (OntologyModelStore) servletContext.getAttribute( "ontologyModelStore" );

        OntModelWrapper ontModelWrapper  = RestOntInterfaceUtil.getOntModel(ontologyModelStore, ontologyName);

        Element ontologyElement = new Element(RestOntInterfaceConstants.ONTOLOGY );
        ontologyElement.setAttribute( RestOntInterfaceConstants.NAME, ontModelWrapper.getOntologyName() ).
                setAttribute( RestOntInterfaceConstants.URI, ontModelWrapper.getURI() );


        try {
            ServletOutputStream os = httpResponse.getOutputStream();
            ontModelWrapper.getOntModel().write( os );
        } catch (IOException e) {
            RestOntInterfaceUtil.log(OntologyManagementServiceImpl.class.getName(), e);
        }

        return "";

    }

    public String addOntology(String ontologyName, InputStream request, ServletContext context) {

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read( request, "");
         for( Map.Entry<String, String>ns : model.getNsPrefixMap().entrySet()){
                System.out.println("Key: " + ns.getKey());
                System.out.println("Value: " + ns.getValue());
            }
        String modelURI = model.getNsPrefixURI( ontologyName );
        if( modelURI == null ){
            String exp = " No namespace URI for the ontology specified.";
            RestOntInterfaceUtil.log( OntologyManagementServiceImpl.class.getName(), exp);
            throw new BadRequestException( exp );
        }

        OntModelWrapper modelWrapper = new OntModelWrapper(model, modelURI, ontologyName);

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute("ontologyModelStore");
        ontologyModelStore.addOntologyModel( ontologyName, modelWrapper );

        return "";
    }

    public String removeOntology(String ontologyName,  ServletContext context) {


        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute("ontologyModelStore");

        OntModelWrapper modelWrapper = ontologyModelStore.getOntologyModel( ontologyName);

        if( modelWrapper != null ){
            ontologyModelStore.remove ( ontologyName );
        }

        return "";
    }

    public String validateOntology (String ontologyName, ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute("ontologyModelStore");

        OntModelWrapper modelWrapper = ontologyModelStore.getOntologyModel( ontologyName);

        Reasoner reasoner = ReasonerRegistry.getOWLMicroReasoner();

        InfModel infModel = ModelFactory.createInfModel( reasoner, modelWrapper.getOntModel() );

        ValidityReport validityReport = infModel.validate();

        Element root = new Element("Reports");

        Iterator<ValidityReport.Report> itr = validityReport.getReports();

        while( itr.hasNext()){

            ValidityReport.Report report = itr.next();

            Element typeElement = new Element("Type");
            typeElement.addContent( report.getType() );

            Element description = new Element("Description");
            description.addContent( report.description);


            root.addContent( new Element("Report").addContent( typeElement).addContent( description ) );
        }

        Document doc = new Document( root );
        return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );

    }



    public String addNameSpace( String ontologyName, String nameSpace, ServletContext context){


        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute("ontologyModelStore");

        OntModelWrapper modelWrapper = ontologyModelStore.getOntologyModel( ontologyName);






        return Response.Status.CREATED.toString();
    }


    public String getNameSpace( String ontologyName, String nameSpace, ServletContext context){

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute("ontologyModelStore");

        OntModelWrapper modelWrapper = ontologyModelStore.getOntologyModel( ontologyName);


        return "";

    }
}
