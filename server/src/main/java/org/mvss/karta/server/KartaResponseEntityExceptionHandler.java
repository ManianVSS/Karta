package org.mvss.karta.server;

import java.util.NoSuchElementException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestController
@ControllerAdvice
@RestControllerAdvice
public class KartaResponseEntityExceptionHandler extends ResponseEntityExceptionHandler
{
   @ExceptionHandler( {BadRequestException.class, DataIntegrityViolationException.class, EmptyResultDataAccessException.class, ConstraintViolationException.class, NoSuchElementException.class} )
   public ResponseEntity<String> handleBadRequests( Exception exception )
   {
      return new ResponseEntity<String>( exception.getMessage(), HttpStatus.BAD_REQUEST );
   }
}
