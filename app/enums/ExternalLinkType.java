package enums;

import play.i18n.Messages;

public enum ExternalLinkType {
    FACEBOOK("https://www.facebook.com/"), LINKEDIN("http://www.linkedin.com/in/"), TWITTER("http://twitter.com/"),
    G_PLUS("https://plus.google.com/"), OTHER("http://");
    
    public String urlPrefix;

    private ExternalLinkType(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    @Override
    public String toString() {
        return Messages.get(getClass().getName() + "." + name());
    }
}
