package edu.uga.cs.restendpoint.model;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 10/18/11
 * Time: 4:49 AM
 * Email: <kale@cs.uga.edu>
 */
public class SparqlQueryResultStore {
    private Integer id;
    private ResultSet resultSet;
    private PrefixMapping prefixMap;
    private long timeout;
}
