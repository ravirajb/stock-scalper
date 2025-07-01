package com.egrub.scanner.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CustomDateDeserializer extends JsonDeserializer<String> {

    private static final DateTimeFormatter inputFormatter =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);  // Locale is crucial

    private static final DateTimeFormatter outputFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getText();  // e.g. "19-May-2025"
        LocalDate parsedDate = LocalDate.parse(dateString, inputFormatter);
        return parsedDate.format(outputFormatter);  // returns "2025-05-19"
    }

}
