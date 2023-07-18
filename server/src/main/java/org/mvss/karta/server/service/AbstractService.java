package org.mvss.karta.server.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.server.fitering.SearchCriteriaTree;
import org.mvss.karta.server.models.BaseModel;
import org.mvss.karta.server.models.Test;
import org.mvss.karta.server.models.TestCategory;
import org.mvss.karta.server.repository.AbstractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractService<T extends BaseModel, V extends AbstractRepository<T>> implements EntityFetcher {
    @Autowired
    protected V repository;

    public static ConcurrentHashMap<Class<? extends BaseModel>, JpaRepository<? extends BaseModel, Long>> entityRepositoryMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Class<? extends BaseModel>, AbstractService<? extends BaseModel, ? extends AbstractRepository<? extends BaseModel>>> entityServiceMap = new ConcurrentHashMap<>();

    public static final List<Class<? extends BaseModel>> entityDependencySequence;

    static {
        //Put a list of model class in the dependency sequence
        entityDependencySequence = List.of(TestCategory.class, Test.class);
    }

    @Autowired
    protected BeanUtilsBean nonNullCopierBeanUtil;

    @PostConstruct
    protected void init() {
        @SuppressWarnings("unchecked") Class<T> modelClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        entityRepositoryMap.put(modelClass, repository);
        entityServiceMap.put(modelClass, this);
    }

    public long count() {
        return repository.count();
    }

    @Transactional(readOnly = true)
    public List<T> getAll(SearchCriteriaTree searchCriteriaTree) {
        if (searchCriteriaTree != null) {
            Specification<T> spec = searchCriteriaTree.buildSpecification(entityRepositoryMap);
            return repository.findAll(spec);
        }

        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<T> getPage(SearchCriteriaTree searchCriteriaTree, int pageNumber, int paginationSize, String sortBy, boolean ascending) {
        Sort sort = Sort.by(ascending ? Direction.ASC : Direction.DESC, sortBy.split(Constants.COMMA));
        Pageable pageable = PageRequest.of(pageNumber, paginationSize, sort);

        if (searchCriteriaTree != null) {
            Specification<T> spec = searchCriteriaTree.buildSpecification(entityRepositoryMap);

            if (spec == null) {
                return null;
            }

            return repository.findAll(spec, pageable);
        }

        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public T get(long id) {
        return repository.findById(id).orElse(null);
    }

    protected boolean processOptionalReferences(T object) {
        // Add optional references process code above and return true here.
        return true;
    }

    protected boolean processMandatoryReferences(T object, boolean isPatch) {
        // Return isPatch at end if there exists mandatory references
        return true;
    }

    protected boolean processObject(T object) {
        return true;
    }

    public boolean resolveReferences(BaseModel object, HashMap<Class<? extends BaseModel>, HashMap<Long, Long>> idReplaceMaps) {
        Class<? extends BaseModel> modelClass = object.getClass();

        try {
            for (Field field : modelClass.getDeclaredFields()) {
                field.setAccessible(true);
                Class<?> fieldClass = field.getType();
                Object fieldValue = field.get(object);

                if ((fieldValue != null) && BaseModel.class.isAssignableFrom(fieldClass)) {
                    if (!idReplaceMaps.containsKey(fieldClass)) {
                        log.error("Missing id replace map for field class " + fieldClass + " in maps " + idReplaceMaps);
                        return false;
                    }

                    log.debug("Found a referring class " + modelClass + " to " + fieldClass);

                    BaseModel referredModel = (BaseModel) fieldValue;
                    HashMap<Long, Long> idReplaceMap = idReplaceMaps.get(fieldClass);

                    Long referredId = referredModel.getId();
                    if (referredId != null) {
                        if (!idReplaceMap.containsKey(referredId)) {
                            log.error("Id reference " + referredId + " missing in replace map " + idReplaceMap);
                            return false;
                        }
                        referredModel.setId(idReplaceMap.get(referredId));
                        field.set(object, referredModel);
                        log.debug("Replaced model with actual ids " + ParserUtils.getObjectMapper().writeValueAsString(referredModel));
                    } else {
                        log.warn("Referring model id missing " + referredModel);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    @Transactional
    public T add(T object) {
        if (processOptionalReferences(object) && processMandatoryReferences(object, false) && processObject(object)) {
            return repository.save(object);
        }
        return null;
    }

    @Transactional
    public List<T> addAll(List<T> objects) {
        for (T object : objects) {
            if (!(processOptionalReferences(object) && processMandatoryReferences(object, false) && processObject(object))) {
                return null;
            }
        }
        return repository.saveAll(objects);
    }

    @Transactional
    public T update(long id, T object) throws IllegalAccessException, InvocationTargetException {
        Optional<T> optObj = repository.findById(id);

        if (optObj.isEmpty()) {
            return null;
        }

        T entityToUpdate = optObj.get();
        object.copyIdAndVersionFrom(entityToUpdate);

        if (processOptionalReferences(object) && processMandatoryReferences(object, false) && processObject(object)) {
            BeanUtils.copyProperties(entityToUpdate, object);
            return repository.save(entityToUpdate);
        }

        return null;
    }

    //TODO: Change exception thrown on copy properties
    @Transactional
    public T patch(long id, T object) throws IllegalAccessException, InvocationTargetException {
        Optional<T> optObj = repository.findById(id);

        if (optObj.isEmpty()) {
            return null;
        }

        T entityToUpdate = optObj.get();
        object.copyIdAndVersionFrom(entityToUpdate);

        if (processOptionalReferences(object) && processMandatoryReferences(object, true) && processObject(object)) {
            nonNullCopierBeanUtil.copyProperties(entityToUpdate, object);
            return repository.save(entityToUpdate);
        }

        return null;
        //
        //      if ( !processOptionalReferences( object ) || !processMandatoryReferences( object, true ) && processObject( object ) )
        //      {
        //         return null;
        //      }
        //
        //      nonNullCopierBeanUtil.copyProperties( entityToUpdate, object );
        //
        //      return repository.save( entityToUpdate );
    }

    @Transactional
    public void deleteById(long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void delete(T object) {
        repository.delete(object);
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    public BaseModel fetch(BaseModel object) {
        return get(object.getId());
    }
}
