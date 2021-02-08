package org.mvss.karta.server.dao.repositories;

import java.util.List;

import org.mvss.karta.framework.enums.TestStatus;
import org.mvss.karta.server.dao.models.ExecutionRecord;
import org.mvss.karta.server.dao.models.Run;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionRecordRepository extends JpaRepository<ExecutionRecord, Long>
{
   public List<ExecutionRecord> getByName( String name );

   public List<ExecutionRecord> getByRun( Run run );

   public List<ExecutionRecord> getByRunAndStatus( Run run, TestStatus status );

   public List<ExecutionRecord> getByRunAndName( Run run, String name );

   public List<ExecutionRecord> getByRunAndTestBed( Run run, String testBed );

   public List<ExecutionRecord> getByNameAndTestBed( String name, String testBed );

   public ExecutionRecord getByRunAndNameAndTestBed( Run run, String name, String testBed );
}
