package edu.uga.cs.restendpoint.service.impl;

import edu.uga.cs.restendpoint.model.SparqlQueryResultStore;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/25/11
 * Time: 1:09 PM
 * Email: <kale@cs.uga.edu>
 */
public class MonitorResultStore implements Runnable{

    private final Map<String, SparqlQueryResultStore> sparqlQueryResultStoreMap;

    public MonitorResultStore(Map<String, SparqlQueryResultStore> sparqlQueryResultStoreMap) {
        this.sparqlQueryResultStoreMap = sparqlQueryResultStoreMap;
    }

    public void run() {

        while( true ){

            synchronized ( sparqlQueryResultStoreMap ){

                while( sparqlQueryResultStoreMap.isEmpty() ){
                    try {
                        sparqlQueryResultStoreMap.wait();
                    } catch (InterruptedException e) {
                        Logger.getLogger(MonitorResultStore.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                for( Map.Entry<String, SparqlQueryResultStore> entry : sparqlQueryResultStoreMap.entrySet() ){



                }
        }
        //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
