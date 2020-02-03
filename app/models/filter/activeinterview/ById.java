package models.filter.activeinterview;


public class ById extends ActiveInterviewFilter<Long> {

    @Override
    public String getAttributeName() {
        return "id";
    }

    @Override
    protected String toString(Long id) {
        if (id == null) {
            return null;
        }
        return String.valueOf(id);
    }

    @Override
    public Long fromString(String id) {
        try {
            return new Long(id);
        } catch (Exception e) {
            return null;
        }
    }
}
