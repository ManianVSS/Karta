package org.mvss.karta.server.service;

import org.mvss.karta.server.models.BaseModel;
import org.mvss.karta.server.models.Test;
import org.mvss.karta.server.models.TestCategory;
import org.mvss.karta.server.repository.TestCategoryRepository;
import org.mvss.karta.server.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TestService extends AbstractService<Test, TestRepository> {
    @Autowired
    private TestCategoryRepository testCategoryRepository;

    @Override
    protected boolean processOptionalReferences(Test Test) {
        TestCategory parent = Test.getParent();

        if (parent != null) {
            Long parentTestCategoryId = parent.getId();

            if (parentTestCategoryId != null) {
                Optional<TestCategory> parentFromORM = testCategoryRepository.findById(parentTestCategoryId);

                if (parentFromORM.isEmpty()) {
                    return false;
                }

                Test.setParent(parentFromORM.get());
            }
        }

        return true;
    }


    @Transactional(readOnly = true)
    public Test findByName(String name) {
        return repository.findByName(name);
    }


    @Override
    public BaseModel fetch(BaseModel model) {
        Test test = (Test) model;
        return findByName(test.getName());
    }
}
