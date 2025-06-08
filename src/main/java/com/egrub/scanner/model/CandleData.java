package com.egrub.scanner.model;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class CandleData {
    private String timestamp;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
    private Long openInteretest;

    public static CandleData fromList(List<Object> candleList) {
        CandleData candleData = new CandleData();
        if (candleList.size() >= 7) {
            candleData.timestamp = candleList.get(0).toString();
            candleData.open = Double.valueOf(candleList.get(1).toString());
            candleData.high = Double.valueOf(candleList.get(2).toString());
            candleData.low = Double.valueOf(candleList.get(3).toString());
            candleData.close = Double.valueOf(candleList.get(4).toString());
            candleData.volume = Long.valueOf(candleList.get(5).toString());
            candleData.openInteretest = Long.valueOf(candleList.get(6).toString());
        }
        return candleData;
    }
}
