package edu.uga.cs.restendpoint.exceptions.mappers;

import edu.uga.cs.restendpoint.exceptions.NotFoundException;
import org.jboss.resteasy.spi.BadRequestException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Created by IntelliJ IDEA.
 * User: kale
 * Date: 11/11/11
 * Time: 10:07 PM
 * Email: <kale@cs.uga.edu>
 */
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException>{
   public Response toResponse(BadRequestException exception)
   {
      return Response.status(Response.Status.BAD_REQUEST )
              .entity(exception.getMessage())
              .type("text/plain").build();

   }
}
