package com.egrub.scanner.model.upstox;

import lombok.Data;

import java.util.Date;

@Data
public class MomentumSignal {
    Date bucketTime;
    double bidAskRatio;
    boolean possibleIceberg;
    String prediction; // "RISE", "FALL", "NEUTRAL"
}
