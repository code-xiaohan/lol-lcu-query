package org.lele.lollcuquery.support;

import com.fasterxml.jackson.databind.JsonNode;

public final class Jsons {

    private Jsons() {
    }

    public static String text(JsonNode node, String field) {
        if (node == null) {
            return "";
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }

    public static int intVal(JsonNode node, String field) {
        if (node == null) {
            return 0;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? 0 : value.asInt();
    }

    public static long longVal(JsonNode node, String field) {
        if (node == null) {
            return 0L;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? 0L : value.asLong();
    }

    public static boolean boolVal(JsonNode node, String field) {
        if (node == null) {
            return false;
        }
        JsonNode value = node.get(field);
        return value != null && !value.isNull() && value.asBoolean();
    }

    public static String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
