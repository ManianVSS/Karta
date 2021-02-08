package org.mvss.karta.server.dao.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.mvss.karta.framework.enums.TestStatus;

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
@Table( uniqueConstraints = {@UniqueConstraint( columnNames = {"run_id", "name", "testBed"} )} )
public class ExecutionRecord implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   @Column( updatable = false, nullable = false )
   private Long              id;

   @Version
   private Long              version;

   @Column( nullable = false )
   private String            name;

   private String            description;

   @Column( nullable = false )
   private String            testBed;

   @Builder.Default
   @Column( nullable = false )
   @Enumerated( EnumType.STRING )
   private TestStatus        status           = TestStatus.SCHEDULED;

   @JoinColumn( name = "run_id", nullable = false )
   @ManyToOne( fetch = FetchType.EAGER )
   @JsonIgnoreProperties( {"version", "description", "release", "build"} )
   private Run               run;

   private Long              incidentCount;

   private Long              iterationCount;
}
