package randomization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.models.randomization.ObjectGenerationRule;
import org.mvss.karta.framework.models.randomization.ObjectGenerationRuleType;
import org.mvss.karta.framework.models.randomization.Range;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;

public class TestObjectGenerationRule {
    public static boolean testVariableParam(Random random, ObjectGenerationRule vp, long iterations, boolean printObj) {
        HashMap<Serializable, Integer> objCount = new HashMap<>();

        for (int i = 0; i < iterations; i++) {
            Serializable nextObject = vp.generateNextValue(random);

            if (nextObject == null) {
                return false;
            }

            Integer currentCount = objCount.get(nextObject);

            if (currentCount == null) {
                currentCount = 0;
            }

            currentCount++;
            objCount.put(nextObject, currentCount);
        }

        for (Serializable obj : objCount.keySet()) {
            System.out.println((printObj ? "Obj: " + (obj.getClass().isArray() ? Arrays.toString((Object[]) obj) : obj.toString()) : "-> Object") + " frequency is " + objCount.get(obj) * 100.0 / iterations + "%");
        }
        return true;
    }

    public static void main(String[] args) {
        try {
            Random random = new Random();
            System.out.println("Demo random distribution for long");
            ObjectGenerationRule longVP = ObjectGenerationRule.builder().fieldName("LongVar").ruleType(ObjectGenerationRuleType.LONG_RANGE).range(new Range(900L, 1000L)).build();
            System.out.println(testVariableParam(random, longVP, 1000000, true));

            System.out.println("Demo random distribution for String");
            ObjectGenerationRule stringVP = ObjectGenerationRule.builder().fieldName("StringVar").ruleType(ObjectGenerationRuleType.STRING_RANGE).range(new Range(10, 20)).build();
            System.out.println(testVariableParam(random, stringVP, 10, true));

            System.out.println("Demo random distribution for String Values");
            ArrayList<ObjectGenerationRule> stringValueProbDist = new ArrayList<>();
            stringValueProbDist.add(ObjectGenerationRule.builder().probability(0.66f).values(new ArrayList<>(Arrays.asList("One", "First"))).build());
            stringValueProbDist.add(ObjectGenerationRule.builder().probability(0.22f).values(new ArrayList<>(Arrays.asList("Two", "Second"))).build());
            stringValueProbDist.add(ObjectGenerationRule.builder().probability(0.12f).values(new ArrayList<>(Arrays.asList("Three", "Third"))).build());

            ObjectGenerationRule stringValuesVP = ObjectGenerationRule.builder().fieldName("StringValuesVar").ruleType(ObjectGenerationRuleType.MUTEX_OBJECT_RULE_VALUE).fieldGenRules(stringValueProbDist).build();
            System.out.println(testVariableParam(random, stringValuesVP, 1000000, true));

            System.out.println("Demo random distribution for cricket scores");
            ArrayList<ObjectGenerationRule> cricketScoreProbDist = new ArrayList<>();
            cricketScoreProbDist.add(ObjectGenerationRule.builder().probability(0.05f).values(new ArrayList<>(List.of(0))).build());
            cricketScoreProbDist.add(ObjectGenerationRule.builder().probability(0.05f).ruleType(ObjectGenerationRuleType.INTEGER_RANGE).range(new Range(1, 30)).build());
            cricketScoreProbDist.add(ObjectGenerationRule.builder().probability(0.40f).ruleType(ObjectGenerationRuleType.INTEGER_RANGE).range(new Range(30, 70)).build());
            cricketScoreProbDist.add(ObjectGenerationRule.builder().probability(0.15f).ruleType(ObjectGenerationRuleType.INTEGER_RANGE).range(new Range(70, 96)).build());
            cricketScoreProbDist.add(ObjectGenerationRule.builder().probability(0.10f).values(new ArrayList<>(Arrays.asList(96, 97, 98, 99))).build());
            cricketScoreProbDist.add(ObjectGenerationRule.builder().probability(0.25f).ruleType(ObjectGenerationRuleType.INTEGER_RANGE).range(new Range(100, 151)).build());
            ObjectGenerationRule cricketScoreVP = ObjectGenerationRule.builder().fieldName("cricketScoreVar").ruleType(ObjectGenerationRuleType.MUTEX_OBJECT_RULE_VALUE).fieldGenRules(cricketScoreProbDist).build();
            System.out.println(testVariableParam(random, cricketScoreVP, 1000000, true));

            System.out.println("Demo random distribution for mutex variable in set");
            longVP.setProbability(0.60f);
            stringValuesVP.setProbability(0.40f);
            ObjectGenerationRule mutexVarSet = ObjectGenerationRule.builder().fieldName("mutexVarSet").ruleType(ObjectGenerationRuleType.MUTEX_OBJECT_RULE_VALUE).build().addFieldGenRules(longVP, stringValuesVP);
            System.out.println(testVariableParam(random, mutexVarSet, 1000000, true));

            System.out.println("Demo random distribution for variable set");
            mutexVarSet.setProbability(0.66f);
            cricketScoreVP.setProbability(0.66f);
            ObjectGenerationRule varSet = ObjectGenerationRule.builder().fieldName("varSet").ruleType(ObjectGenerationRuleType.OBJECT_RULE).build().addFieldGenRules(mutexVarSet, cricketScoreVP);
            System.out.println(testVariableParam(random, varSet, 1000000, true));

            ObjectMapper yamlParser = ParserUtils.getYamlObjectMapper();
            System.out.println("Demo random distribution for employee object");
            ObjectGenerationRule employeeObjFromFile = yamlParser.readValue(FileUtils.readFileToString(new File("SampleObjectGenerationRule.yaml"), Charset.defaultCharset()), ObjectGenerationRule.class);
            // System.out.println(testVariableParam( random, employeeObjFromFile, 100, true ));

            for (int i = 0; i < 200; i++) {
                Employee nextObject = yamlParser.convertValue(employeeObjFromFile.generateNextValue(random), Employee.class);
                System.out.println(nextObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
