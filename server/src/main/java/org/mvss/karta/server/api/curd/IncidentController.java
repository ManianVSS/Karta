package org.mvss.karta.server.api.curd;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.mvss.karta.framework.enums.IncidentStatus;
import org.mvss.karta.server.BadRequestException;
import org.mvss.karta.server.dao.models.ExecutionRecord;
import org.mvss.karta.server.dao.models.Incident;
import org.mvss.karta.server.dao.services.ExecutionRecordService;
import org.mvss.karta.server.dao.services.IncidentService;
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
public class IncidentController
{
   private static final String    STATUS                                = "status";
   private static final String    EXECUTION_RECORD                      = "executionRecord";
   private static final String    EXECUTION_RECORD_ID_NOT_FOUND         = "Execution record id not found: ";
   private static final String    INCIDENT_WITH_ID_NOT_FOUND            = "Incident with id not found: ";
   private static final String    INVALID_REFERENCE_TO_EXECUTION_RECORD = "Invalid reference to execution record: ";

   private static final String    accessPath                            = "/api/incidents";
   private static final String    accessPathCount                       = accessPath + "Count";
   private static final String    accessByIdPath                        = accessPath + "/{id}";
   private static final String    getVersionByIdPath                    = accessByIdPath + "/version";
   private static final String    accessPathAddAll                      = accessPath + "AddAll";
   private static final String    accessPathUpdateAll                   = accessPath + "UpdateAll";
   private static final String    accessPathPatchAll                    = accessPath + "PatchAll";
   private static final String    accessPathDeleteAll                   = accessPath + "DeleteAll";

   private static final String    accessByExecutionRecordPath           = accessPath + "ByExecutionRecord";
   private static final String    accessByStatusPath                    = accessPath + "ByStatus";

   @Autowired
   private IncidentService        service;

   @Autowired
   private ExecutionRecordService executionRecordService;

   @Autowired
   private BeanUtilsBean          nonNullCopier;

   @RequestMapping( value = accessPathCount, method = RequestMethod.GET )
   public long count()
   {
      return service.count();
   }

   @RequestMapping( value = accessPath, method = RequestMethod.GET )
   public List<Incident> getAll()
   {
      return service.getAll();
   }

   @RequestMapping( value = accessByIdPath, method = RequestMethod.GET )
   public Incident get( @PathVariable long id ) throws BadRequestException
   {
      try
      {
         return service.get( id );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( INCIDENT_WITH_ID_NOT_FOUND + id, nsee );
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
         throw new BadRequestException( INCIDENT_WITH_ID_NOT_FOUND + id, nsee );
      }
   }

   private boolean updateRefs( Incident obj )
   {
      ExecutionRecord executionRecord = obj.getExecutionRecord();
      if ( executionRecord != null )
      {
         Long executionRecordId = executionRecord.getId();
         if ( executionRecordId != null )
         {
            executionRecord = executionRecordService.get( executionRecordId );
            obj.setExecutionRecord( executionRecord );
            return true;
         }
      }

      return false;
   }

   @RequestMapping( value = accessPath, method = RequestMethod.POST )
   public ResponseEntity<Incident> add( @RequestBody Incident obj )
   {
      if ( !updateRefs( obj ) )
      {
         return new ResponseEntity<Incident>( HttpStatus.BAD_REQUEST );
      }

      return new ResponseEntity<Incident>( service.save( obj ), HttpStatus.CREATED );
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessByIdPath, method = RequestMethod.PUT )
   public Incident update( @PathVariable long id, @RequestBody Incident incident ) throws Exception
   {
      try
      {
         Incident incidentToModify = get( id );
         incident.setId( incidentToModify.getId() );
         incident.setVersion( incidentToModify.getVersion() );
         BeanUtils.copyProperties( incidentToModify, incident );

         if ( !updateRefs( incidentToModify ) )
         {
            throw new BadRequestException( INVALID_REFERENCE_TO_EXECUTION_RECORD + incidentToModify.getExecutionRecord() );
         }

         return service.save( incidentToModify );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( INCIDENT_WITH_ID_NOT_FOUND + id, nsee );
      }
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessByIdPath, method = RequestMethod.PATCH )
   public Incident patch( @PathVariable long id, @RequestBody Incident incident ) throws Exception
   {
      try
      {
         Incident incidentToModify = get( id );
         incident.setId( null );
         incident.setVersion( null );
         nonNullCopier.copyProperties( incidentToModify, incident );

         if ( !updateRefs( incidentToModify ) )
         {
            throw new BadRequestException( INVALID_REFERENCE_TO_EXECUTION_RECORD + incidentToModify.getExecutionRecord() );
         }

         return service.save( incidentToModify );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( INCIDENT_WITH_ID_NOT_FOUND + id, nsee );
      }
   }

   @ResponseStatus( value = HttpStatus.CREATED )
   @RequestMapping( value = accessPathAddAll, method = RequestMethod.POST )
   public List<Incident> saveAll( @RequestBody List<Incident> incidents )
   {
      return service.saveAll( incidents );
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessPathUpdateAll, method = RequestMethod.POST )
   public List<Incident> updateAll( @RequestBody List<Incident> incidents ) throws Exception
   {
      List<Incident> toReturn = new ArrayList<Incident>();
      for ( Incident incident : incidents )
      {
         toReturn.add( update( incident.getId(), incident ) );
      }
      return toReturn;
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessPathPatchAll, method = RequestMethod.POST )
   public List<Incident> patchAll( @RequestBody List<Incident> incidents ) throws Exception
   {
      List<Incident> toReturn = new ArrayList<Incident>();
      for ( Incident incident : incidents )
      {
         toReturn.add( patch( incident.getId(), incident ) );
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

   @RequestMapping( value = accessByExecutionRecordPath, method = RequestMethod.GET )
   public List<Incident> getByExecutionRecord( @RequestParam( name = EXECUTION_RECORD ) Long executionRecordId ) throws BadRequestException
   {
      try
      {
         ExecutionRecord executionRecord = executionRecordService.get( executionRecordId );
         return service.getByExecutionRecord( executionRecord );
      }
      catch ( NoSuchElementException nsee )
      {
         throw new BadRequestException( EXECUTION_RECORD_ID_NOT_FOUND + executionRecordId, nsee );
      }
   }

   @RequestMapping( value = accessByStatusPath, method = RequestMethod.GET )
   public List<Incident> getByStatus( @RequestParam( name = STATUS ) IncidentStatus status )
   {
      return service.getByStatus( status );
   }

}
