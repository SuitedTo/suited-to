package enums.prep;

public enum VideoStatus {
	UNAVAILABLE, PENDING, AVAILABLE;

    @Override
    public String toString() {
        return name();
    }
}
