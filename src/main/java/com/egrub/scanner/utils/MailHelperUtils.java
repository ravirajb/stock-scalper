package com.egrub.scanner.utils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;

public class MailHelperUtils {

    public static String generateHtmlFromPojo(Object pojo) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("    <meta charset='UTF-8'>\n")
                .append("    <title>Report</title>\n")
                .append("    <style>\n")
                .append("        body { font-family: Arial, sans-serif; margin: 20px; }\n")
                .append("        .header { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n")
                .append("        .section { margin-bottom: 30px; }\n")
                .append("        .section-title { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 5px; margin-bottom: 15px; }\n")
                .append("        table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }\n")
                .append("        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n")
                .append("        th { background-color: #f2f2f2; font-weight: bold; }\n")
                .append("        tr:nth-child(even) { background-color: #f9f9f9; }\n")
                .append("        .single-field { background-color: #e9f7ef; padding: 10px; border-radius: 3px; margin-bottom: 10px; }\n")
                .append("        .field-label { font-weight: bold; color: #2c3e50; }\n")
                .append("        .field-value { color: #34495e; }\n")
                .append("    </style>\n")
                .append("</head>\n")
                .append("<body>\n");

        // Add header with timestamp
        html.append("    <div class='header'>\n")
                .append("        <h1>Generated Report</h1>\n")
                .append("        <p>Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>\n")
                .append("    </div>\n");

        // Process the POJO
        processObject(pojo, html, pojo.getClass().getSimpleName());

        html.append("</body>\n")
                .append("</html>");

        return html.toString();
    }

    /**
     * Process an object and generate HTML tables for collections and single fields for other properties
     */
    private static void processObject(Object obj, StringBuilder html, String sectionTitle) {
        if (obj == null) return;

        html.append("    <div class='section'>\n")
                .append("        <h2 class='section-title'>").append(sectionTitle).append("</h2>\n");

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        // Separate collections from single fields
        StringBuilder singleFieldsHtml = new StringBuilder();
        StringBuilder tablesHtml = new StringBuilder();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(obj);
                String fieldName = field.getName();

                if (fieldValue == null) {
                    continue;
                }

                // Check if field is a Collection or Array
                if (fieldValue instanceof Collection<?> || fieldValue.getClass().isArray()) {
                    generateTableFromCollection(fieldName, fieldValue, tablesHtml);
                } else {
                    // Single field
                    singleFieldsHtml.append("        <div class='single-field'>\n")
                            .append("            <span class='field-label'>").append(capitalizeFirst(fieldName)).append(":</span> ")
                            .append("            <span class='field-value'>").append(escapeHtml(fieldValue.toString())).append("</span>\n")
                            .append("        </div>\n");
                }
            } catch (IllegalAccessException e) {
                // Skip inaccessible fields
            }
        }

        // Add single fields first, then tables
        html.append(singleFieldsHtml);
        html.append(tablesHtml);
        html.append("    </div>\n");
    }

    /**
     * Generate HTML table from a collection or array
     */
    private static void generateTableFromCollection(String fieldName, Object collection, StringBuilder html) {
        html.append("        <h3>").append(capitalizeFirst(fieldName)).append("</h3>\n");

        Collection<?> items;
        if (collection.getClass().isArray()) {
            items = Arrays.asList((Object[]) collection);
        } else {
            items = (Collection<?>) collection;
        }

        if (items.isEmpty()) {
            html.append("        <p><em>No items found</em></p>\n");
            return;
        }

        Object firstItem = items.iterator().next();

        // If collection contains primitive types or simple objects
        if (isPrimitiveOrWrapper(firstItem.getClass()) || firstItem instanceof String) {
            generateSimpleTable(items, html);
        } else {
            // Collection contains complex objects
            generateComplexTable(items, html);
        }
    }

    /**
     * Generate table for simple values (primitives, strings)
     */
    private static void generateSimpleTable(Collection<?> items, StringBuilder html) {
        html.append("        <table>\n")
                .append("            <thead>\n")
                .append("                <tr><th>Value</th></tr>\n")
                .append("            </thead>\n")
                .append("            <tbody>\n");

        for (Object item : items) {
            html.append("                <tr>\n")
                    .append("                    <td>").append(escapeHtml(item.toString())).append("</td>\n")
                    .append("                </tr>\n");
        }

        html.append("            </tbody>\n")
                .append("        </table>\n");
    }

    /**
     * Generate table for complex objects
     */
    private static void generateComplexTable(Collection<?> items, StringBuilder html) {
        Object firstItem = items.iterator().next();
        Field[] fields = firstItem.getClass().getDeclaredFields();

        // Table header
        html.append("        <table>\n")
                .append("            <thead>\n")
                .append("                <tr>\n");

        for (Field field : fields) {
            html.append("                    <th>").append(capitalizeFirst(field.getName())).append("</th>\n");
        }

        html.append("                </tr>\n")
                .append("            </thead>\n")
                .append("            <tbody>\n");

        // Table rows
        for (Object item : items) {
            html.append("                <tr>\n");

            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(item);
                    String displayValue = fieldValue != null ? fieldValue.toString() : "";
                    html.append("                    <td>").append(escapeHtml(displayValue)).append("</td>\n");
                } catch (IllegalAccessException e) {
                    html.append("                    <td>N/A</td>\n");
                }
            }

            html.append("                </tr>\n");
        }

        html.append("            </tbody>\n")
                .append("        </table>\n");
    }

    /**
     * Check if a class is a primitive or wrapper type
     */
    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class || clazz == Integer.class || clazz == Character.class ||
                clazz == Byte.class || clazz == Short.class || clazz == Double.class ||
                clazz == Long.class || clazz == Float.class;
    }

    /**
     * Capitalize first letter of a string
     */
    private static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Escape HTML special characters
     */
    private static String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
