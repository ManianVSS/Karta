package org.mvss.karta.server.api;

import org.mvss.karta.server.Constants;
import org.mvss.karta.server.fitering.SearchCriteriaTree;
import org.mvss.karta.server.models.Test;
import org.mvss.karta.server.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;

@CrossOrigin
@RestController
public class TestController {
    @Autowired
    private TestService testCategoryService;

    @ResponseStatus(code = HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, value = Constants.PATH_API + Constants.PATH_TESTS)
    public Page<Test> getAll(@RequestBody(required = false) SearchCriteriaTree searchCriteriaTree, @RequestParam(required = false, defaultValue = Constants.PV_ZERO) int pageNumber, @RequestParam(required = false, defaultValue = Constants.PV_HUNDRED) int paginationSize, @RequestParam(required = false, defaultValue = Constants.PV_ID) String sortBy, @RequestParam(required = false, defaultValue = Constants.PV_TRUE) boolean ascending) {
        return testCategoryService.getPage(searchCriteriaTree, pageNumber, paginationSize, sortBy, ascending);
    }

    @ResponseStatus(code = HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, value = Constants.PATH_API + Constants.PATH_TESTS + Constants.API_SEARCH)
    public Page<Test> search(@RequestBody(required = false) SearchCriteriaTree searchCriteriaTree, @RequestParam(required = false, defaultValue = Constants.PV_ZERO) int pageNumber, @RequestParam(required = false, defaultValue = Constants.PV_HUNDRED) int paginationSize, @RequestParam(required = false, defaultValue = Constants.PV_ID) String sortBy, @RequestParam(required = false, defaultValue = Constants.PV_TRUE) boolean ascending) {
        return testCategoryService.getPage(searchCriteriaTree, pageNumber, paginationSize, sortBy, ascending);
    }

    @ResponseStatus(code = HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, value = Constants.PATH_API + Constants.PATH_TESTS + Constants.API_ID)
    //TODO: Change null find as 404 response for all controllers
    public Test get(@PathVariable long id) {
        return testCategoryService.get(id);
    }

    @RequestMapping(method = RequestMethod.POST, value = Constants.PATH_API + Constants.PATH_TESTS)
    public ResponseEntity<Test> add(@RequestBody Test category) {
        Test categoryORM = testCategoryService.add(category);
        if (categoryORM == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(categoryORM, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, value = Constants.PATH_API + Constants.PATH_TESTS + Constants.API_ID)
    public ResponseEntity<Test> update(@PathVariable long id, @RequestBody Test category) throws IllegalAccessException, InvocationTargetException {
        Test categoryORM = testCategoryService.update(id, category);
        if (categoryORM == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(categoryORM, HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.PATCH, value = Constants.PATH_API + Constants.PATH_TESTS + Constants.API_ID)
    public ResponseEntity<Test> patch(@PathVariable long id, @RequestBody Test category) throws IllegalAccessException, InvocationTargetException {
        Test categoryFromORM = testCategoryService.patch(id, category);
        if (categoryFromORM == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(categoryFromORM, HttpStatus.ACCEPTED);
    }

    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @RequestMapping(method = RequestMethod.DELETE, value = Constants.PATH_API + Constants.PATH_TESTS + Constants.API_ID)
    public void deleteById(@PathVariable long id) {
        testCategoryService.deleteById(id);
    }

    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @RequestMapping(method = RequestMethod.DELETE, value = Constants.PATH_API + Constants.PATH_TESTS)
    public void deleteAll() {
        testCategoryService.deleteAll();
    }
}
