package org.mvss.karta.server.api;

import org.mvss.karta.server.Constants;
import org.mvss.karta.server.fitering.SearchCriteriaTree;
import org.mvss.karta.server.models.TestCategory;
import org.mvss.karta.server.service.TestCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;

@CrossOrigin
@RestController
public class TestCategoryController {
    @Autowired
    private TestCategoryService testCategoryService;

    @ResponseStatus(code = HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, value = Constants.PATH_API + Constants.PATH_TEST_CATEGORIES)
    public Page<TestCategory> getAll(@RequestBody(required = false) SearchCriteriaTree searchCriteriaTree, @RequestParam(required = false, defaultValue = Constants.PV_ZERO) int pageNumber, @RequestParam(required = false, defaultValue = Constants.PV_HUNDRED) int paginationSize, @RequestParam(required = false, defaultValue = Constants.PV_ID) String sortBy, @RequestParam(required = false, defaultValue = Constants.PV_TRUE) boolean ascending) {
        return testCategoryService.getPage(searchCriteriaTree, pageNumber, paginationSize, sortBy, ascending);
    }

    @ResponseStatus(code = HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, value = Constants.PATH_API + Constants.PATH_TEST_CATEGORIES + Constants.API_SEARCH)
    public Page<TestCategory> search(@RequestBody(required = false) SearchCriteriaTree searchCriteriaTree, @RequestParam(required = false, defaultValue = Constants.PV_ZERO) int pageNumber, @RequestParam(required = false, defaultValue = Constants.PV_HUNDRED) int paginationSize, @RequestParam(required = false, defaultValue = Constants.PV_ID) String sortBy, @RequestParam(required = false, defaultValue = Constants.PV_TRUE) boolean ascending) {
        return testCategoryService.getPage(searchCriteriaTree, pageNumber, paginationSize, sortBy, ascending);
    }

    @ResponseStatus(code = HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, value = Constants.PATH_API + Constants.PATH_TEST_CATEGORIES + Constants.API_ID)
    //TODO: Change null find as 404 response for all controllers
    public TestCategory get(@PathVariable long id) {
        return testCategoryService.get(id);
    }

    @RequestMapping(method = RequestMethod.POST, value = Constants.PATH_API + Constants.PATH_TEST_CATEGORIES)
    public ResponseEntity<TestCategory> add(@RequestBody TestCategory category) {
        TestCategory categoryORM = testCategoryService.add(category);
        if (categoryORM == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(categoryORM, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, value = Constants.PATH_API + Constants.PATH_TEST_CATEGORIES + Constants.API_ID)
    public ResponseEntity<TestCategory> update(@PathVariable long id, @RequestBody TestCategory category) throws IllegalAccessException, InvocationTargetException {
        TestCategory categoryORM = testCategoryService.update(id, category);
        if (categoryORM == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(categoryORM, HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.PATCH, value = Constants.PATH_API + Constants.PATH_TEST_CATEGORIES + Constants.API_ID)
    public ResponseEntity<TestCategory> patch(@PathVariable long id, @RequestBody TestCategory category) throws IllegalAccessException, InvocationTargetException {
        TestCategory categoryFromORM = testCategoryService.patch(id, category);
        if (categoryFromORM == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(categoryFromORM, HttpStatus.ACCEPTED);
    }

    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @RequestMapping(method = RequestMethod.DELETE, value = Constants.PATH_API + Constants.PATH_TEST_CATEGORIES + Constants.API_ID)
    public void deleteById(@PathVariable long id) {
        testCategoryService.deleteById(id);
    }

    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @RequestMapping(method = RequestMethod.DELETE, value = Constants.PATH_API + Constants.PATH_TEST_CATEGORIES)
    public void deleteAll() {
        testCategoryService.deleteAll();
    }
}
