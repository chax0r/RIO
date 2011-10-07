/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uga.cs.restendpoint.service;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;
import edu.uga.cs.restendpoint.utils.OntologyModelStore;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kale
 */
@Path("/")
public class SPARQLQueryService {
private  String ontologyName;

    public SPARQLQueryService() {
    }

    @GET
    @Path("{ontologyName}/execute")
    @Produces("text/html")
    public String executeQuery ( @PathParam("expName") String ontologyName,
                                 @Context ServletContext context,
                                 String queryString ){
        System.out.println( "Name of the ontology: " + ontologyName);

        OntologyModelStore ontologyModelStore = (OntologyModelStore) context.getAttribute( "ontologyModelStore" );
        OntModel model = ontologyModelStore.getOntologyModel( "ontologyName" );
		try {
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
			Prologue prologue = new Prologue( prefixMap );
			String output = ResultSetFormatter.asText( results, prologue );
			System.out.println("Output: "+output);
		} catch(QueryException qe){

            Logger.getLogger(SPARQLQueryService.class.getName()).log(Level.SEVERE, null, qe);
		}catch (FileNotFoundException ex) {

            Logger.getLogger(SPARQLQueryService.class.getName()).log(Level.SEVERE, null, ex);
		}catch (IOException ex) {

            Logger.getLogger(SPARQLQueryService.class.getName()).log(Level.SEVERE, null, ex);
		}
        return null;
    }
}
