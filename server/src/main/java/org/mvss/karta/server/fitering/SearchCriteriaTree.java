package org.mvss.karta.server.fitering;

import lombok.*;
import org.mvss.karta.server.models.BaseModel;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SearchCriteriaTree extends SearchCriteria {

    private static final long serialVersionUID = 1L;

    private FilterGroupConditionType conditionType = FilterGroupConditionType.SINGLE;

    private ArrayList<SearchCriteriaTree> searchCriteriaGroupList;

    public SearchCriteriaTree(String key, String operator, Serializable value) {
        super(key, operator, value);
    }

    public SearchCriteriaTree(FilterGroupConditionType conditionType, SearchCriteriaTree... subtrees) {
        this.conditionType = conditionType;
        this.searchCriteriaGroupList = new ArrayList<>(List.of(subtrees));
    }

    public SearchCriteriaTree add(SearchCriteriaTree... subtrees) {
        if ((subtrees != null) && (subtrees.length > 0)) {
            if (this.searchCriteriaGroupList == null) {
                this.searchCriteriaGroupList = new ArrayList<>();
            }
            this.searchCriteriaGroupList.addAll(List.of(subtrees));
        }
        return this;
    }

    @Builder(builderMethodName = "groupBuilder")
    public SearchCriteriaTree(String key, String operation, Serializable value, FilterGroupConditionType conditionType,
                              ArrayList<SearchCriteriaTree> searchCriteriaGroupList) {
        super(key, operation, value);
        this.conditionType = conditionType;
        this.searchCriteriaGroupList = searchCriteriaGroupList;
    }

    public <T> Specification<T> buildSpecification(Map<Class<? extends BaseModel>, JpaRepository<? extends BaseModel, Long>> entityFetcherMap) {
        Specification<T> result = null;
        ArrayList<Specification<T>> specs = new ArrayList<>();

        if (conditionType == null) {
            conditionType = FilterGroupConditionType.SINGLE;
        }

        if ((searchCriteriaGroupList == null) || searchCriteriaGroupList.isEmpty()) {
            return new DefaultSpecification<>(this, entityFetcherMap);
        } else {
            searchCriteriaGroupList.forEach((searchCriteriaGroup) -> specs.add(searchCriteriaGroup.buildSpecification(entityFetcherMap)));
        }

        switch (conditionType) {
            case AND:
                for (Specification<T> spec : specs) {
                    result = Specification.where(result).and(spec);
                }
                break;

            case OR:
                for (Specification<T> spec : specs) {
                    result = Specification.where(result).or(spec);
                }
                break;

            case SINGLE:
            default:
                result = new DefaultSpecification<>(this, entityFetcherMap);
                break;

        }

        return result;
    }
}
