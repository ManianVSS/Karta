package org.mvss.karta.server.service;

import lombok.extern.slf4j.Slf4j;
import org.mvss.karta.server.models.BaseModel;
import org.mvss.karta.server.models.TestCategory;
import org.mvss.karta.server.repository.TestCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TestCategoryService extends AbstractService<TestCategory, TestCategoryRepository> {
   
    @Override
    protected boolean processOptionalReferences(TestCategory TestCategory) {
        TestCategory parent = TestCategory.getParent();
        if (parent != null) {
            Long parentTestCategoryId = parent.getId();

            if (parentTestCategoryId != null) {
                Optional<TestCategory> parentFromORM = repository.findById(parentTestCategoryId);

                if (parentFromORM.isEmpty()) {
                    return false;
                }

                TestCategory.setParent(parentFromORM.get());
            }
        }

        return true;
    }

    @Transactional(readOnly = true)
    public List<TestCategory> findByParent(Long parentId) {
        return parentId >= 0 ? repository.findByParent(get(parentId)) : repository.findByParentIsNull();
    }

    @Transactional(readOnly = true)
    public TestCategory findByParentAndName(TestCategory parent, String name) {
        return parent == null ? repository.findByParentIsNullAndName(name).orElse(null) : repository.findByParentAndName(parent, name).orElse(null);
    }

    @Transactional(readOnly = true)
    public TestCategory findByParentAndName(Long parentId, String name) {
        return parentId >= 0 ? repository.findByParentAndName(get(parentId), name).orElse(null) : repository.findByParentIsNullAndName(name).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<TestCategory> findByParentIsNull() {
        return repository.findByParentIsNull();
    }

    @Transactional(readOnly = true)
    public Optional<TestCategory> findByParentIsNullAndName(String name) {
        return repository.findByParentIsNullAndName(name);
    }


    @Override
    public BaseModel fetch(BaseModel model) {
        TestCategory TestCategory = (TestCategory) model;
        return findByParentAndName(TestCategory.getParent(), TestCategory.getName());
    }
}
