package com.egrub.scanner.model.upstox;

import com.upstox.feeder.MarketUpdateV3.Feed;
import com.upstox.feeder.MarketUpdateV3.MarketInfo;
import com.upstox.feeder.MarketUpdateV3.Type;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@Document("market_feed")
public class FullD30 {
    private String symbol;
    private Date ts;
    private MarketInfo marketInfo;
    private Feed feed;
    private Type type;

}
