package edu.uga.cs.restendpoint.model;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;
import org.joda.time.DateTime;

/**
 * Author: kale
 * Date: 10/18/11
 * Time: 4:49 AM
 * Email: <kale@cs.uga.edu>
 */
public class SparqlQueryResultStore {
    private String id;
    private ResultSet resultSet;
    private Prologue prologue;
    private long createTime;

    public SparqlQueryResultStore(String id, ResultSet resultSet, Prologue prologue, long createTime ) {
        this.id = id;
        this.resultSet = resultSet;
        this.prologue = prologue;
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public Prologue getPrologue() {
        return prologue;
    }

    public long getCreateTime() {
        return createTime;
    }
}
