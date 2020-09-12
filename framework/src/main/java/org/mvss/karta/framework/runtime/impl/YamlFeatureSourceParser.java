package org.mvss.karta.framework.runtime.impl;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YamlFeatureSourceParser implements FeatureSourceParser
{

   @Override
   public void initFeatureParser( HashMap<String, Serializable> testProperties ) throws Throwable
   {
      // Nothing to do as of now
   }

   @Override
   public TestFeature parseFeatureSource( String sourceCode ) throws Throwable
   {
      ObjectMapper objectMapper = new ObjectMapper( new YAMLFactory() );
      return objectMapper.readValue( sourceCode, TestFeature.class );
   }

}