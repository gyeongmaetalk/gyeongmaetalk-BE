package auctionTalk.auction.config.security.auth;

public enum ClientEnv {
    DEV,
    PREVIEW,
    PROD;

    public static ClientEnv fromOrDefault(String value, ClientEnv defaultEnv) {
        if (value == null || value.isBlank()) {
            return defaultEnv;
        }

        try {
            return ClientEnv.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultEnv;
        }
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            ClientEnv.valueOf(value.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}