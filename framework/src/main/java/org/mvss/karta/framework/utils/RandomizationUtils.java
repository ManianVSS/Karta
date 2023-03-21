package org.mvss.karta.framework.utils;

import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.utils.DataUtils;
import org.mvss.karta.framework.models.chaos.Chaos;
import org.mvss.karta.framework.models.randomization.ObjectWithChance;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Utility class for chance/probability based selection of objects
 *
 * @author Manian
 */
@SuppressWarnings("unused")
public class RandomizationUtils {
    /**
     * Check if the collection of objects with chance cover all possibilities (1.0f)
     */
    public static <E extends ObjectWithChance> boolean checkForProbabilityCoverage(Collection<E> chanceObjects) {
        return checkForProbabilityCoverage(chanceObjects, 1.0f);
    }

    /**
     * Check if the collection of objects with chance cover specified probability value.
     */
    public static <E extends ObjectWithChance> boolean checkForProbabilityCoverage(Collection<E> chanceObjects, float probabilityToCover) {
        return getMissingProbabilityCoverage(chanceObjects, probabilityToCover) == 0;
    }

    /**
     * Evaluates the missing probability(assuming total to be 1.0f) from a collection of objects with chance.
     */
    public static <E extends ObjectWithChance> Float getMissingProbabilityCoverage(Collection<E> chanceObjects) {
        return getMissingProbabilityCoverage(chanceObjects, 1);
    }

    /**
     * Evaluates the missing probability from a collection of objects with chance for the probability to cover.
     * Returns null for over shooting
     */
    public static <E extends ObjectWithChance> Float getMissingProbabilityCoverage(Collection<E> chanceObjects, float probabilityToCover) {
        return getMissingProbabilityCoverage(chanceObjects, probabilityToCover, false);
    }

    /**
     * Evaluates the missing probability from a collection of objects with chance for the probability to cover.
     * ignoreOverflow if set to true won't return null on over shooting probability to cover.
     */
    public static <E extends ObjectWithChance> Float getMissingProbabilityCoverage(Collection<E> chanceObjects, float probabilityToCover,
                                                                                   boolean ignoreOverflow) {
        float probabilityNotCovered = probabilityToCover;
        for (ObjectWithChance chanceObject : chanceObjects) {
            float probability = chanceObject.getProbability();

            if (!DataUtils.inRange(probability, 0, probabilityToCover, false, true)) {
                return null;
            }

            probabilityNotCovered = probabilityNotCovered - chanceObject.getProbability();

            if (!ignoreOverflow && (probabilityNotCovered < 0)) {
                return null;
            }
        }

        return (float) (((long) (probabilityNotCovered * 1000000.f)) / 1000000L);
    }

    /**
     * Compose a ArrayList of objects selected from a collection of objects with individual probability of occurrences.
     */
    public static <E extends ObjectWithChance> ArrayList<E> generateNextComposition(Random random, Collection<E> objectsToChooseFrom) {
        ArrayList<E> chosenObjects = new ArrayList<>();

        if ((objectsToChooseFrom == null) || (objectsToChooseFrom.isEmpty())) {
            return chosenObjects;
        }

        for (E object : objectsToChooseFrom) {
            float probability = object.getProbability();

            if (!DataUtils.inRange(probability, 0, 1)) {
                continue;
            }

            float randomChance = (1 + random.nextInt(1000000)) / 1000000.0f;

            if ((probability == 1) || (randomChance <= probability)) {
                chosenObjects.add(object);
            }
        }

        return chosenObjects;
    }

    /**
     * Select one object from a collection of objects with chance taking into consideration their probability of occurrence.
     * The sum of probabilities for the objects with chance should be 1.0f else null is returned.
     */
    public static <E extends ObjectWithChance> E generateNextMutexComposition(Random random, Collection<E> objectsToChooseFrom) {
        if ((objectsToChooseFrom == null) || (objectsToChooseFrom.isEmpty())) {
            return null;
        }

        // TODO: Check for consistency in probability
        float probabilityCovered = 0;
        float randomChance = (1 + random.nextInt(1000000)) / 1000000.0f;

        boolean alreadyPicked = false;
        E returnValue = null;

        for (E object : objectsToChooseFrom) {
            float probability = object.getProbability();

            if (!DataUtils.inRange(probability, 0, 1)) {
                return null;
            }

            if (!alreadyPicked && DataUtils.inRange(randomChance, probabilityCovered, probabilityCovered + probability)) {
                returnValue = object;
                alreadyPicked = true;
            }

            probabilityCovered += probability;
        }

        if (!(Math.round(probabilityCovered) == 1.0f)) {
            return null;
        }

        return returnValue;
    }

