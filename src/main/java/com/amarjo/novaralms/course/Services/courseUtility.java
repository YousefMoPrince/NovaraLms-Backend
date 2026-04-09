package com.amarjo.novaralms.course.Services;

public class courseUtility {

    public static String generateCourseCode(String department) {
        String prefix = department.toUpperCase();
        String uniquePart = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + "-" + uniquePart;

    }
}