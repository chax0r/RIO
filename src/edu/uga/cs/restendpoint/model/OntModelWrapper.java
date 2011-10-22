package edu.uga.cs.restendpoint.model;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 10/16/11
 * Time: 12:18 AM
 * Email: <kale@cs.uga.edu>
 */
public class OntModelWrapper {
    private final OntModel ontModel;
    private final String URI;
    private final String ontologyName;

    public OntModelWrapper(OntModel ontModel, String URI, String ontologyName) {
        this.ontModel = ontModel;
        this.URI = URI;
        this.ontologyName = ontologyName;
    }

    public OntModel getOntModel() {
        return ontModel;
    }

    public String getURI() {
        return URI;
    }

    public String getOntologyName() {
        return ontologyName;
    }
}
