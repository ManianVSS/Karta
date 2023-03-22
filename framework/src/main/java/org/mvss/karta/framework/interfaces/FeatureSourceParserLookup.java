package org.mvss.karta.framework.interfaces;

import org.mvss.karta.framework.plugins.FeatureSourceParser;

import java.util.ArrayList;

@FunctionalInterface
public interface FeatureSourceParserLookup {
    ArrayList<FeatureSourceParser> lookup(ArrayList<String> featureSourceParserNames);
}
