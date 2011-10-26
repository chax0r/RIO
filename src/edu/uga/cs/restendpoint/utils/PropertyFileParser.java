/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uga.cs.restendpoint.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Author: kale
 * Date: 10/6/11
 * Time: 5:54 PM
 * Email: <kale@cs.uga.edu>
 */
/**
 * Author: kale
 * Date: 10/16/11
 * Time: 10:59 PM
 * Email: <kale@cs.uga.edu>
 */
public class PropertyFileParser {

    private String propertiesFileName;
    private Properties props;

    public PropertyFileParser(String porpertiesFileName) {
        this.propertiesFileName = porpertiesFileName;
        this.props = new Properties();
    }

    public Boolean loadPropertiesFile() {

        if (propertiesFileName == null) {
            return false;
        } else {

            try {
                props.load(new FileInputStream(propertiesFileName));
                return true;
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }

    }

    public String getSystemProperty(String property) {

        return props.getProperty(property) != null ? props.getProperty(property) : "";

    }

    public Properties getProps() {
        return props;
    }
}
