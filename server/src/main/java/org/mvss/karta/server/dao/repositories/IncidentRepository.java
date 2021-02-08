package org.mvss.karta.server.dao.repositories;

import java.util.List;

import org.mvss.karta.framework.enums.IncidentStatus;
import org.mvss.karta.server.dao.models.ExecutionRecord;
import org.mvss.karta.server.dao.models.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>
{
   public List<Incident> getByExecutionRecord( ExecutionRecord executionRecord );

   public List<Incident> getByStatus( IncidentStatus status );
}
