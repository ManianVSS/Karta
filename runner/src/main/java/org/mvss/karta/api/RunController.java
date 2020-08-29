package org.mvss.karta.api;

import java.util.HashMap;

import org.mvss.karta.framework.core.TestRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class RunController
{
   private static final String RUNURL = "/api/runs";

   @Autowired
   ObjectMapper                objectMapper;

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = RUNURL )
   public boolean startRun( @RequestBody HashMap<String, Object> runConfiguration )
   {
      TestRunner testRunner = objectMapper.convertValue( runConfiguration, TestRunner.class );
      new Thread( testRunner ).run();
      return true;
   }
}
