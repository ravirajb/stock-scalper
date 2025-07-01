package com.egrub.scanner.model.nse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NseApiResponse {

    @JsonProperty("securityWiseDP")
    private SecurityWiseDP securityWiseDP;

    public SecurityWiseDP getSecurityWiseDP() {
        return securityWiseDP;
    }
}