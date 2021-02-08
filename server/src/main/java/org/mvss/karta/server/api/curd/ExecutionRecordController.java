package org.mvss.karta.server.api.curd;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.mvss.karta.framework.enums.TestStatus;
import org.mvss.karta.server.BadRequestException;
import org.mvss.karta.server.dao.models.ExecutionRecord;
import org.mvss.karta.server.dao.models.Run;
import org.mvss.karta.server.dao.services.ExecutionRecordService;
import org.mvss.karta.server.dao.services.RunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class ExecutionRecordController
{
   private static final String    RUN                                = "run";
   private static final String    NAME                               = "name";
   private static final String    STATUS                             = "status";
   private static final String    TEST_BED                           = "testBed";
   private static final String    RUN_ID_NOT_FOUND                   = "Run id not found: ";
   private static final String    INVALID_REFERENCE_TO_RUN           = "Invalid reference to run: ";
   private static final String    EXECUTION_RECORD_WITH_ID_NOT_FOUND = "Execution record with id not found: ";

   private static final String    accessPath                         = "/api/executionRecords";
   private static final String    accessPathCount                    = accessPath + "Count";
   private static final String    accessByIdPath                     = accessPath + "/{id}";
   private static final String    getVersionByIdPath                 = accessByIdPath + "/version";
   private static final String    accessPathAddAll                   = accessPath + "AddAll";
   private static final String    accessPathUpdateAll                = accessPath + "UpdateAll";
   private static final String    accessPathPatchAll                 = accessPath + "PatchAll";
   private static final String    accessPathDeleteAll                = accessPath + "DeleteAll";

   private static final String    accessByNamePath                   = accessPath + "ByName";
   private static final String    accessByRunPath                    = accessPath + "ByRun";
   private static final String    accessByRunAndStatusPath           = accessPath + "ByRunAndStatus";
   private static final String    accessByRunAndNamePath             = accessPath + "ByRunAndName";
   private static final String    accessByRunAndTestBedPath          = accessPath + "ByRunAndTestBed";
   private static final String    accessByNameAndTestBedPath         = accessPath + "ByNameAndTestBed";
   private static final String    accessByRunAndNameAndTestBedPath   = accessPath + "ByRunAndNameAndTestBed";

   @Autowired
   private ExecutionRecordService service;

   @Autowired
   private RunService             runService;

   @Autowired
   private BeanUtilsBean          nonNullCopier;

   @RequestMapping( value = accessPathCount, method = RequestMethod.GET )
   public long count()
   {
      return service.count();
   }

   @RequestMapping( value = accessPath, method = RequestMethod.GET )
   public List<ExecutionRecord> getAll()
   {
      return service.getAll();
   }

   @RequestMapping( value = accessByIdPath, method = RequestMethod.GET )
   public ExecutionRecord get( @PathVariable long id ) throws BadRequestException
   {
      try
      {
         return service.get( id );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( EXECUTION_RECORD_WITH_ID_NOT_FOUND + id, nsee );
      }
   }

   @RequestMapping( value = getVersionByIdPath, method = RequestMethod.GET )
   public long getVersion( @PathVariable long id ) throws BadRequestException
   {
      try
      {
         return service.getVersion( id );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( EXECUTION_RECORD_WITH_ID_NOT_FOUND + id, nsee );
      }
   }

   private boolean updateRefs( ExecutionRecord obj )
   {
      Run run = obj.getRun();
      if ( run != null )
      {
         Long runId = run.getId();
         if ( runId != null )
         {
            run = runService.get( runId );
            obj.setRun( run );
            return true;
         }
      }

      return false;
   }

   @RequestMapping( value = accessPath, method = RequestMethod.POST )
   public ResponseEntity<ExecutionRecord> add( @RequestBody ExecutionRecord obj )
   {
      if ( !updateRefs( obj ) )
      {
         return new ResponseEntity<ExecutionRecord>( HttpStatus.BAD_REQUEST );
      }

      return new ResponseEntity<ExecutionRecord>( service.save( obj ), HttpStatus.CREATED );
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessByIdPath, method = RequestMethod.PUT )
   public ExecutionRecord update( @PathVariable long id, @RequestBody ExecutionRecord executionRecord ) throws Exception
   {
      try
      {
         ExecutionRecord executionRecordToModify = get( id );
         executionRecord.setId( executionRecordToModify.getId() );
         executionRecord.setVersion( executionRecordToModify.getVersion() );
         BeanUtils.copyProperties( executionRecordToModify, executionRecord );

         if ( !updateRefs( executionRecordToModify ) )
         {
            throw new BadRequestException( INVALID_REFERENCE_TO_RUN + executionRecordToModify.getRun() );
         }

         return service.save( executionRecordToModify );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( EXECUTION_RECORD_WITH_ID_NOT_FOUND + id, nsee );
      }
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessByIdPath, method = RequestMethod.PATCH )
   public ExecutionRecord patch( @PathVariable long id, @RequestBody ExecutionRecord executionRecord ) throws Exception
   {
      try
      {
         ExecutionRecord executionRecordToModify = get( id );
         executionRecord.setId( null );
         executionRecord.setVersion( null );
         nonNullCopier.copyProperties( executionRecordToModify, executionRecord );

         if ( !updateRefs( executionRecordToModify ) )
         {
            throw new BadRequestException( INVALID_REFERENCE_TO_RUN + executionRecordToModify.getRun() );
         }

         return service.save( executionRecordToModify );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( EXECUTION_RECORD_WITH_ID_NOT_FOUND + id, nsee );
      }
   }

   @ResponseStatus( value = HttpStatus.CREATED )
   @RequestMapping( value = accessPathAddAll, method = RequestMethod.POST )
   public List<ExecutionRecord> saveAll( @RequestBody List<ExecutionRecord> executionRecords )
   {
      return service.saveAll( executionRecords );
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessPathUpdateAll, method = RequestMethod.POST )
   public List<ExecutionRecord> updateAll( @RequestBody List<ExecutionRecord> executionRecords ) throws Exception
   {
      List<ExecutionRecord> toReturn = new ArrayList<ExecutionRecord>();
      for ( ExecutionRecord executionRecord : executionRecords )
      {
         toReturn.add( update( executionRecord.getId(), executionRecord ) );
      }
      return toReturn;
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessPathPatchAll, method = RequestMethod.POST )
   public List<ExecutionRecord> patchAll( @RequestBody List<ExecutionRecord> executionRecords ) throws Exception
   {
      List<ExecutionRecord> toReturn = new ArrayList<ExecutionRecord>();
      for ( ExecutionRecord executionRecord : executionRecords )
      {
         toReturn.add( patch( executionRecord.getId(), executionRecord ) );
      }
      return toReturn;
   }

   @ResponseStatus( HttpStatus.NO_CONTENT )
   @RequestMapping( value = accessByIdPath, method = RequestMethod.DELETE )
   public void deleteById( @PathVariable long id )
   {
      service.deleteById( id );
   }

   @ResponseStatus( HttpStatus.NO_CONTENT )
   @RequestMapping( value = accessPathDeleteAll, method = RequestMethod.DELETE )
   public void deleteAll()
   {
      service.deleteAll();
   }

   @RequestMapping( value = accessByNamePath, method = RequestMethod.GET )
   public List<ExecutionRecord> getByName( @RequestParam( name = NAME ) String name )
   {
      return service.getByName( name );
   }

   @RequestMapping( value = accessByRunPath, method = RequestMethod.GET )
   public List<ExecutionRecord> getByRun( @RequestParam( name = RUN ) Long runId ) throws BadRequestException
   {
      try
      {
         Run run = runService.get( runId );
         return service.getByRun( run );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( RUN_ID_NOT_FOUND + runId, nsee );
      }
   }

   @RequestMapping( value = accessByRunAndStatusPath, method = RequestMethod.GET )
   public List<ExecutionRecord> getByRunAndStatus( @RequestParam( name = RUN ) Long runId, @RequestParam( name = STATUS ) TestStatus status ) throws BadRequestException
   {
      try
      {
         Run run = runService.get( runId );
         return service.getByRunAndStatus( run, status );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( RUN_ID_NOT_FOUND + runId, nsee );
      }
   }

   @RequestMapping( value = accessByRunAndNamePath, method = RequestMethod.GET )
   public List<ExecutionRecord> getByRunAndName( @RequestParam( name = RUN ) Long runId, @RequestParam( name = NAME ) String name ) throws BadRequestException
   {
      try
      {
         Run run = runService.get( runId );
         return service.getByRunAndName( run, name );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( RUN_ID_NOT_FOUND + runId, nsee );
      }
   }

   @RequestMapping( value = accessByRunAndTestBedPath, method = RequestMethod.GET )
   public List<ExecutionRecord> getByRunAndTestBed( @RequestParam( name = RUN ) Long runId, @RequestParam( name = TEST_BED ) String testBed ) throws BadRequestException
   {
      try
      {
         Run run = runService.get( runId );
         return service.getByRunAndTestBed( run, testBed );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( RUN_ID_NOT_FOUND + runId, nsee );
      }
   }

   @RequestMapping( value = accessByNameAndTestBedPath, method = RequestMethod.GET )
   public List<ExecutionRecord> getByNameAndTestBed( @RequestParam( name = NAME ) String name, @RequestParam( name = TEST_BED ) String testBed )
   {
      return service.getByNameAndTestBed( name, testBed );
   }

   @RequestMapping( value = accessByRunAndNameAndTestBedPath, method = RequestMethod.GET )
   public ExecutionRecord getByRunAndNameAndTestBed( @RequestParam( name = RUN ) Long runId, @RequestParam( name = NAME ) String name, @RequestParam( name = TEST_BED ) String testBed ) throws BadRequestException
   {
      try
      {
         Run run = runService.get( runId );
         return service.getByRunAndNameAndTestBed( run, name, testBed );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( RUN_ID_NOT_FOUND + runId, nsee );
      }
   }
}
