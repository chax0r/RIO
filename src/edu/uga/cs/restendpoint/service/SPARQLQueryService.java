/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uga.cs.restendpoint.service;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;

/**
 * Author: kale
 * Date: 10/16/11
 * Time: 10:59 PM
 * Email: <kale@cs.uga.edu>
 */

//TODO: 1) Exception Handling
//      2) Return appropriate
@Path("/query")
public class SPARQLQueryService {

    public SPARQLQueryService() {
    }

    @POST
    @Path("{ontologyName}/execute")
    @Produces("text/html")
    public String executeQuery ( @PathParam("ontologyName") String ontologyName,
                                 @Context ServletContext context,
                                 String queryString ){
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
			Prologue prologue = new Prologue( prefixMap );
			String output = ResultSetFormatter.asText( results, prologue );
			System.out.println("Output: "+output);
        return "";
    }
}
