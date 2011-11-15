package edu.uga.cs.restendpoint.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/11/11
 * Time: 10:08 PM
 * Email: <kale@cs.uga.edu>
 */
public class BadRequestException extends RuntimeException
{
   public BadRequestException(String s)
   {
      super(s);
   }
}
