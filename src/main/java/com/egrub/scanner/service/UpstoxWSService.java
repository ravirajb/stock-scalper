package com.egrub.scanner.service;


import com.egrub.scanner.model.upstox.FullD30;
import com.upstox.ApiClient;
import com.upstox.ApiException;
import com.upstox.Configuration;
import com.upstox.auth.OAuth;
import com.upstox.feeder.MarketDataStreamerV3;
import com.upstox.feeder.MarketUpdateV3;
import com.upstox.feeder.constants.Mode;
import com.upstox.feeder.listener.OnErrorListener;
import com.upstox.feeder.listener.OnMarketUpdateV3Listener;
import com.upstox.feeder.listener.OnOpenListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log4j2
public class UpstoxWSService {

    public static Map<String, String> INSTRUMENT_MAP = new HashMap<>();


    private final MongoTemplate mongoTemplate;

    public UpstoxWSService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        INSTRUMENT_MAP = Map.of(
                "NSE_EQ|INE040A01034", "HDFCBANK",
                "NSE_EQ|INE090A01021", "ICICIBANK",
                "NSE_EQ|INE002A01018", "RELIANCE",
                "NSE_EQ|INE062A01020", "SBIN",
                "NSE_EQ|INE009A01021", "INFY",
                "NSE_EQ|INE467B01029", "TCS",
                "NSE_EQ|INE397D01024", "AIRTEL",
                "NSE_EQ|INE018A01030", "LT",
                "NSE_EQ|INE154A01025", "ITC",
                "NSE_INDEX|Nifty 50", "NIFTY50"
        );
    }

    public void listenWS(String accessToken) throws ApiException {

        ApiClient defaultClient = Configuration.getDefaultApiClient();

        OAuth oAuth = (OAuth) defaultClient.getAuthentication("OAUTH2");
        oAuth.setAccessToken(accessToken);

        final MarketDataStreamerV3 marketDataStreamer = new MarketDataStreamerV3(defaultClient);

        marketDataStreamer.setOnOpenListener(new OnOpenListener() {

            @Override
            public void onOpen() {
                System.out.println("Connection Established");

                Set<String> instrumentKeys = new HashSet<>();
                instrumentKeys.add("NSE_INDEX|Nifty 50");
                instrumentKeys.add("NSE_EQ|INE040A01034");
                instrumentKeys.add("NSE_EQ|INE090A01021");
                instrumentKeys.add("NSE_EQ|INE002A01018");
                instrumentKeys.add("NSE_EQ|INE062A01020");
                instrumentKeys.add("NSE_EQ|INE009A01021");
                instrumentKeys.add("NSE_EQ|INE467B01029");
                instrumentKeys.add("NSE_EQ|INE397D01024");
                instrumentKeys.add("NSE_EQ|INE018A01030");
                instrumentKeys.add("NSE_EQ|INE154A01025");

                marketDataStreamer.subscribe(instrumentKeys, Mode.FULL_D30);

            }
        });

        marketDataStreamer.setOnMarketUpdateListener(new OnMarketUpdateV3Listener() {

            @Override
            public void onUpdate(MarketUpdateV3 marketUpdate) {
                handleMessage(marketUpdate);
            }
        });

        marketDataStreamer.setOnErrorListener(new OnErrorListener() {
            @Override
            public void onError(Throwable error) {
                System.out.println("On error message: " + error.getMessage());
            }
        });

        marketDataStreamer.connect();
    }

    private void handleMessage(MarketUpdateV3 marketUpdate) {

        try {
            marketUpdate
                    .getFeeds()
                    .keySet()
                    .forEach(key -> {
                        MarketUpdateV3.Feed feed = marketUpdate.getFeeds()
                                .get(key);

                        FullD30 instance = FullD30.builder()
                                .type(marketUpdate.getType())
                                .ts(new Date(marketUpdate.getCurrentTs()))
                                .feed(feed)
                                .marketInfo(marketUpdate.getMarketInfo())
                                .symbol(INSTRUMENT_MAP.get(key))
                                .build();
                        mongoTemplate.save(instance, "market_feed");

                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // log.info("jsonFormat:{}", marketUpdate.getFeeds());
    }
}
