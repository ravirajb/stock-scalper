package com.egrub.scanner.model.nse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class SecurityInfo {
    private String boardStatus;
    private String tradingStatus;
    private String tradingSegment;
    private String sessionNo;
    private String slb;
    private String classOfShare;
    private String derivatives;
    private Surveillance surveillance;
    private int faceValue;
    private long issuedSize;
}

