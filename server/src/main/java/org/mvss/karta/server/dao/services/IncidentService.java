package org.mvss.karta.server.dao.services;

import java.util.List;

import org.mvss.karta.framework.enums.IncidentStatus;
import org.mvss.karta.server.dao.models.ExecutionRecord;
import org.mvss.karta.server.dao.models.Incident;
import org.mvss.karta.server.dao.repositories.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IncidentService
{
   @Autowired
   private IncidentRepository repository;

   public long count()
   {
      return repository.count();
   }

   public List<Incident> getAll()
   {
      return repository.findAll();
   }

   public Incident get( long id )
   {
      return repository.findById( id ).get();
   }

   public long getVersion( long id )
   {
      return get( id ).getVersion();
   }

   public Incident save( Incident incident )
   {
      return repository.save( incident );
   }

   public List<Incident> saveAll( List<Incident> incident )
   {
      return repository.saveAll( incident );
   }

   public void delete( Incident incident )
   {
      repository.delete( incident );
   }

   public void deleteById( long id )
   {
      repository.deleteById( id );
   }

   public void deleteAll()
   {
      repository.deleteAll();
   }

   public List<Incident> getByExecutionRecord( ExecutionRecord executionRecord )
   {
      return repository.getByExecutionRecord( executionRecord );
   }

   public List<Incident> getByStatus( IncidentStatus status )
   {
      return repository.getByStatus( status );
   }
}
