package org.mvss.karta.server.repository;

import org.mvss.karta.server.models.BaseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AbstractRepository<T extends BaseModel> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

}
