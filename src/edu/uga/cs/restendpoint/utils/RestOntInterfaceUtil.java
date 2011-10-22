package edu.uga.cs.restendpoint.utils;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import edu.uga.cs.restendpoint.model.OntologyModelStore;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 10/16/11
 * Time: 11:03 PM
 * Email: <kale@cs.uga.edu>
 */
public class RestOntInterfaceUtil {

    public static OntClass getClass(String ontologyName, String className, OntologyModelStore ontologyModelStore) {
        OntModelWrapper ontModelWrapper = ontologyModelStore.getOntologyModel( ontologyName );

        //TODO: Throw appropriate exception and return HTTP code.
        if( ontModelWrapper == null){
               System.out.println( ontologyName +  " is not loaded in the server" );
               return null;
        }
        OntModel model = ontModelWrapper.getOntModel();
        System.out.println("Looking for " + ontModelWrapper.getURI() +"#"+className);
        OntClass ontClass = model.getOntClass( ontModelWrapper.getURI() + "#" + className);

        if(ontClass == null){
            System.out.println( className +  " does not exist in " + ontologyName + " ontology");
            return null;

        }
        return ontClass;
    }
}
