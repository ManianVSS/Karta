package org.mvss.karta.framework.plugins;

import org.mvss.karta.framework.models.catalog.Test;

import java.util.ArrayList;

public interface TestProvider extends Plugin {
    ArrayList<Test> filterTestsByTag(ArrayList<String> tags);
}
