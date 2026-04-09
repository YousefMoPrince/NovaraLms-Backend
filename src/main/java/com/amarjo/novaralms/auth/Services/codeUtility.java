package com.amarjo.novaralms.auth.Services;

public class codeUtility {
    public static String generateUserCode(String role) {
        String prefix;
        if ("instructor".equalsIgnoreCase(role)) {
            prefix = "INS-";
        } else if ("student".equalsIgnoreCase(role)) {
            prefix = "STD-";
        } else {
            throw new IllegalArgumentException("Invalid role");
        }

        String uniquePart = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        return prefix + uniquePart;}
}
