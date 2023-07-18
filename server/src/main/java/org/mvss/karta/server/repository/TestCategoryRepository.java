package org.mvss.karta.server.repository;


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
public interface TestCategoryRepository extends AbstractRepository<TestCategory> {
    @Override
    @Query("select i from TestCategory i where i.id = ?1")
    Optional<TestCategory> findById(Long id);

    Page<TestCategory> findAll(Pageable pageable);

    Page<TestCategory> findByCreatedAtBefore(Date createdAt, Pageable pageable);

    Page<TestCategory> findByUpdatedAtBefore(Date createdAt, Pageable pageable);

    Page<TestCategory> findByCreatedAtAfter(Date createdAt, Pageable pageable);

    Page<TestCategory> findByUpdatedAtAfter(Date createdAt, Pageable pageable);

    long deleteByCreatedAtBefore(Date createdAt);

    long deleteByUpdatedAtBefore(Date createdAt);

    List<TestCategory> findByParent(TestCategory parent);

    //   @Query( "select i from TestCategory i where i.parent = ?1 and i.name=$2" )
    Optional<TestCategory> findByParentAndName(TestCategory parent, String name);

    Optional<TestCategory> findByParentIsNullAndName(String name);

    List<TestCategory> findByParentIsNull();

    List<TestCategory> findByParentIn(List<Long> parentTestCategoryIdList);
}
