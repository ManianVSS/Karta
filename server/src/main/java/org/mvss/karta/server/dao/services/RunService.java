package org.mvss.karta.server.dao.services;

import java.util.List;

import org.mvss.karta.server.dao.models.Run;
import org.mvss.karta.server.dao.repositories.RunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RunService
{
   @Autowired
   private RunRepository repository;

   public long count()
   {
      return repository.count();
   }

   public List<Run> getAll()
   {
      return repository.findAll();
   }

   public Run get( long id )
   {
      return repository.findById( id ).get();
   }

   public long getVersion( long id )
   {
      return get( id ).getVersion();
   }

   public Run save( Run run )
   {
      return repository.save( run );
   }

   public List<Run> saveAll( List<Run> run )
   {
      return repository.saveAll( run );
   }

   public void delete( Run run )
   {
      repository.delete( run );
   }

   public void deleteById( long id )
   {
      repository.deleteById( id );
   }

   public void deleteAll()
   {
      repository.deleteAll();
   }

   public Run getByName( String name )
   {
      return repository.getByName( name );
   }

   // public List<Run> getByRelease( String release )
   // {
   // return repository.getByRelease( release );
   // }
   //
   // public List<Run> getByReleaseAndBuild( String release, String build )
   // {
   // return repository.getByReleaseAndBuild( release, build );
   // }
   //
   // public Run getByReleaseAndBuildAndName( String release, String build, String name )
   // {
   // return repository.getByReleaseAndBuildAndName( release, build, name );
   // }
}
