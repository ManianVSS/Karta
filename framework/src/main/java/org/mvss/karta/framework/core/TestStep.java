package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
public class TestStep implements Serializable
{
   /**
    * 
    */
   private static final long                        serialVersionUID = 1L;

   private String                                   identifier;

   private HashMap<String, Serializable>            testData;

   private HashMap<String, ArrayList<Serializable>> testDataSet;

   private String                                   node;
}
