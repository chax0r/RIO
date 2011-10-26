package edu.uga.cs.restendpoint.service;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.sun.tools.jdi.EventSetImpl;
import edu.uga.cs.restendpoint.model.OntModelWrapper;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 10/13/11
 * Time: 12:07 PM
 * Email: <kale@cs.uga.edu>
 */
public class NavigationalServiceTest {

    @Test
    public void testNavigationService(){

        InputStream is = null;
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF, null);
        try{
            is = new FileInputStream("resources/pizza.owl");
            model.read(is, "");
        }catch( FileNotFoundException fe ){
            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, fe);
        } catch (IOException e) {
            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, e);
        }

        String NS = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
        OntModelWrapper ontModelWrapper = new OntModelWrapper(model, NS, "pizza" );
        System.out.println("Output of the test case: ");
        String testPath = "hasTopping/hasSpiciness";
        System.out.println( new NavigationalService().executePathQuery(testPath, ontModelWrapper));
    }


    @Test
    public void testRestrictions(){
         InputStream is = null;
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF, null);

        try{
            is = new FileInputStream("resources/pizza.owl");
            model.read(is, "");
            String NS = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
            is.close();

            OntClass pizza = model.getOntClass(NS + "#American");
            OntProperty p = model.getOntProperty(NS + "#hasTopping");
            ExtendedIterator superClassItr = pizza.listSuperClasses( false );
            while( superClassItr.hasNext() ){
                OntClass c = (OntClass) superClassItr.next();

                if( c.isRestriction()){
                    Restriction r = c.as(Restriction.class);
                    System.out.println("Restriction on : "+ r.getOnProperty().getLocalName() );

                    if(r.isAllValuesFromRestriction()){
                        AllValuesFromRestriction allValuesFromRestriction = r.asAllValuesFromRestriction();
                        Resource res = allValuesFromRestriction.getAllValuesFrom();
                        System.out.println( "All value restriction: " +res.getLocalName() );
                    }

                    if( r.isSomeValuesFromRestriction() ){
                        SomeValuesFromRestriction someValuesFromRestriction = r.asSomeValuesFromRestriction();
                        Resource res = someValuesFromRestriction.getSomeValuesFrom();
                        System.out.println( "Some value restriction: " + res.getURI() );
                    }
                    if( r.isHasValueRestriction() ){

                            HasValueRestriction hasValueRestriction = r.asHasValueRestriction();
                            if( hasValueRestriction.getHasValue() != null &&
                                    hasValueRestriction.getHasValue().isResource()){

                                OntResource res =  hasValueRestriction.getHasValue().asResource().as(OntResource.class);
                                if( res.isClass() ){
                                OntClass o = hasValueRestriction.getHasValue().as( OntClass.class );
                                System.out.println("Has value restriction: " + o.getURI() );
                                }else {
                                    System.out.println("Has value restriction is not a class.");
                                }
                            }
                    }
                }
            }

        }catch( FileNotFoundException fe ){
            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, fe);
        } catch (IOException e) {
            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Test
    public void browseClasses(){
        InputStream is = null;
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);

        try{
            is = new FileInputStream("resources/pizza.owl");
            model.read(is, "");
            String NS = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
//            OntClass cheeseyClass = model.getOntClass(NS + "#CheeseyPizza");
//            System.out.println(cheeseyClass.toString());
            //System.out.println( cheeseyClass.getO(NS + "#hasBase").toString() );

            OntClass pizza = model.getOntClass(NS + "#American");
            OntProperty p = model.getOntProperty(NS + "#hasTopping");


            System.out.println("Super Classes of American Pizza: ");
            ExtendedIterator superClassItr = pizza.listSuperClasses(true);
            while( superClassItr.hasNext() ){
                OntClass c = (OntClass) superClassItr.next();

                if( c.isRestriction()){
                    Restriction r = c.as(Restriction.class);
                    System.out.println("Restriction on : "+ r.getOnProperty().getLocalName() );

                    if(r.isAllValuesFromRestriction()){
                        AllValuesFromRestriction allValuesFromRestriction = r.asAllValuesFromRestriction();
                        Resource res = allValuesFromRestriction.getAllValuesFrom();
                        System.out.println( res.getLocalName() );
                    }

                    if( r.isSomeValuesFromRestriction() ){
                        SomeValuesFromRestriction someValuesFromRestriction = r.asSomeValuesFromRestriction();
                        Resource res = someValuesFromRestriction.getSomeValuesFrom();
                        System.out.println( "Some Values: " + res.getURI() );
                    }
                }
/*

                if( c.isDatatypeProperty() ){
                    DatatypeProperty d = c.as(DatatypeProperty.class);
                    System.out.println("Parent of Pizza is dataproperty: " + d.getLocalName());
                }

                if( c.isObjectProperty() ){
                    ObjectProperty o = c.as( ObjectProperty.class );
                    System.out.println("Parent of Pizza is objectproperty: " + o.getLocalName());
                }
  */

            }
            System.out.println("All properties of Pizza: ");
            ExtendedIterator propItr = pizza.listDeclaredProperties();
            while( propItr.hasNext() ){
                OntProperty ontProperty = (OntProperty)propItr.next();

                if( ontProperty.isDatatypeProperty() ){

                    DatatypeProperty d = ontProperty.as(DatatypeProperty.class);
                    System.out.println(" dataproperty: " + d.getLocalName());
                }
                if(ontProperty.isObjectProperty()){

                    ObjectProperty o = ontProperty.as( ObjectProperty.class );
                    /*if(ontProperty.isResource()){
                        System.out.println("Property is resource: ");
                        OntResource objResource = ontProperty.as(OntResource.class);
                        if(objResource.isClass()){
                            System.out.println("REsource is class");
                            OntClass resClass = objResource.as(OntClass.class);
                        }
                    } */
                    System.out.println("Parent of Pizza is objectproperty: " + o.getLocalName());
                }
            }

               /*
            System.out.println(pizza.asResource().hasProperty(p));
            System.out.println("American pizza has toppings: " + pizza.hasDeclaredProperty(p, false ) );

            ExtendedIterator propItr = pizza.listDeclaredProperties();

            ObjectProperty objectProperty = model.getObjectProperty(NS + "#hasTopping");

            Resource res1 = objectProperty.getRange();

            if(res1.canAs(DataRange.class)){
                DataRange dr = res1.as(DataRange.class);
                for(Iterator i=dr.listOneOf(); i.hasNext();){
                    System.out.println(i.next());
                }
            }

            System.out.println("*********** Range");
            while(propItr.hasNext()){
                OntProperty p1 = (OntProperty) propItr.next();
                ExtendedIterator  rangeItr = p1.listRange();
                while( rangeItr.hasNext() ){
                    System.out.println(rangeItr.next().toString());
                }
                   /*
                if(top!=null && top.canAs(DataRange.class)){
                    DataRange rr = top.as(DataRange.class);
                    ExtendedIterator i = rr.listOneOf();
                    while( i.hasNext()){
                            System.out.println( i.next().toString() );
                    }
                }

            }
            System.out.println("*********** Domain");
            while(propItr.hasNext()){
                OntProperty p1 = (OntProperty) propItr.next();
                OntResource top = p1.getDomain();
                if(top!=null)
                    System.out.println( top.getLocalName() );

            }
            ExtendedIterator restitr = p.listReferringRestrictions();

            while( restitr.hasNext() ){
                Restriction r = (Restriction) restitr.next();
                OntProperty pp = r.getOnProperty();
                System.out.println( pp.toString() );

            }*/


/*
            System.out.println("Ont Properties");
            ExtendedIterator propItr = pizza.listDeclaredProperties();
            while ( propItr.hasNext() ){
                System.out.println(propItr.next().toString());
            }*/
           /* System.out.println(" Properties");
            StmtIterator sr = pizza.listProperties();
            while ( sr.hasNext() ){
                System.out.println(sr.next().toString());
            } */


         /*   StmtIterator itr = venPizza.listProperties();
            while ( itr.hasNext() ){
                System.out.println("**********************");
                Statement st = itr.nextStatement();
                System.out.println(" Subject: " + st.getSubject().toString());
                System.out.println(" Predicate: " + st.getPredicate().toString());
                System.out.println(" Object: " + st.getObject().toString());

            }*/
                /* Code to List out all restrictions
            Property topping = model.getProperty(NS+"#hasTopping");
            System.out.println("URI to topping: "+topping.getLocalName());
            ResIterator resItr = venPizza.getModel().listSubjectsWithProperty( topping );
            ExtendedIterator restrictions = model.listRestrictions();
              while(restrictions.hasNext()){
                Restriction r = (Restriction) restrictions.next();

                  System.out.println(((Restriction) restrictions.next()).getOnProperty().toString());
              } */
            //HasValueRestriction hvr = model.getHasValueRestriction(NS+"#hasTopping");
            //RDFNode r = hvr.getHasValue();
            //System.out.println(r.toString());

           // while(resItr.hasNext()){
              //  System.out.println( resItr.nextResource().getURI() );
            //}

           // ExtendedIterator ctr = venPizza.listSuperClasses(true);

           /*  DatatypeProperty dp = model.getDatatypeProperty(NS+"#hasTopping");

            System.out.println(dp.toString());
            Resource ran = dp.getRange();
            if(ran.canAs(DataRange.class)){
                DataRange dRange = (DataRange)ran.as(DataRange.class);
                for(Iterator i=dRange.listOneOf();i.hasNext(); ){
                    System.out.println(i.next());
                }
            }*/

//            OntProperty vp = (OntProperty) model.getProperty(NS+"#hasTopping");

 //           Iterator it = vp.listReferringRestrictions();
//            while( it.hasNext()){
  //              Restriction r = (Restriction) it.next();
    //            System.out.println(r.getOnProperty().toString());
      //      }


            //while ( ctr.hasNext() ){
              // OntClass f = (OntClass)ctr.next();
             //   System.out.println(f.asResource().toString());
              //  System.out.println(ctr.next().toString());:
                /*if( res.isDatatypeProperty() ){
                    System.out.println(res.toString() + ": is a data property");
                   // DatatypeProperty dataProp = (DatatypeProperty) res;



                }else if( res.isObjectProperty() ){
                    System.out.println(res.toString() + ": is an object  property");
                    /*ExtendedIterator restrictions = res.listReferringRestrictions();
                    while( restrictions.hasNext() ){
                        System.out.println("**********");
                        Restriction r = (Restriction) restrictions.next();

                        System.out.println( r.getOnProperty().toString() );
                    }
                    ExtendedIterator itr = res.listSubProperties();
                    while( itr.hasNext() ){
                        System.out.println( ( ( OntProperty)  itr.next()  ).toString()  );
                    }

                    ExtendedIterator classItr = res.listDeclaringClasses(true);
                    while( classItr.hasNext() ){
                        System.out.println( ( ( OntClass)  classItr.next()  ).toString()  );
                    }

                  ExtendedIterator itr = res.listRange();
                    while ( itr.hasNext() ){
                        System.out.println("**********");
                        System.out.println(itr.next().toString());
                    }


                }*/


       //     }
            is.close();
        }catch( FileNotFoundException fe ){
            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, fe);
        } catch (IOException e) {
            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    @Test
    public void getAll(){

        InputStream is = null;
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        try{
            is = new FileInputStream("resources/pizza.owl");
            model.read(is, "");
            is.close();

//****************** List all the classes in the ontology *************************
            ExtendedIterator classItr =  model.listNamedClasses();
            System.out.println("************ Following are all the classes: *******************");
            while( classItr.hasNext() ){
                OntClass o = (OntClass) classItr.next();
                System.out.println( o.getURI() );
            }

//****************** List all the properties in the ontology *************************
            System.out.println("************** Following are all the properties: *****************");
            ExtendedIterator propItr = model.listAllOntProperties();
            while( propItr.hasNext()){
                System.out.println(propItr.next().toString());
            }

            System.out.println("************** Following are all the Instances: *****************");
            ExtendedIterator indItr = model.listIndividuals();
            while( indItr.hasNext() ){
                System.out.println( indItr.next().toString() );
            }

            System.out.println("************** Following are all the Restrictions: *****************");
            ExtendedIterator restrictionItr = model.listRestrictions();
            while( restrictionItr.hasNext() ){
                System.out.println( restrictionItr.next().toString() );
            }



        }catch( FileNotFoundException fe){
            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, fe);
        }   catch (IOException e){

            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, e);
        }
    }


    @Test
    public void testInferredClasses(){

        InputStream is = null;
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        String NS = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
        try{
            is = new FileInputStream("resources/pizza.owl");
            model.read(is, "");
            is.close();



            OntClass namedPizza = model.getOntClass(NS + "#NamedPizza");
            OntProperty hasTopping = model.getObjectProperty(NS + "#hasTopping");
/*
            ExtendedIterator restrictions = model.listRestrictions();
            while( restrictions.hasNext() ){
                Restriction r = (Restriction) restrictions.next();

                if(r.onProperty(hasTopping)){
                    System.out.println("This restriction is on hasTopping");

                    if(r.isSomeValuesFromRestriction()){
                        SomeValuesFromRestriction sv = r.asSomeValuesFromRestriction();
                        System.out.println( sv.getSomeValuesFrom().toString() );
                    }
                }
            }*/

            ExtendedIterator propItr = namedPizza.listDeclaredProperties();
            System.out.println("Named pizza has toppings: " + namedPizza.hasDeclaredProperty(hasTopping, false));
            Statement s = namedPizza.getProperty(hasTopping);
            System.out.println(s.getSubject().getURI());
            System.out.println(s.getObject().toString());
            /*while( stmItr.hasNext() ){
            } */
        }catch( FileNotFoundException fe){
            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, fe);
        }   catch (IOException e){

            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, e);
        }
    }


    @Test
    public void testPeople(){

        String [] a = {"1", "2", "3"};
        String [] b = new String[a.length-1];
        System.arraycopy( a, 1, b, 0, a.length -1);

        a[a.length-1] = "xyz";
        System.out.println("Array a: ");
        for (String s : a) {
            System.out.println(s);
        }

        System.out.println("Array b: ");
        for (String s : b) {
            System.out.println(s);
        }
       // InputStream is = null;
       // OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
       // String NS = "http://owl.man.ac.uk/2006/07/sssw/people";
      //  try{
            //is = new FileInputStream("resources/people.owl");
         //   model.read(NS);
           // is.close();
           // OntClass cat = model.getOntClass(NS + "#cat");
           // ExtendedIterator parentItr = cat.listSuperClasses();
            //while( parentItr.hasNext() ){
             //   System.out.println(parentItr.next().toString());
           // }
       /* }catch( FileNotFoundException fe){
            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, fe);
        }   catch (IOException e){

            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, e);
        }*/
    }


    @Test
    public void testMineClass(){
        InputStream is = null;
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
        try{
            is = new FileInputStream("resources/pizza.owl");
            model.read(is, "");
            String NS = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
            OntClass americanPizza = model.getOntClass(NS + "#American");

            ExtendedIterator propItr = americanPizza.listDeclaredProperties();
            while( propItr.hasNext() ){
                OntProperty p = (OntProperty) propItr.next();
                System.out.println( p.getLocalName() );

            }

            OntProperty p = model.getOntProperty(NS + "#hasTopping");

            System.out.println("******* SuperClasses of American");

            ExtendedIterator baap =  americanPizza.listSuperClasses(false);
            while ( baap.hasNext() ){
                OntClass c = (OntClass) baap.next();
                if( c.getLocalName() != null)
                System.out.println( c.getLocalName());
            }


            System.out.println("***** Restriction ************");
           baap =  americanPizza.listSuperClasses(false);
            while( baap.hasNext() ){
                OntClass cls = (OntClass) baap.next();
                if(  cls.isRestriction() ){
                    Restriction r = cls.as(Restriction.class);
                    System.out.println( r.getOnProperty().getURI() );
                    //System.out.println( ( (Restriction) ( (OntClass)baap.next() ).as(Restriction.class) ).getOnProperty().getURI() );
                    //System.out.println( ( (Restriction) ( (OntClass)baap.next() ).as(Restriction.class) ).getOnProperty().getURI() );
                }
            }
        }catch( FileNotFoundException fe){
            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, fe);
        }   catch (IOException e){

            Logger.getLogger( NavigationalServiceTest.class.getName()).log(Level.SEVERE, null, e);
        }

    }
}
