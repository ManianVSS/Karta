package org.mvss.karta.framework.models.randomization;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.utils.RandomizationUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

/**
 * This class describes an object generation rule.
 * Sub objects can be nested by nesting object generation rule.
 * Objects generated are either value types(int, float, String, boolean) or HashMap&lt; String, Serializable &gt;
 * A serializable object with serializable fields can be expressed as a HashMap&lt; String, Serializable &gt;
 * Serialization/deserialization libraries usually represent generic objects in a similar fashion and can handle nested expansions
 *
 * @author Manian
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectGenerationRule implements Serializable, ObjectWithChance {
    private static final long serialVersionUID = 1L;

    private static final long STRING_LENGTH_RANGE_DEFAULT_MAX = 100;
    private static final long BINARY_DATA_RANGE_DEFAULT_MAX = 1024;

    /**
     * Name of a field in the parent object for which this acts as the generation rule.
     */
    @Builder.Default
    private String fieldName = null;

    /**
     * The rule type indicates what kind of object is to be generated.
     * For nested generation rule, "ObjectGenerationRuleType.OBJECT_RULE" can be used.
     * For static generic objects, "ObjectGenerationRuleType.VALUES" can be used. etc
     */
    @Builder.Default
    private ObjectGenerationRuleType ruleType = ObjectGenerationRuleType.VALUES;

    /**
     * The generic range to pick/generate an object with a range characteristic.
     * If rule type is INTEGER_RANGE, a random integer is picked from ranged.
     * If rule type is STRING_RANGE, a random string is generated with the length subjected to this range.
     */
    @Builder.Default
    private Range range = null;

    /**
     * The list of possible values(generic serializable objects) if object generation rule is "ObjectGenerationRuleType.VALUES"
     */
    @Builder.Default
    private ArrayList<Serializable> values = null;

    /**
     * The probability that this rule is selected for generating a sub object for the parent object.
     * Only applies if rule type is OBJECT_RULE or MUTEX_OBJECT_RULE_VALUE.
     */
    @Builder.Default
    private float probability = 1.0f;

    /**
     * The object generation rules for sub-objects(fields to be generated by object rules).
     * The actual fields included are subject to the probability expressed in these rules and if generating a mutually exclusive field.
     * If using MUTEX_OBJECT_RULE_VALUE, the sum of probability of all object rules in this list should be 1.0
     */
    @Builder.Default
    private ArrayList<ObjectGenerationRule> fieldGenRules = null;

    /**
     * Returns true if there are nested objects
     *
     * @return boolean
     */
    public boolean checkIfNestedRule() {
        return (ruleType == ObjectGenerationRuleType.OBJECT_RULE) || (ruleType == ObjectGenerationRuleType.MUTEX_OBJECT_RULE_VALUE);
    }

    /**
     * Returns true if the object generation rule is valid.
     *
     * @return boolean
     */
    public boolean validateConfiguration() {
        if ((ruleType == ObjectGenerationRuleType.OBJECT_RULE) || (ruleType == ObjectGenerationRuleType.MUTEX_OBJECT_RULE_VALUE)) {
            if ((fieldGenRules == null) || (fieldGenRules.isEmpty())) {
                return false;
            }

            for (ObjectGenerationRule variableField : fieldGenRules) {
                // Variable names can't be blank for variable object
                if ((variableField.ruleType == ObjectGenerationRuleType.OBJECT_RULE) && StringUtils.isBlank(fieldName)) {
                    return false;
                }

                if (!variableField.validateConfiguration()) {
                    return false;
                }
            }

            return ruleType != ObjectGenerationRuleType.MUTEX_OBJECT_RULE_VALUE || RandomizationUtils.checkForProbabilityCoverage(fieldGenRules);
        }

        return true;
    }

    /**
     * Add field generation rules
     *
     * @param fieldGenRulesToAdd ObjectGenerationRule[]
     * @return ObjectGenerationRule
     */
    public ObjectGenerationRule addFieldGenRules(ObjectGenerationRule... fieldGenRulesToAdd) {
        if (fieldGenRules == null) {
            fieldGenRules = new ArrayList<>();
        }

        for (ObjectGenerationRule fieldGenRule : fieldGenRulesToAdd) {
            if ((fieldGenRule != null) && !fieldGenRules.contains(fieldGenRule)) {
                fieldGenRules.add(fieldGenRule);
            }
        }

        return this;
    }

    /**
     * Generate the next object value based on this object generation rule.
     * Nested field generations rules are evaluated for inclusion and generateNextValue is called on them to generate field value.
     *
     * @param random Random
     * @return Serializable
     */
    public Serializable generateNextValue(Random random) {
        try {
            switch (ruleType) {
                case OBJECT_RULE:
                    if (fieldGenRules == null) {
                        return null;
                    }

                    HashMap<String, Serializable> variableMap = new HashMap<>();
                    ArrayList<ObjectGenerationRule> selectedVariables = RandomizationUtils.generateNextComposition(random, fieldGenRules);
                    for (ObjectGenerationRule variableParam : selectedVariables) {
                        variableMap.put(variableParam.fieldName, variableParam.generateNextValue(random));
                    }

                    return variableMap;

                case MUTEX_OBJECT_RULE_VALUE:
                    ObjectGenerationRule variableParam = RandomizationUtils.generateNextMutexComposition(random, fieldGenRules);
                    return (variableParam == null) ? null : variableParam.generateNextValue(random);

                case VALUES:
                    if ((values == null) || values.isEmpty()) {
                        return null;
                    }
                    if (values.size() == 1) {
                        return values.get(0);
                    } else {
                        int randomIndex = random.nextInt(values.size());
                        return values.get(randomIndex);
                    }

                case UUID:
                    return UUID.randomUUID();

                case BOOLEAN:
                    return random.nextBoolean();

                case INTEGER_RANGE:
                    if (range == null) {
                        range = new Range(0, Integer.MAX_VALUE);
                    }
                    return (int) range.getNext(random);

                case LONG_RANGE:
                    if (range == null) {
                        range = new Range();
                    }
                    return range.getNext(random);

                case STRING_RANGE:
                    if (range == null) {
                        range = new Range(1, STRING_LENGTH_RANGE_DEFAULT_MAX);
                    }
                    return RandomizationUtils.randomAlphaNumericString(random, (int) range.getNext(random));

                case BINARY_DATA_RANGE:
                    if (range == null) {
                        range = new Range(1, BINARY_DATA_RANGE_DEFAULT_MAX);
                    }
                    int length = (int) range.getNext(random);

                    if (length == 0) {
                        return null;
                    }

                    byte[] toReturn = new byte[length];
                    random.nextBytes(toReturn);
                    return toReturn;

                default:
                    return null;
            }
        } catch (Throwable t) {
            return null;
        }
    }
}
