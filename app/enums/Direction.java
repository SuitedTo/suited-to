package enums;

public enum Direction {

    ASC, DESC;



    public static Direction fromStringIgnoreCase(String value){
        if(value == null){
            throw new IllegalArgumentException("value cannot be null");
        }
        return Direction.valueOf(value.toUpperCase());
    }
}
