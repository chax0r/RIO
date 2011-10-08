package edu.uga.cs.restendpoint.utils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 10/6/11
 * Time: 5:54 PM
 * Email: <kale@cs.uga.edu>
 */
public class OntologyModelStore {
    private HashMap<String, OntModel> ontModelSet;

    public OntologyModelStore( ) {
        this.ontModelSet = new HashMap<String, OntModel>( );
    }

    public void populateOntologyStoreFromFile( String fileName, InputStream is ){
            OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
            m.read(is , "");
            this.ontModelSet.put( fileName, m);
    }

    public void populateOntologyStoreFromURI( List<String> uriList){

        for( String uri : uriList ){
            OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
            m.read(uri , "");
            this.ontModelSet.put( uri, m);
        }
    }
    /**
     * This method is used to added ontologies to the ontology server.
     * @param uri: The uri for the ontology that is to be added.
     */
    public void addOntologyModel( String uri ){

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
                 m.read( uri, "");
        this.ontModelSet.put( uri, m);

    }

    /**
     *
     * @param uri : The uri that represents the ontology.
     * @return: The Jena ontology model <OntModel> class reference
     */
    public OntModel getOntologyModel( String uri ){

        return this.ontModelSet.get( uri );

    }

}
