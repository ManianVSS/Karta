package org.mvss.karta.server.api.curd;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.server.BadRequestException;
import org.mvss.karta.server.dao.models.Run;
import org.mvss.karta.server.dao.services.RunService;
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
public class RunController
{
   // private static final String BUILD = "build";
   // private static final String RELEASE = "release";
   private static final String NAME                = "name";

   private static final String accessPath          = Constants.PATH_API_RUNS;
   private static final String accessPathCount     = accessPath + "Count";
   private static final String accessByIdPath      = accessPath + "/{id}";
   private static final String getVersionByIdPath  = accessByIdPath + "/version";
   private static final String accessPathAddAll    = accessPath + "AddAll";
   private static final String accessPathUpdateAll = accessPath + "UpdateAll";
   private static final String accessPathPatchAll  = accessPath + "PatchAll";
   private static final String accessPathDeleteAll = accessPath + "DeleteAll";

   private static final String accessByNamePath    = accessPath + "ByName";
   // private static final String accessByReleasePath = accessPath + "ByRelease";
   // private static final String accessByReleaseAndBuildPath = accessPath + "ByReleaseAndBuild";
   // private static final String accessByReleaseAndBuildAndNamePath = accessPath + "ByReleaseAndBuildAndName";

   @Autowired
   private RunService          service;

   @Autowired
   private BeanUtilsBean       nonNullCopier;

   @RequestMapping( value = accessPathCount, method = RequestMethod.GET )
   public long count()
   {
      return service.count();
   }

   @RequestMapping( value = accessPath, method = RequestMethod.GET )
   public List<Run> getAll()
   {
      return service.getAll();
   }

   @RequestMapping( value = accessByIdPath, method = RequestMethod.GET )
   public Run get( @PathVariable long id )
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
   public Run add( @RequestBody Run obj ) throws BadRequestException
   {
      // String release = obj.getRelease();
      // String build = obj.getBuild();

      // String name = obj.getName();
      //
      // if ( name == null )// ( ( release == null ) || ( build == null ) || ( name == null ) )
      // {
      // throw new BadRequestException( "Run name is a mandatory parameter." );
      // }
      //
      // Run existingRun = service.getByName( name );
      //
      // if ( existingRun != null )
      // {
      // return existingRun;
      // }

      return service.save( obj );
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessByIdPath, method = RequestMethod.PUT )
   public Run update( @PathVariable long id, @RequestBody Run run ) throws IllegalAccessException, InvocationTargetException
   {
      Run runToModify = get( id );
      run.setId( runToModify.getId() );
      run.setVersion( runToModify.getVersion() );
      BeanUtils.copyProperties( runToModify, run );
      return service.save( runToModify );
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessByIdPath, method = RequestMethod.PATCH )
   public Run patch( @PathVariable long id, @RequestBody Run run ) throws IllegalAccessException, InvocationTargetException
   {
      Run runToModify = get( id );
      run.setId( null );
      run.setVersion( null );
      nonNullCopier.copyProperties( runToModify, run );
      return service.save( runToModify );
   }

   @ResponseStatus( value = HttpStatus.CREATED )
   @RequestMapping( value = accessPathAddAll, method = RequestMethod.POST )
   public List<Run> saveAll( @RequestBody List<Run> runs )
   {
      return service.saveAll( runs );
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessPathUpdateAll, method = RequestMethod.POST )
   public List<Run> updateAll( @RequestBody List<Run> runs ) throws IllegalAccessException, InvocationTargetException
   {
      List<Run> toReturn = new ArrayList<Run>();
      for ( Run run : runs )
      {
         toReturn.add( update( run.getId(), run ) );
      }
      return toReturn;
   }

   @ResponseStatus( value = HttpStatus.ACCEPTED )
   @RequestMapping( value = accessPathPatchAll, method = RequestMethod.POST )
   public List<Run> patchAll( @RequestBody List<Run> runs ) throws IllegalAccessException, InvocationTargetException
   {
      List<Run> toReturn = new ArrayList<Run>();
      for ( Run run : runs )
      {
         toReturn.add( patch( run.getId(), run ) );
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
   public Run getByName( @RequestParam( name = NAME ) String name ) throws BadRequestException
   {
      // if ( name == null )
      // {
      // throw new BadRequestException( "Run name is a mandatory parameter." );
      // }
      //
      // Run existingRun = service.getByName( name );
      //
      // if ( existingRun != null )
      // {
      // return existingRun;
      // }
      // else
      // {
      // return add( Run.builder().name( name ).build() );
      // }

      return service.getByName( name );
   }
   //
   // @RequestMapping( value = accessByReleasePath, method = RequestMethod.GET )
   // public List<Run> getByRelease( @RequestParam( name = RELEASE ) String release )
   // {
   // return service.getByRelease( release );
   // }
   //
   // @RequestMapping( value = accessByReleaseAndBuildPath, method = RequestMethod.GET )
   // public List<Run> getByReleaseAndBuild( @RequestParam( name = RELEASE ) String release, @RequestParam( name = BUILD ) String build )
   // {
   // return service.getByReleaseAndBuild( release, build );
   // }
   //
   // @RequestMapping( value = accessByReleaseAndBuildAndNamePath, method = RequestMethod.GET )
   // public Run getByReleaseAndBuildAndName( @RequestParam( name = RELEASE ) String release, @RequestParam( name = BUILD ) String build, @RequestParam( name = NAME ) String name )
   // {
   // return service.getByReleaseAndBuildAndName( release, build, name );
   // }
}
