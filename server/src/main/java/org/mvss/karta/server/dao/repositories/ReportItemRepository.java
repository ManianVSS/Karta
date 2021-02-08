package org.mvss.karta.server.dao.repositories;

import java.util.List;

import org.mvss.karta.server.dao.models.ReportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportItemRepository extends JpaRepository<ReportItem, Long>
{
   public List<ReportItem> getByName( String name );

   public List<ReportItem> getByRelease( String release );

   public List<ReportItem> getByReleaseAndBuild( String release, String build );

   public List<ReportItem> getByReleaseAndBuildAndName( String release, String build, String name );
}
