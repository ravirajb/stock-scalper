package com.egrub.scanner.model.nse;

import com.egrub.scanner.utils.DoubleNaNDeserializer;
import com.egrub.scanner.utils.StringOrArrayToListDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {
    private String series;
    private String symbol;
    private String isin;
    private String status;
    private String listingDate;
    private String industry;
    private String lastUpdateTime;

    @JsonDeserialize(using = DoubleNaNDeserializer.class)
    private Double pdSectorPe;
    @JsonDeserialize(using = DoubleNaNDeserializer.class)

    private Double pdSymbolPe;
    private String pdSectorInd;
    @JsonDeserialize(using = StringOrArrayToListDeserializer.class)
    private List<String> pdSectorIndAll;
}
