package org.mvss.karta.server.repository;

import org.mvss.karta.server.models.Test;
import org.mvss.karta.server.models.TestCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface TestRepository extends AbstractRepository<Test> {
    @Override
    @Query("select i from Test i where i.id = ?1")
    Optional<Test> findById(Long id);

    Page<Test> findAll(Pageable pageable);

    Page<Test> findByCreatedAtBefore(Date createdAt, Pageable pageable);

    Page<Test> findByUpdatedAtBefore(Date createdAt, Pageable pageable);

    Page<Test> findByCreatedAtAfter(Date createdAt, Pageable pageable);

    Page<Test> findByUpdatedAtAfter(Date createdAt, Pageable pageable);

    long deleteByCreatedAtBefore(Date createdAt);

    long deleteByUpdatedAtBefore(Date createdAt);

    Page<Test> findByParent(TestCategory parent, Pageable pageable);

    @Query("select c from Test c where c.parent.id = ?1")
    Page<Test> findByParentId(Long parentId, Pageable pageable);

    Page<Test> findByParentIsNull(Pageable pageable);

    Test findByName(String name);

    Page<Test> findByParentIn(List<TestCategory> parentCategoryList, Pageable pageable);

    @Query("select c from Test c where c.parent.id in ?1")
    Page<Test> findByParentIdIn(List<Long> parentCategoryIdList, Pageable pageable);
}
