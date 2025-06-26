package com.egrub.scanner.service;

import com.egrub.scanner.model.upstox.FullD30;
import com.upstox.feeder.MarketUpdateV3.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Log4j2
public class ValidatorService {

    private final MongoTemplate mongoTemplate;

    public ValidatorService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void validate() {
        log.info("In Validate");

        Query query = new Query();
        query.addCriteria(Criteria.where("symbol").is("RELIANCE"));
        List<FullD30> fullD30List = mongoTemplate.find(query, FullD30.class, "market_feed");

        log.info(fullD30List.size());

        Map<Long, List<FullD30>> bucketMap = new TreeMap<>();

        for (FullD30 tick : fullD30List) {
            long bucket = tick.getTs().getTime() / 30_000 * 30_000;
            bucketMap.computeIfAbsent(bucket, k -> new ArrayList<>()).add(tick);
        }

        for (Map.Entry<Long, List<FullD30>> entry : bucketMap.entrySet()) {
            long bucketTs = entry.getKey();
            List<FullD30> ticks = entry.getValue();

            long totalBidQty = 0;
            long totalAskQty = 0;

            long icebergBid = 0;
            long icebergAsk = 0;


            Map<Double, Long> bidCluster = new HashMap<>();
            Map<Double, Long> askCluster = new HashMap<>();
            Set<String> symbols = new HashSet<>();

            for (FullD30 tick : ticks) {
                symbols.add(tick.getSymbol());
                List<Quote> quotes = Optional.ofNullable(tick.getFeed())
                        .map(Feed::getFullFeed)
                        .map(FullFeed::getMarketFF)
                        .map(MarketFullFeed::getMarketLevel)
                        .map(MarketLevel::getBidAskQuote)
                        .orElse(Collections.emptyList());

                for (Quote q : quotes) {
                    totalBidQty += q.getBidQ();
                    totalAskQty += q.getAskQ();

                    bidCluster.merge(q.getBidP(), q.getBidQ(), Long::sum);
                    askCluster.merge(q.getAskP(), q.getAskQ(), Long::sum);
                }
            }

            double bidAskRatio = totalAskQty == 0 ? 0.0 :
                    (double) totalBidQty / totalAskQty;

            icebergBid = bidCluster
                    .values()
                    .stream()
                    .filter(qty -> qty > 3000)
                    .count();
            icebergAsk = askCluster
                    .values()
                    .stream()
                    .filter(qty -> qty > 3000)
                    .count();

            LocalDateTime ldt = Instant.ofEpochMilli(bucketTs)
                    .atZone(ZoneId.of("Asia/Kolkata"))
                    .toLocalDateTime();

            if (icebergAsk > 0 && bidAskRatio > 2) {
                log.info("INSTITUTIONAL BUYING (iceberg on ASK) ldt:{}", ldt);
            } else if (icebergBid > 0 && bidAskRatio < 0.5) {
                log.info("INSTITUTIONAL SELLING (iceberg on BID) ldt:{}", ldt);
            } else if (icebergBid > 0 && icebergAsk > 0) {
                log.info("BOTH SIDES ICEBERG – CONSOLIDATION ldt:{}", ldt);
            } else {
                log.info("RETAIL PRESSURE or WEAK ICEBERG ldt:{}", ldt);
            }
/*
            String prediction = "NEUTRAL";
            if (bidAskRatio > 1.5 && !iceberg) prediction = "POSSIBLE RISE";
            else if (bidAskRatio < 0.8 && iceberg) prediction = "POSSIBLE FALL";



            // 🚨 ALERT OUTPUT
            log.info("{} | Symbols: {} | Bid/Ask Ratio: {} | Iceberg: {} | ⚡ {}",
                    ldt,
                    symbols,
                    bidAskRatio,
                    iceberg ? "YES" : "NO",
                    prediction
            );*/
        }
    }
}
