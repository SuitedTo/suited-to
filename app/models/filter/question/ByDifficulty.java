package models.filter.question;

import enums.Difficulty;

/**
 * Filter questions by difficulty.
 *
 * @author joel
 */
public class ByDifficulty extends QuestionFilter<Difficulty> {


    public static ByDifficulty Easy() {
        ByDifficulty instance = new ByDifficulty();
        instance.include(Difficulty.EASY.name());
        return instance;
    }

    public static ByDifficulty Medium() {
        ByDifficulty instance = new ByDifficulty();
        instance.include(Difficulty.MEDIUM.name());
        return instance;
    }

    public static ByDifficulty Hard() {
        ByDifficulty instance = new ByDifficulty();
        instance.include(Difficulty.HARD.name());
        return instance;
    }

    @Override
    public String getAttributeName() {
        return "difficulty";
    }

    @Override
    protected String toString(Difficulty difficulty) {
        if (difficulty == null) {
            return null;
        }
        return difficulty.name();
    }

    @Override
    public Difficulty fromString(String difficulty) {
        try {
            return Difficulty.valueOf(difficulty.toUpperCase());
        } catch (Exception e) {
        }

        return null;
    }

}
