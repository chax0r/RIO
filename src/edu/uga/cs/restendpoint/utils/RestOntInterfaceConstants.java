package edu.uga.cs.restendpoint.utils;

import org.joda.time.DateTime;
import org.w3c.dom.ranges.Range;

/**
 * Author: kale
 * Date: 10/17/11
 * Time: 1:44 AM
 * Email: <kale@cs.uga.edu>
 */
public interface RestOntInterfaceConstants {
    String SUPERCLASS = "SuperClass";
    String SUBCLASS = "SubClass";
    String INSTANCE = "Instance";
    String DISJOINT = "DISJOINT";
    String UNION = "UNION";
    String INTERSECTION = "INTERSECTION";
    String COMPLEMENT = "COMPLEMENT";
    String EQUIVALENT = "EQUIVALENT";
    String DOMAIN = "DOMAIN";
    String RANGE = "RANGE";
    String INSTANCEOF = "INSTANCEOF";
    String CLASSOF = "CLASSOF";

    String URI = "uri";
    String NAME = "name";
    String CLASSES = "Classes";
    String CLASS = "Class";
    String INSTANCES = "Instances";
    String SUPERCLASSES = "SuperClasses";
    String SUBCLASSES = "SubClasses";
    String UPDATE = "Update";

    String PROPERTY = "Property";
    String PROPERTY_TYPE = "type";
    String PROPERTY_VALUE ="value";
    String PROPERTIES = "Properties";
    String DECLARING_CLASSES = "DeclaringClasses";
    String DECLARING_CLASS = "DeclaringClass";
    String SUBPROPERTIES = "SubProperties";
    String SUBPROPERTY = "SubProperty";
    String SUPERPROPERTIES = "SuperProperties";
    String SUPERPROPERTY = "SuperProperty";

    String ONTOLOGIES = "ONTOLOGIES";
    String ONTOLOGY = "ONTOLOGY";

    String RESTRICTIONS = "Restrictions";
    String RESTRICTION = "Restriction";
    String RESTRICTION_TYPE ="type";
    String ALLVALUESFROM = "allValuesFrom";
    String HASVALUEFROM = "hasValueFrom";
    String SOMEVALUESFROM = "someValuesFrom";
    String VALUE = "Value";
    String MINCARDINALITY = "minCardanility";
    String MAXCARDINALITY = "maxCardanility";
    String COMMA_DELIMITER = ",";

    String RANGE_FLOAT = "float";
    String RANGE_INTEGER = "integer";
    String RANGE_REAL = "real";
    String RANGE_DECIMAL = "decimal";
    String RANGE_STRING = "string";
    String RANGE_TOKEN = "token";
    String RANGE_LANGUAGE = "language";
    String RANGE_BOOLEAN = "boolean";
    String RANGE_URI = "URI";
    String RANGE_XML="XML";
    String RANGE_TIME = "TIME";
    String RANGE_NONPOSITIVE = "nonPositive";
    String RANGE_NONNEGATIVE = "nonNegative";
    String DATATYPE = "DATATYPE";

    long cacheTimeOut = 240000l;

}
