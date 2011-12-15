package edu.uga.cs.restendpoint.model;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import edu.uga.cs.restendpoint.exceptions.BadRequestException;
import edu.uga.cs.restendpoint.service.impl.OntologyManagementServiceImpl;
import edu.uga.cs.restendpoint.utils.RestOntInterfaceUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        //May use OWL_DL_MEM_TRANS_INF
            OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        //OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF, null);
           // m.read(is , "");
            /*if( URI == null){
                String modelURI = m.getNsPrefixURI( ontName );

                if( modelURI == null ){

                    String exp =  "No namespace available for the " + ontName + " ontology in the given OWL file";
                    RestOntInterfaceUtil.log(OntologyManagementServiceImpl.class.getName(), new BadRequestException(exp));
                    throw new BadRequestException( exp );

                }else{
                    URI = modelURI;
                }
            } */
            //m.
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
     * @param name: The uri for the ontology that is to be added.
     */
    public void addOntologyModel( String name, OntModelWrapper modelWrapper ){

        this.ontModelSet.put( name, modelWrapper);

    }

    /**
     *
     * @param ontologyName : The ontologyName  that represents the ontology.
     * @return: The Jena ontology model <OntModel> class reference
     */
    public OntModelWrapper getOntologyModel( String ontologyName ){

        return this.ontModelSet.get( ontologyName );

    }

    public HashMap<String, OntModelWrapper> getOntModelSet() {
        return ontModelSet;
    }

    public void remove(String ontologyName) {

        this.ontModelSet.put( ontologyName, null );
        this.ontModelSet.remove( ontologyName );
        System.gc();
    }
}
