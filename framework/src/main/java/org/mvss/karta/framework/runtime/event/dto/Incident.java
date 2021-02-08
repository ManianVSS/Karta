package org.mvss.karta.framework.runtime.event.dto;

import java.io.Serializable;

import org.mvss.karta.framework.enums.IncidentStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Incident implements Serializable
{
   private static final long serialVersionUID = 1L;

   private Long              id;

   private Long              version;

   private String            description;

   private String            details;

   @Builder.Default
   private IncidentStatus    status           = IncidentStatus.DRAFT;

   @JsonIgnoreProperties( {"version", "description", "testBed", "status", "run", "incidentCount", "iterationCount"} )
   private ExecutionRecord   executionRecord;

}
