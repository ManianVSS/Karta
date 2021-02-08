package org.mvss.karta.server.dao.models;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.mvss.karta.framework.enums.IncidentStatus;
import org.mvss.karta.framework.runtime.Constants;

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
@Entity
@ToString
@Builder
public class Incident implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   @Column( updatable = false, nullable = false )
   private Long              id;

   @Version
   private Long              version;

   private String            description;

   @Lob
   @Column
   @Basic( fetch = FetchType.EAGER )
   @Size( max = Constants.MAX_BLOB_SIZE )
   private String            details;

   @Builder.Default
   @Column( nullable = false )
   @Enumerated( EnumType.STRING )
   private IncidentStatus    status           = IncidentStatus.DRAFT;

   @JoinColumn( name = "executionRecord_id", nullable = false )
   @ManyToOne( fetch = FetchType.EAGER )
   @JsonIgnoreProperties( {"version", "description", "testBed", "status", "run", "incidentCount", "iterationCount"} )
   private ExecutionRecord   executionRecord;

}
