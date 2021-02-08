package org.mvss.karta.server.dao.repositories;

import org.mvss.karta.server.dao.models.Run;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunRepository extends JpaRepository<Run, Long>
{
   public Run getByName( String name );

   // public List<Run> getByRelease( String release );

   // public List<Run> getByReleaseAndBuild( String release, String build );
   //
   // public Run getByReleaseAndBuildAndName( String release, String build, String name );
}
