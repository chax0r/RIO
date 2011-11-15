package edu.uga.cs.restendpoint.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/3/11
 * Time: 11:47 PM
 * Email: <kale@cs.uga.edu>
 */
public class NotFoundException extends RuntimeException
{
   public NotFoundException(String s)
   {
      super(s);
   }
}