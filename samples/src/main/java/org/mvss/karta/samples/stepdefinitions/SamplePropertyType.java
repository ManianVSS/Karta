package org.mvss.karta.samples.stepdefinitions;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SamplePropertyType implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   String                    v2v1;
   String[]                  v2v2;
}
