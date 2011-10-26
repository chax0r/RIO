package edu.uga.cs.restendpoint.service;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 10/16/11
 * Time: 11:53 AM
 * Email: <kale@cs.uga.edu>
 */
public class ConfigurationTest {
    @Test
    public void testOntologyLoad(){

        Configuration configuration = new PropertiesConfiguration( );
        String propertiesFile = "owl.properties";
        InputStream propertiesFileStream = null;
        try {
            propertiesFileStream = new FileInputStream("resources/" + propertiesFile);
            ( (PropertiesConfiguration)configuration ).load( propertiesFileStream );
            List<String> ontologies =  Arrays.asList(configuration.getStringArray("ontologies"));
            String [] ont = configuration.getStringArray("ontologies");
            for(String o : ont){
                System.out.println(o);
            }

            for( String ontology : ontologies ){
                String fileName = configuration.getString( ontology + ".filename" );
                String URI = configuration.getString( ontology + ".uri");
                System.out.println("Name: "  + ontology);
                System.out.println("FileName: " + fileName);
                System.out.println("URI: " + URI);

             /*   InputStream is = new FileInputStream( "/resource/"+ fileName );

                if( is == null ){
                    System.out.println("The owl file mentioned in the properties file does not exist.");
                }else{
                    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
                    model.read(is, "");
                    is.close();
                }*/
            }
        } catch (ConfigurationException e) {
            Logger.getLogger(ConfigurationTest.class.getName()).log(Level.SEVERE, null, e);
        }catch (FileNotFoundException e) {
            Logger.getLogger(ConfigurationTest.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            Logger.getLogger(ConfigurationTest.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