    /**
     * Randomly discard few items by count from list using the provided randomizer.
     */
    public static <T> ArrayList<T> discardListItems(Random random, Collection<T> items, int count) {
        ArrayList<T> returnList = null;

        if (items != null) {
            if (count >= items.size()) {
                returnList = new ArrayList<>();
            } else {
                returnList = new ArrayList<>(items);
                if (count > 0) {
                    for (int discardedItems = 0; discardedItems < count; discardedItems++) {
                        returnList.remove(random.nextInt(returnList.size()));
                    }
                }
            }
        }

        return returnList;
    }

    /**
     * Randomly select few items by count from list using the provided randomizer.
     */
    public static <T> ArrayList<T> selectListItems(Random random, Collection<T> items, int count) {
        ArrayList<T> returnList = null;

        if (items != null) {
            if (count <= 0) {
                returnList = new ArrayList<>();
            } else {
                returnList = new ArrayList<>(items);

                if (count < items.size()) {
                    ArrayList<T> selectedList = new ArrayList<>();
                    for (int selected = 0; selected < count; selected++) {
                        selectedList.add(returnList.remove(random.nextInt(returnList.size())));
                    }
                    returnList = selectedList;
                }
            }
        }

        return returnList;
    }

    /**
     * Randomly select few items by percentage from list using the provided randomizer.
     */
    public static <T> ArrayList<T> selectByPercentage(Random random, Collection<T> items, float percentage) {
        if (items == null) {
            return null;
        }

        int selectCount = (int) Math.ceil(items.size() * percentage / 100);
        return selectListItems(random, items, selectCount);
    }

    /**
     * Randomly select few items by max percentage(0-maxPercentage selected randomly) from list using the provided randomizer.
     */
    public static <T> ArrayList<T> selectByMaxPercentage(Random random, Collection<T> items, float maxPercentage) {
        if (items == null) {
            return null;
        }

        float percentage = maxPercentage * ((1 + random.nextInt(1000000)) / 1000000.0f);

        int selectCount = (int) Math.ceil(items.size() * percentage / 100);
        return selectListItems(random, items, selectCount);
    }

    /**
     * Randomly select few items by the chaos level and unit using the provided randomizer.
     */
    public static <T> Collection<T> selectByChaos(Random random, Collection<T> items, Chaos chaos) {
        if ((items == null) || items.isEmpty()) {
            return items;
        }

        switch (chaos.getChaosUnit()) {
            case CONSTANT:
                return selectListItems(random, items, (int) Math.ceil(chaos.getChaosLevel()));
            case ALL_BUT_ONE:
                return discardListItems(random, items, 1);
            case MAX_PERCENTAGE:
                return selectByMaxPercentage(random, items, chaos.getChaosLevel());
            case PERCENTAGE:
                return selectByPercentage(random, items, chaos.getChaosLevel());
            default:
                return items;
        }
    }

    /**
     * Randomly generate an alphanumeric string of specified length.
     */
    public static String randomAlphaNumericString(Random random, int length) {
        if (random == null) {
            random = new Random();
        }

        StringBuilder returnValue = new StringBuilder(Constants.EMPTY_STRING);

        for (int i = 0; i < length; i++) {
            int randomDigit = random.nextInt(62);

            if (randomDigit < 10) {
                returnValue.append((char) ('0' + randomDigit));
            } else if (randomDigit < 36) {
                returnValue.append((char) ('A' + (randomDigit - 10)));
            } else {
                returnValue.append((char) ('a' + (randomDigit - 36)));
            }

        }
        return returnValue.toString();
    }

    public static void createRandomFile(Random random, String fileName, long fileSize, int BLOCK_SIZE) throws IOException {
        try (OutputStream os = Files.newOutputStream(Paths.get(fileName))) {
            long bytesToWrite = fileSize;

            byte[] writeBuffer = new byte[BLOCK_SIZE];

            while (bytesToWrite >= BLOCK_SIZE) {
                random.nextBytes(writeBuffer);
                os.write(writeBuffer);
                bytesToWrite -= BLOCK_SIZE;
            }
            if (bytesToWrite > 0) {
                writeBuffer = new byte[(int) bytesToWrite];
                random.nextBytes(writeBuffer);
                os.write(writeBuffer);
            }
        }
    }
}
