package org.mvss.karta.server.service;

import org.mvss.karta.server.models.BaseModel;

@FunctionalInterface
public interface EntityFetcher {
    BaseModel fetch(BaseModel model);
}
