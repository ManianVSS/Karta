package org.mvss.karta.server.fitering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.server.Constants;
import org.mvss.karta.server.models.BaseModel;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultSpecification<T> implements Specification<T> {

    protected static final long serialVersionUID = 1L;

    protected SearchCriteria criteria;
    protected Map<Class<? extends BaseModel>, JpaRepository<? extends BaseModel, Long>> entityRepositoryMap;

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        String criteriaKey = criteria.getKey();
        Serializable criteriaValue = criteria.getValue();
        Class<?> valueType = root.get(criteriaKey).getJavaType();

        String criteriaStrValue = (criteriaValue == null) ? null : criteriaValue.toString();

        query.distinct(true);
        try {
            if (valueType == Float.class) {
                return getFloatPredicate(root, builder, criteriaKey, criteriaStrValue);
            } else if (valueType == Double.class) {
                return getDoublePredicate(root, builder, criteriaKey, criteriaStrValue);
            } else if (valueType == Integer.class) {
                return getIntegerPredicate(root, builder, criteriaKey, criteriaStrValue);
            } else if (valueType == Long.class) {
                return getLongPredicate(root, builder, criteriaKey, criteriaStrValue);
            } else if (valueType == Boolean.class) {
                return getBooleanPredicate(root, builder, criteriaKey, criteriaStrValue);
            } else if (Map.class.isAssignableFrom(valueType)) {
                return getMapPredicate(root, builder, criteriaKey, criteriaValue);
            }
            // else if ( List.class.isAssignableFrom( valueType ) )
            // {
            // return getListPredicate( root, builder, criteriaKey, criteriaValue );
            // }
            else if (valueType.isEnum()) {
                return getEnumPredicate(root, builder, criteriaKey, criteriaValue, valueType);
            } else if (BaseModel.class.isAssignableFrom(valueType)) {
                return getBaseModelPredicate(root, builder, criteriaKey, valueType, criteriaStrValue);
            } else {
                return getStringPredicate(root, builder, criteriaKey, criteriaStrValue);
            }
        } catch (Throwable e) {
            return null;
        }
    }

    protected Predicate getStringPredicate(Root<T> root, CriteriaBuilder builder, String criteriaKey, String criteriaStrValue) {
        Path<String> criteriaKeyPath = root.get(criteriaKey);
        switch (criteria.getOperation()) {
            case Constants.OP_GT:
                return builder.greaterThan(criteriaKeyPath, criteriaStrValue);
            case Constants.OP_GTE:
                return builder.greaterThanOrEqualTo(criteriaKeyPath, criteriaStrValue);
            case Constants.OP_LT:
                return builder.lessThan(criteriaKeyPath, criteriaStrValue);
            case Constants.OP_LTE:
                return builder.lessThanOrEqualTo(criteriaKeyPath, criteriaStrValue);
            case Constants.OP_DEQ:
            case Constants.OP_EQ:
                return builder.equal(criteriaKeyPath, criteriaStrValue);
            case Constants.OP_LIKE:
            case Constants.OP_LIKEEQ:
            default:
                return builder.like(builder.lower(criteriaKeyPath), criteriaStrValue.toLowerCase());
            case Constants.OP_NOTLIKE:
            case Constants.OP_NOTLIKEEQ:
                return builder.notLike(builder.lower(criteriaKeyPath), criteriaStrValue.toLowerCase());
            case Constants.OP_NEQ:
            case Constants.OP_NWEQ:
                return builder.notEqual(criteriaKeyPath, criteriaStrValue);
        }
    }

    protected Predicate getBaseModelPredicate(Root<T> root, CriteriaBuilder builder, String criteriaKey, Class<?> valueType, String criteriaStrValue) {
        Path<BaseModel> criteriaKeyPath = root.get(criteriaKey);
        JpaRepository<? extends BaseModel, Long> entityRepository = entityRepositoryMap.get(valueType);

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_EXISTS)) {
            return builder.isNotNull(criteriaKeyPath);
        }

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_DOES_NOT_EXISTS)) {
            return builder.isNull(criteriaKeyPath);
        }

        Optional<? extends BaseModel> fetchedEntity = entityRepository.findById(Long.parseLong(criteriaStrValue));

        if (fetchedEntity.isEmpty()) {
            return null;
        }

        BaseModel criteriaParsedValue = fetchedEntity.get();
        switch (criteria.getOperation()) {
            case Constants.OP_DEQ:
            case Constants.OP_EQ:
            default:
                return builder.equal(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_NEQ:
            case Constants.OP_NWEQ:
                return builder.notEqual(criteriaKeyPath, criteriaParsedValue);
        }
    }

    @SuppressWarnings("unchecked")
    protected Predicate getEnumPredicate(Root<T> root, CriteriaBuilder builder, String criteriaKey, Serializable criteriaValue, Class<?> valueType) {
        Path<?> criteriaKeyPath = root.get(criteriaKey);

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_EXISTS)) {
            return builder.isNotNull(criteriaKeyPath);
        }

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_DOES_NOT_EXISTS)) {
            return builder.isNull(criteriaKeyPath);
        }

        @SuppressWarnings({"rawtypes"}) Class<? extends Enum> enumType = (Class<? extends Enum>) valueType;
        Enum<?> criteriaParsedValue = Enum.valueOf(enumType, criteriaValue.toString());

        switch (criteria.getOperation()) {
            case Constants.OP_DEQ:
            case Constants.OP_EQ:
            default:
                return builder.equal(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_NEQ:
            case Constants.OP_NWEQ:
                return builder.notEqual(criteriaKeyPath, criteriaParsedValue);
        }
    }

    protected Predicate getMapPredicate(Root<T> root, CriteriaBuilder builder, String criteriaKey, Serializable criteriaValue) {
        MapJoin<T, String, String> join = root.joinMap(criteriaKey);
        HashMap<String, Serializable> criteriaValueMap = ParserUtils.getObjectMapper().convertValue(criteriaValue, ParserUtils.genericHashMapObjectType);
        Predicate mapPredicate = null;

        for (Entry<String, Serializable> entry : criteriaValueMap.entrySet()) {
            switch (criteria.getOperation()) {
                case Constants.OP_DEQ:
                case Constants.OP_EQ:
                    if (mapPredicate == null) {
                        mapPredicate = builder.and(builder.equal(join.key(), entry.getKey()), builder.equal(join.value(), entry.getValue()));
                    } else {
                        mapPredicate = builder.and(mapPredicate, builder.equal(join.key(), entry.getKey()), builder.equal(join.value(), entry.getValue()));
                    }
                    break;

                case Constants.OP_NEQ:
                case Constants.OP_NWEQ:
                    if (mapPredicate == null) {
                        mapPredicate = builder.and(builder.equal(join.key(), entry.getKey()), builder.notEqual(join.value(), entry.getValue()));
                    } else {
                        mapPredicate = builder.and(mapPredicate, builder.equal(join.key(), entry.getKey()), builder.notEqual(join.value(), entry.getValue()));
                    }
                    break;

                // case Constants.OP_GT:
                // if ( mapPredicate == null )
                // {
                // mapPredicate = builder.and( builder.equal( join.key(), entry.getKey() ), builder.greaterThan( join.value(), entry.getValue() ) );
                // }
                // else
                // {
                // mapPredicate = builder
                // .and( mapPredicate, builder.equal( join.key(), entry.getKey() ), builder.greaterThan( join.value(), entry.getValue() ) );
                // }
                // break;
            }

        }
        if (mapPredicate == null) {
            mapPredicate = builder.and();
        }
        return mapPredicate;
    }

    protected Predicate getBooleanPredicate(Root<T> root, CriteriaBuilder builder, String criteriaKey, String criteriaStrValue) {
        Path<Boolean> criteriaKeyPath = root.get(criteriaKey);

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_EXISTS)) {
            return builder.isNotNull(criteriaKeyPath);
        }

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_DOES_NOT_EXISTS)) {
            return builder.isNull(criteriaKeyPath);
        }

        Boolean criteriaParsedValue = Boolean.valueOf(criteriaStrValue);

        switch (criteria.getOperation()) {
            case Constants.OP_GT:
                return builder.greaterThan(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_GTE:
                return builder.greaterThanOrEqualTo(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_LT:
                return builder.lessThan(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_LTE:
                return builder.lessThanOrEqualTo(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_DEQ:
            case Constants.OP_EQ:
            default:
                return builder.equal(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_NEQ:
            case Constants.OP_NWEQ:
                return builder.notEqual(criteriaKeyPath, criteriaParsedValue);
        }
    }

    protected Predicate getLongPredicate(Root<T> root, CriteriaBuilder builder, String criteriaKey, String criteriaStrValue) {
        Path<Long> criteriaKeyPath = root.get(criteriaKey);

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_EXISTS)) {
            return builder.isNotNull(criteriaKeyPath);
        }

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_DOES_NOT_EXISTS)) {
            return builder.isNull(criteriaKeyPath);
        }

        Long criteriaParsedValue = Long.valueOf(criteriaStrValue);

        switch (criteria.getOperation()) {
            case Constants.OP_GT:
                return builder.greaterThan(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_GTE:
                return builder.greaterThanOrEqualTo(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_LT:
                return builder.lessThan(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_LTE:
                return builder.lessThanOrEqualTo(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_DEQ:
            case Constants.OP_EQ:
            default:
                return builder.equal(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_NEQ:
            case Constants.OP_NWEQ:
                return builder.notEqual(criteriaKeyPath, criteriaParsedValue);
        }
    }

    protected Predicate getIntegerPredicate(Root<T> root, CriteriaBuilder builder, String criteriaKey, String criteriaStrValue) {
        Path<Integer> criteriaKeyPath = root.get(criteriaKey);

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_EXISTS)) {
            return builder.isNotNull(criteriaKeyPath);
        }

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_DOES_NOT_EXISTS)) {
            return builder.isNull(criteriaKeyPath);
        }

        Integer criteriaParsedValue = Integer.valueOf(criteriaStrValue);

        switch (criteria.getOperation()) {
            case Constants.OP_GT:
                return builder.greaterThan(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_GTE:
                return builder.greaterThanOrEqualTo(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_LT:
                return builder.lessThan(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_LTE:
                return builder.lessThanOrEqualTo(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_DEQ:
            case Constants.OP_EQ:
            default:
                return builder.equal(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_NEQ:
            case Constants.OP_NWEQ:
                return builder.notEqual(criteriaKeyPath, criteriaParsedValue);
        }
    }

    protected Predicate getDoublePredicate(Root<T> root, CriteriaBuilder builder, String criteriaKey, String criteriaStrValue) {
        Path<Double> criteriaKeyPath = root.get(criteriaKey);

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_EXISTS)) {
            return builder.isNotNull(criteriaKeyPath);
        }

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_DOES_NOT_EXISTS)) {
            return builder.isNull(criteriaKeyPath);
        }

        Double criteriaParsedValue = Double.valueOf(criteriaStrValue);

        switch (criteria.getOperation()) {
            case Constants.OP_GT:
                return builder.greaterThan(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_GTE:
                return builder.greaterThanOrEqualTo(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_LT:
                return builder.lessThan(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_LTE:
                return builder.lessThanOrEqualTo(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_DEQ:
            case Constants.OP_EQ:
            default:
                return builder.equal(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_NEQ:
            case Constants.OP_NWEQ:
                return builder.notEqual(criteriaKeyPath, criteriaParsedValue);
        }
    }

    protected Predicate getFloatPredicate(Root<T> root, CriteriaBuilder builder, String criteriaKey, String criteriaStrValue) {
        Path<Float> criteriaKeyPath = root.get(criteriaKey);

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_EXISTS)) {
            return builder.isNotNull(criteriaKeyPath);
        }

        if (criteria.getOperation().equalsIgnoreCase(Constants.OP_DOES_NOT_EXISTS)) {
            return builder.isNull(criteriaKeyPath);
        }

        Float criteriaParsedValue = Float.valueOf(criteriaStrValue);

        switch (criteria.getOperation()) {
            case Constants.OP_GT:
                return builder.greaterThan(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_GTE:
                return builder.greaterThanOrEqualTo(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_LT:
                return builder.lessThan(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_LTE:
                return builder.lessThanOrEqualTo(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_DEQ:
            case Constants.OP_EQ:
            default:
                return builder.equal(criteriaKeyPath, criteriaParsedValue);
            case Constants.OP_NEQ:
            case Constants.OP_NWEQ:
                return builder.notEqual(criteriaKeyPath, criteriaParsedValue);
        }
    }
}
