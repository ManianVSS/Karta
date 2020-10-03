package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.HashMap;

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
   private static final long             serialVersionUID = 1L;

   private String                        identifier;

   private HashMap<String, Serializable> testData;

   private String                        node;
}
