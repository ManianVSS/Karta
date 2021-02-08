package org.mvss.karta.framework.runtime.event.dto;

import java.io.Serializable;

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
public class Run implements Serializable
{
   private static final long serialVersionUID = 1L;

   private Long              id;

   private Long              version;

   private String            name;

   private String            description;
}
