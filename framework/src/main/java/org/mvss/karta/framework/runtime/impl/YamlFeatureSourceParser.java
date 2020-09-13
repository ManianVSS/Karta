package org.mvss.karta.framework.runtime.impl;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YamlFeatureSourceParser implements FeatureSourceParser
{
   ObjectMapper objectMapper;

   @Override
   public void initFeatureParser( HashMap<String, Serializable> testProperties ) throws Throwable
   {
      objectMapper = new ObjectMapper( new YAMLFactory() );
   }

   @Override
   public TestFeature parseFeatureSource( String sourceCode ) throws Throwable
   {
      return objectMapper.readValue( sourceCode, TestFeature.class );
   }

}
