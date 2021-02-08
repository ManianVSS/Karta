package org.mvss.karta.server.dao.services;

import java.util.List;

import org.mvss.karta.framework.enums.TestStatus;
import org.mvss.karta.server.dao.models.ExecutionRecord;
import org.mvss.karta.server.dao.models.Run;
import org.mvss.karta.server.dao.repositories.ExecutionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExecutionRecordService
{
   @Autowired
   private ExecutionRecordRepository repository;

   public long count()
   {
      return repository.count();
   }

   public List<ExecutionRecord> getAll()
   {
      return repository.findAll();
   }

   public ExecutionRecord get( long id )
   {
      return repository.findById( id ).get();
   }

   public long getVersion( long id )
   {
      return get( id ).getVersion();
   }

   public ExecutionRecord save( ExecutionRecord executionRecord )
   {
      return repository.save( executionRecord );
   }

   public List<ExecutionRecord> saveAll( List<ExecutionRecord> executionRecords )
   {
      return repository.saveAll( executionRecords );
   }

   public void delete( ExecutionRecord executionRecord )
   {
      repository.delete( executionRecord );
   }

   public void deleteById( long id )
   {
      repository.deleteById( id );
   }

   public void deleteAll()
   {
      repository.deleteAll();
   }

   public List<ExecutionRecord> getByName( String name )
   {
      return repository.getByName( name );
   }

   public List<ExecutionRecord> getByRun( Run run )
   {
      return repository.getByRun( run );
   }

   public List<ExecutionRecord> getByRunAndStatus( Run run, TestStatus status )
   {
      return repository.getByRunAndStatus( run, status );
   }

   public List<ExecutionRecord> getByRunAndName( Run run, String name )
   {
      return repository.getByRunAndName( run, name );
   }

   public List<ExecutionRecord> getByRunAndTestBed( Run run, String testBed )
   {
      return repository.getByRunAndTestBed( run, testBed );
   }

   public List<ExecutionRecord> getByNameAndTestBed( String name, String testBed )
   {
      return repository.getByNameAndTestBed( name, testBed );
   }

   public ExecutionRecord getByRunAndNameAndTestBed( Run run, String name, String testBed )
   {
      return repository.getByRunAndNameAndTestBed( run, name, testBed );
   }
}
