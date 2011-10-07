package edu.uga.cs.restendpoint.listener;
/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 10/6/11
 * Time: 6:18 PM
 * Email: <kale@cs.uga.edu>
 */

import edu.uga.cs.restendpoint.utils.OntologyModelStore;
import edu.uga.cs.restendpoint.utils.PropertyFileParser;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionBindingEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomServletContextListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    // Public constructor is required by servlet spec
    public CustomServletContextListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
        /* This method is called when the servlet context is
           initialized(when the Web application is deployed). 
           You can initialize servlet context related data here.
        */


        ServletContext sc = sce.getServletContext();
        String propertiesFile = sc.getInitParameter( "resourceFile" );
        try {
            Configuration configuration = new PropertiesConfiguration( propertiesFile );
            List<String> ontFileNames =  Arrays.asList( configuration.getStringArray( "owlFiles" ) );
            List<String> filePaths = new ArrayList<String>(ontFileNames.size());
            for(String fileName : ontFileNames){
                if( configuration.getString( fileName ) != null)
                    filePaths.add( configuration.getString(fileName) );
            }
            OntologyModelStore ontologyModelStore = new OntologyModelStore();
            ontologyModelStore.populateOntologyStoreFromFile( filePaths );
            sc.setAttribute( "ontologyModelStore", ontologyModelStore);
        } catch (ConfigurationException e) {
            Logger.getLogger(CustomServletContextListener.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        /* This method is invoked when the Servlet Context 
           (the Web application) is undeployed or 
           Application Server shuts down.
        */
    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    public void sessionCreated(HttpSessionEvent se) {
        /* Session is created. */
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        /* Session is destroyed. */
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded(HttpSessionBindingEvent sbe) {
        /* This method is called when an attribute 
           is added to a session.
        */
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
        /* This method is called when an attribute
           is removed from a session.
        */
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
        /* This method is invoked when an attibute
           is replaced in a session.
        */
    }
}
