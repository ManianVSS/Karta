package org.mvss.karta.server.dao.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

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
// @Table( uniqueConstraints = {@UniqueConstraint( columnNames = {"release", "build", "name"} )} )
public class Run implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue
   @Column( updatable = false, nullable = false )
   private Long              id;

   @Version
   private Long              version;

   @Column( nullable = false, unique = true )
   private String            name;

   private String            description;

   // @Column( nullable = false )
   // private String release;
   //
   // @Column( nullable = false )
   // private String build;

}
