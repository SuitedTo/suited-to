package enums;

/**
 *  Images sizes (in pixels) for our scaling helpers
 */
public enum ImageSize {
    THUMBNAIL(100),
    PROFILE(200);

    private Integer size;

    private ImageSize(Integer size) {
        this.size = size;
    }

    public Integer toInteger() {
        return size;
    }
}
