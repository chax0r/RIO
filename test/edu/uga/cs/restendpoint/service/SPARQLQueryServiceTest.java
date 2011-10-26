package edu.uga.cs.restendpoint.service;


import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.rdf.model.Model;
import java.io.FileNotFoundException;
import java.io.InputStream;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.FileInputStream;
import org.junit.Test;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.lang.StringBuilder;
import static org.junit.Assert.*;

/*
 *
 * @author: CK <kale@cs.uga.edu>
 */
public class SPARQLQueryServiceTest {


	//@Test
	public void testSPARQLQuery(){
		try {
			URL 			  url 	 	 = new URL( "http://localhost:8080/restonto/sparql" );
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
							  connection.setRequestMethod( "POST" );
							  connection.setRequestProperty( "Content-Type", "text" );
							  connection.setDoOutput( true );
							  connection.setInstanceFollowRedirects( false );
			StringBuilder sparqlQuery = new StringBuilder();
			//type in your query here
			//

			OutputStream os = connection.getOutputStream();
						 os.write( sparqlQuery.toString().getBytes() );
						 os.flush();

			assertEquals( HttpURLConnection.HTTP_CREATED, connection.getResponseCode() );
			connection.disconnect();
		} 
		catch (MalformedURLException ex) {
			Logger.getLogger(SPARQLQueryServiceTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (IOException ex) {
			Logger.getLogger(SPARQLQueryServiceTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		
	}

	@Test
	public void executeQuery(){
		InputStream is = null;
		try {
            is = new FileInputStream("src/resources/pizza.owl");
            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
            String source = "http://www.co-ode.org/ontologies/pizza/ ";
			model.read(is, "");
			is.close();

			/*StringBuilder queryString = new StringBuilder();
			queryString.append( "PREFIX rdf: http://www.w3.org/1999/02/22-rdf-syntax-ns# \n" ).
				append(" PREFIX pizza: http://www.co-ode.org/ontologies/pizza/pizza.owl# \n").
				append("SELECT * WHERE { ?s rdfs:subclassof :NamedPizza }");*/
            String queryString = " PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    " PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    " PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>\n" +
                    " PREFIX owl:   <http://www.w3.org/2000/07/owl#>\n" +
                    " PREFIX  p:    <http://www.co-ode.org/ontologies/pizza/pizza.owl#>\n" +
                    " SELECT DISTINCT  *\n" +
                    " WHERE\n" +
                    "  { ?targetPizza    rdfs:subClassOf   _:pizza .\n" +
                    "    \n" +
                    " }";

			
			
	    		Query query = QueryFactory.create( queryString );
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

		    	QueryExecution qexec = QueryExecutionFactory.create( query, model );

			ResultSet results =  qexec.execSelect();
			Prologue prologue = new Prologue( prefixMap );
			String output = ResultSetFormatter.asText( results, prologue );
			System.out.println("Output: "+output);
		} catch(QueryException qe){
				Logger.getLogger(SPARQLQueryServiceTest.class.getName()).log(Level.SEVERE, null, qe);
			} catch (IOException e) {
            Logger.getLogger(SPARQLQueryServiceTest.class.getName()).log(Level.SEVERE, null, e);
        }

    }

} 