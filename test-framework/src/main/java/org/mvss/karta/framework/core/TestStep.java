package org.mvss.karta.framework.core;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestStep implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * The actual conjunction used in the step.
    * e.g. For Gherkin: "Given", "When", "Then"
    */
   private String            conjunction;

   /**
    * The step definition identifier.
    * e.g. For Gherkin - Words following the conjunction like Given the plate is full of &quotalmond&quot cookies
    */
   private String            identifier;
}
