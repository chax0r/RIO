/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uga.cs.restendpoint.service.impl;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;
import edu.uga.cs.restendpoint.exceptions.BadRequestException;
import edu.uga.cs.restendpoint.exceptions.NotFoundException;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;
import edu.uga.cs.restendpoint.model.SparqlQueryResultStore;
import edu.uga.cs.restendpoint.service.api.SPARQLQueryService;
import edu.uga.cs.restendpoint.service.api.SchemaInfoService;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceConstants;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.joda.time.DateTime;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Author: kale
 * Date: 10/16/11
 * Time: 10:59 PM
 * Email: <kale@cs.uga.edu>
 */

//TODO: 1) Exception Handling
//      2) Return appropriate
public class SPARQLQueryServiceImpl implements SPARQLQueryService {


    public String executeQuery ( String ontologyName,
                                 ServletContext context,
                                 String queryString){


        SAXBuilder builder = new SAXBuilder();

         //try {

          //  Document inputDoc = builder.build( inputXML );
           // Element queryRoot = inputDoc.getRootElement();

           /* if( queryRoot == null ){
                String exp =   " The request body is not in correct format. ";
                RestOntInterfaceUtil.log(SPARQLQueryServiceImpl.class.getName(), new NotFoundException(exp));
                throw new BadRequestException( exp );
            }

            if( queryRoot.getContent().size() != 1){

                String exp =   " The request body is not in correct format. ";
                RestOntInterfaceUtil.log(SPARQLQueryServiceImpl.class.getName(), new NotFoundException(exp));
                throw new BadRequestException( exp );

            }

            String queryString = queryRoot.getContent().get(0).toString();
             */
            System.out.println( "Name of the ontology: " + ontologyName);
            System.out.println( "Data posted: " + queryString);


            OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
            OntModelWrapper ontModelWrapper = ontologyModelStore.getOntologyModel( ontologyName );

            //TODO: Throw appropriate exception and return HTTP code.
            if( ontModelWrapper == null){
                System.out.println( ontologyName +  " is not loaded in the server" );
                return "";
            }
            OntModel model = ontModelWrapper.getOntModel();
/*
			StringBuilder queryString = new StringBuilder();
			queryString.append( "PREFIX rdf: http://www.w3.org/1999/02/22-rdf-syntax-ns#" ).
				append( " PREFIX pizza: http://www.co-ode.org/ontologies/pizza/pizza.owl# " ).
				append( "SELECT * WHERE { ?s rdfs:subclassof :NamedPizza }" );
  */


	    		Query query = QueryFactory.create(queryString);
		    	System.out.println( "Query>>" + query + "<<" );
		    	System.out.flush();
	    		PrefixMapping prefixMap = query.getPrefixMapping();
			if( prefixMap != null ) {
				System.out.println( "Have prefixMap" );
				System.out.flush();
			} else {
				System.out.println( "Have no prefixMap" );
				System.out.flush();
	   		}

		    QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results =  qexec.execSelect();

            SparqlQueryResultStore sparqlQueryResultStore =
                        new SparqlQueryResultStore(UUID.randomUUID().toString(),
                                                    results,
                                                   new Prologue(prefixMap),
                                                   new DateTime().getMillis() );
			//Prologue prologue = new Prologue( prefixMap );
			//String output = ResultSetFormatter.asText( results, prologue );

        @SuppressWarnings("unchecked")
            Map<String, SparqlQueryResultStore> sparqlQueryResultStoreMap = (Map<String, SparqlQueryResultStore>) context.getAttribute("sparqlQueryResultStoreMap");

            synchronized ( sparqlQueryResultStoreMap ){
                sparqlQueryResultStoreMap.put( sparqlQueryResultStore.getId(), sparqlQueryResultStore );
                sparqlQueryResultStoreMap.notifyAll();
            }

            Element URI = new Element("URI");
             String resultURI = "http://localhost:8080/rio/sparqlService/"+sparqlQueryResultStore.getId();
             URI.addContent( resultURI );
            Element timeOutElement = new Element("TimeOut");
            timeOutElement.addContent( Long.toString( RestOntInterfaceConstants.cacheTimeOut ) );
            Element output = new Element("ResultSet");
             output.addContent(URI);
             output.addContent(timeOutElement);

             Document doc = new Document( output );
             return new XMLOutputter( Format.getPrettyFormat() ).outputString( doc );

      /*  } catch (JDOMException e) {
                    RestOntInterfaceUtil.log(SchemaInfoService.class.getName(), e);
                    throw new BadRequestException( "There was a problem while parsing the request body.");
                } catch (IOException e) {
                    RestOntInterfaceUtil.log( SchemaInfoService.class.getName(), e);
            throw new BadRequestException( "There was a problem while parsing the request body.");
        }*/
    }


    public  String getResult( String identifier,  ServletContext context){


        @SuppressWarnings("unchecked")
        Map<String, SparqlQueryResultStore> sparqlQueryResultStoreMap = (Map<String, SparqlQueryResultStore>) context.getAttribute("sparqlQueryResultStoreMap");

        synchronized ( sparqlQueryResultStoreMap ){

          SparqlQueryResultStore sparqlQueryResultStore = sparqlQueryResultStoreMap.get( identifier );

          long diff =   new DateTime().getMillis() - sparqlQueryResultStore.getCreateTime() ;

          if( diff <= RestOntInterfaceConstants.cacheTimeOut ){

            return ResultSetFormatter.asXMLString( sparqlQueryResultStore.getResultSet() );

          }
        }

        throw new NotFoundException(" The requested resource does not exist on the server");
    }
}
