package edu.uga.cs.restendpoint.exceptions.mappers;

import edu.uga.cs.restendpoint.exceptions.NotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Author: kale
 * Date: 11/3/11
 * Time: 11:45 PM
 * Email: <kale@cs.uga.edu>
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException>
{
   public Response toResponse(NotFoundException exception)
   {
      return Response.status(Response.Status.NOT_FOUND)
              .entity(exception.getMessage())
              .type("text/plain").build();

   }
}
