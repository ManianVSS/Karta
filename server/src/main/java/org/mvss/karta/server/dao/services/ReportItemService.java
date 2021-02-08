package org.mvss.karta.server.dao.services;

import java.util.List;

import org.mvss.karta.server.dao.models.ReportItem;
import org.mvss.karta.server.dao.repositories.ReportItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportItemService
{
   @Autowired
   private ReportItemRepository repository;

   public long count()
   {
      return repository.count();
   }

   public List<ReportItem> getAll()
   {
      return repository.findAll();
   }

   public ReportItem get( long id )
   {
      return repository.findById( id ).get();
   }

   public long getVersion( long id )
   {
      return get( id ).getVersion();
   }

   public ReportItem save( ReportItem reportItem )
   {
      return repository.save( reportItem );
   }

   public List<ReportItem> saveAll( List<ReportItem> reportItem )
   {
      return repository.saveAll( reportItem );
   }

   public void delete( ReportItem reportItem )
   {
      repository.delete( reportItem );
   }

   public void deleteById( long id )
   {
      repository.deleteById( id );
   }

   public void deleteAll()
   {
      repository.deleteAll();
   }

   public List<ReportItem> getByName( String name )
   {
      return repository.getByName( name );
   }

   public List<ReportItem> getByRelease( String release )
   {
      return repository.getByRelease( release );
   }

   public List<ReportItem> getByReleaseAndBuild( String release, String build )
   {
      return repository.getByReleaseAndBuild( release, build );
   }

   public List<ReportItem> getByReleaseAndBuildAndName( String release, String build, String name )
   {
      return repository.getByReleaseAndBuildAndName( release, build, name );
   }
}
