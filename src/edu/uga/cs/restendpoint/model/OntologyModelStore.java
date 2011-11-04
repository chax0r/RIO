package edu.uga.cs.restendpoint.model;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Author: kale
 * Date: 10/6/11
 * Time: 5:54 PM
 * Email: <kale@cs.uga.edu>
 */
public class OntologyModelStore {
    private HashMap<String, OntModelWrapper> ontModelSet;

    public OntologyModelStore( ) {
        this.ontModelSet = new HashMap<String, OntModelWrapper>( );
    }

    public void populateOntologyStoreFromFile( String ontName, InputStream is, String URI ){
            OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF, null);
            m.read(is , "");
            OntModelWrapper ow = new OntModelWrapper(m, URI, ontName);
            this.ontModelSet.put( ontName, ow);
    }

    public void populateOntologyStoreFromURI( List<String> uriList){

        for( String uri : uriList ){
            OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF, null);
            m.read(uri , "");
            OntModelWrapper ow = new OntModelWrapper(m, "", "");
            this.ontModelSet.put( uri, ow);
        }
    }
    /**
     * This method is used to added ontologies to the ontology server.
     * @param uri: The uri for the ontology that is to be added.
     */
    public void addOntologyModel( String uri ){

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF, null);
                 m.read( uri, "");
        OntModelWrapper ow = new OntModelWrapper(m, "", "");
        this.ontModelSet.put( uri, ow);

    }

    /**
     *
     * @param uri : The uri that represents the ontology.
     * @return: The Jena ontology model <OntModel> class reference
     */
    public OntModelWrapper getOntologyModel( String uri ){

        return this.ontModelSet.get( uri );

    }

    public HashMap<String, OntModelWrapper> getOntModelSet() {
        return ontModelSet;
    }
}
