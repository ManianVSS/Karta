package org.mvss.karta.dependencyinjection.utils;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    /**
     * Replace all the tokens in an input using the algorithm provided for each
     *
     * @param original     original string
     * @param tokenPattern the pattern to match with
     * @param converter    the conversion to apply
     * @return the substituted string
     */
    public static String replaceTokens(String original, Pattern tokenPattern,
                                       Function<Matcher, String> converter) {
        int lastIndex = 0;
        StringBuilder output = new StringBuilder();
        Matcher matcher = tokenPattern.matcher(original);
        while (matcher.find()) {
            output.append(original, lastIndex, matcher.start())
                    .append(converter.apply(matcher));

            lastIndex = matcher.end();
        }
        if (lastIndex < original.length()) {
            output.append(original, lastIndex, original.length());
        }
        return output.toString();
    }

    public static String recursiveReplaceTokens(String original, Pattern tokenPattern,
                                                Function<Matcher, String> converter) {

        String currentReplacement = original;
        while (true) {
            String replaced = replaceTokens(currentReplacement, tokenPattern, converter);
            if (replaced.equals(currentReplacement)) {
                return replaced;
            } else {
                currentReplacement = replaced;
            }
        }
    }

    public static final Pattern variablePattern = Pattern.compile("\\$\\{(?<var>[A-Za-z0-9-_]+)}");

    public static String replaceVariables(Map<String, Object> variableMap, String toReplaceString) {
        return replaceVariables(variableMap::get, toReplaceString);
    }

    public static String replaceVariables(Function<String, Object> getVariableFunction, String toReplaceString) {
        return recursiveReplaceTokens(toReplaceString, variablePattern, match -> {
                    Object objectToReplaceWith = getVariableFunction.apply(match.group("var"));//variableMap.get();
                    return (objectToReplaceWith == null) ? "null" : objectToReplaceWith.toString();
                }
        );
    }

    public static final Pattern longLiteralPattern = Pattern.compile("^[+-]?[0-9]+$");
    public static final Pattern doubleLiteralPattern = Pattern.compile("^[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)$");
//    public static final Pattern objectLiteralPattern = Pattern.compile("^([{].*[}]) | (\\[.*\\])$");

    public static boolean isNumeric(String valString) {
        return longLiteralPattern.matcher(valString).find() || doubleLiteralPattern.matcher(valString).find();
    }

    public static Serializable getValue(String valString) {
        if (longLiteralPattern.matcher(valString).find()) {
            return Long.parseLong(valString);
        } else if (doubleLiteralPattern.matcher(valString).find()) {
            return Double.parseDouble(valString);
        } else {
            return valString;
        }
    }
}
