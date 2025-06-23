package com.egrub.scanner.service;


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
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Log4j2
public class UpstoxWSService {

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
                instrumentKeys.add("MCX_FO|439037");

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
        log.info("jsonFormat:{}", marketUpdate.getFeeds());
    }
}
