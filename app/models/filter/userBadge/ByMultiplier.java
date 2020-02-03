package models.filter.userBadge;


public class ByMultiplier extends UserBadgeFilter<Integer> {
    @Override
    public String getAttributeName() {
        return "multiplier";
    }

    @Override
    protected String toString(Integer multiplier) {
        if (multiplier == null) {
            return null;
        }

        return multiplier.toString();
    }

    @Override
    public Integer fromString(String multiplier) {
        try {
            return Integer.parseInt(multiplier);
        } catch (Exception e) {
            return null;
        }
    }
}
