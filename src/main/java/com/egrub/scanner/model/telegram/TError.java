package com.egrub.scanner.model.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TError {
    private boolean ok;
    @JsonProperty("error_code")
    private int errorCode;
    private String description;
    private Parameters parameters;

    @Data
    public static class Parameters {
        @JsonProperty("retry_after")
        private int retryAfter;
    }
}
