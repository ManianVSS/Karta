package org.mvss.karta.server.api.curd;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.mvss.karta.server.dao.models.ReportItem;
import org.mvss.karta.server.dao.services.ReportItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
public class ReportItemController
{
   private static final String BUILD                              = "build";
   private static final String RELEASE                            = "release";
   private static final String NAME                               = "name";

   private static final String accessPath                         = "/api/reportItems";
   private static final String accessPathCount                    = accessPath + "Count";
   private static final String accessByIdPath                     = accessPath + "/{id}";
   private static final String getVersionByIdPath                 = accessByIdPath + "/version";
   private static final String accessPathAddAll                   = accessPath + "AddAll";
   private static final String accessPathUpdateAll                = accessPath + "UpdateAll";
   private static final String accessPathPatchAll                 = accessPath + "PatchAll";
   private static final String accessPathDeleteAll                = accessPath + "DeleteAll";

   private static final String accessByNamePath                   = accessPath + "ByName";
   private static final String accessByReleasePath                = accessPath + "ByRelease";
   private static final String accessByReleaseAndBuildPath        = accessPath + "ByReleaseAndBuild";
   private static final String accessByReleaseAndBuildAndNamePath = accessPath + "ByReleaseAndBuildAndName";

   @Autowired
   private ReportItemService   service;

   @Autowired
   private BeanUtilsBean       nonNullCopier;

   @RequestMapping( value = accessPathCount, method = RequestMethod.GET )
   public long count()
   {
      return service.count();
   }

   @RequestMapping( value = accessPath, method = RequestMethod.GET )
   public List<ReportItem> getAll()
   {
      return service.getAll();
   }

   @RequestMapping( value = accessByIdPath, method = RequestMethod.GET )
   public ReportItem get( @PathVariable long id )
   {
      return service.get( id );
   }

   @RequestMapping( value = getVersionByIdPath, method = RequestMethod.GET )
   public long getVersion( @PathVariable long id )
   {
      return service.getVersion( id );
   }

   @ResponseStatus( value = HttpStatus.CREATED )
   @RequestMapping( value = accessPath, method = RequestMethod.POST )
   public ReportItem add( @RequestBody ReportItem obj )
   {
      return service.save( obj );
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessByIdPath, method = RequestMethod.PUT )
   public ReportItem update( @PathVariable long id, @RequestBody ReportItem reportItem ) throws IllegalAccessException, InvocationTargetException
   {
      ReportItem reportItemToModify = get( id );
      reportItem.setId( reportItemToModify.getId() );
      reportItem.setVersion( reportItemToModify.getVersion() );
      BeanUtils.copyProperties( reportItemToModify, reportItem );
      return service.save( reportItemToModify );
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessByIdPath, method = RequestMethod.PATCH )
   public ReportItem patch( @PathVariable long id, @RequestBody ReportItem reportItem ) throws IllegalAccessException, InvocationTargetException
   {
      ReportItem reportItemToModify = get( id );
      reportItem.setId( null );
      reportItem.setVersion( null );
      nonNullCopier.copyProperties( reportItemToModify, reportItem );
      return service.save( reportItemToModify );
   }

   @ResponseStatus( value = HttpStatus.CREATED )
   @RequestMapping( value = accessPathAddAll, method = RequestMethod.POST )
   public List<ReportItem> saveAll( @RequestBody List<ReportItem> reportItems )
   {
      return service.saveAll( reportItems );
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessPathUpdateAll, method = RequestMethod.POST )
   public List<ReportItem> updateAll( @RequestBody List<ReportItem> reportItems ) throws IllegalAccessException, InvocationTargetException
   {
      List<ReportItem> toReturn = new ArrayList<ReportItem>();
      for ( ReportItem reportItem : reportItems )
      {
         toReturn.add( update( reportItem.getId(), reportItem ) );
      }
      return toReturn;
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessPathPatchAll, method = RequestMethod.POST )
   public List<ReportItem> patchAll( @RequestBody List<ReportItem> reportItems ) throws IllegalAccessException, InvocationTargetException
   {
      List<ReportItem> toReturn = new ArrayList<ReportItem>();
      for ( ReportItem reportItem : reportItems )
      {
         toReturn.add( patch( reportItem.getId(), reportItem ) );
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
   public List<ReportItem> getByName( @RequestParam( name = NAME ) String name )
   {
      return service.getByName( name );
   }

   @RequestMapping( value = accessByReleasePath, method = RequestMethod.GET )
   public List<ReportItem> getByRelease( @RequestParam( name = RELEASE ) String release )
   {
      return service.getByRelease( release );
   }

   @RequestMapping( value = accessByReleaseAndBuildPath, method = RequestMethod.GET )
   public List<ReportItem> getByReleaseAndBuild( @RequestParam( name = RELEASE ) String release, @RequestParam( name = BUILD ) String build )
   {
      return service.getByReleaseAndBuild( release, build );
   }

   @RequestMapping( value = accessByReleaseAndBuildAndNamePath, method = RequestMethod.GET )
   public List<ReportItem> getByReleaseAndBuildAndName( @RequestParam( name = RELEASE ) String release, @RequestParam( name = BUILD ) String build, @RequestParam( name = NAME ) String name )
   {
      return service.getByReleaseAndBuildAndName( release, build, name );
   }
}
