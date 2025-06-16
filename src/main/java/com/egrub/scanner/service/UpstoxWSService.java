package com.egrub.scanner.service;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.upstox.ApiClient;
import com.upstox.ApiException;
import com.upstox.Configuration;
import com.upstox.api.WebsocketAuthRedirectResponse;
import com.upstox.auth.OAuth;
import com.upstox.marketdatafeederv3udapi.rpc.proto.MarketDataFeedV3;
import io.swagger.client.api.WebsocketApi;
import lombok.extern.log4j.Log4j2;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
@Log4j2
public class UpstoxWSService {

    public void listenWS(String accessToken) throws ApiException {
        ApiClient authenticatedClient = authenticateApiClient(accessToken);

        URI serverUri = getAuthorizedWebSocketUri(authenticatedClient);

        WebSocketClient client = createWebSocketClient(serverUri);
        client.connect();
    }

    private ApiClient authenticateApiClient(String accessToken) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        OAuth oAuth = (OAuth) defaultClient.getAuthentication("OAUTH2");
        oAuth.setAccessToken(accessToken);

        return defaultClient;
    }

    private URI getAuthorizedWebSocketUri(ApiClient authenticatedClient) throws ApiException {
        WebsocketApi websocketApi = new WebsocketApi(authenticatedClient);
        WebsocketAuthRedirectResponse response = websocketApi.getMarketDataFeedAuthorize("2.0");

        return URI.create(response.getData()
                .getAuthorizedRedirectUri());
    }

    private WebSocketClient createWebSocketClient(URI serverUri) {
        return new WebSocketClient(serverUri) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                log.info("Opened Connection");
                sendSubscriptionRequest(this);
            }

            @Override
            public void onMessage(String message) {
                log.info("onMessage, String : message:{}", message);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                handleBinaryMessage(bytes);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.info("on close by:{}, Info:{}",
                        (remote ? "remote peer" : "us"), reason);
            }

            @Override
            public void onError(Exception ex) {
                log.error("WS Error ", ex);
            }
        };
    }

    private void sendSubscriptionRequest(WebSocketClient client) {
        JsonObject requestObject = constructSubscriptionRequest();
        byte[] binaryData = requestObject.toString()
                .getBytes(StandardCharsets.UTF_8);

        log.info("subscription message {}", requestObject);
        client.send(binaryData);
    }

    private JsonObject constructSubscriptionRequest() {
        JsonObject dataObject = new JsonObject();
        dataObject.addProperty("mode", "full_d30");

        JsonArray instrumentKeys = new Gson().toJsonTree(Arrays.asList("NSE_INDEX|Nifty Bank", "NSE_INDEX|Nifty 50"))
                .getAsJsonArray();


        dataObject.add("instrumentKeys", instrumentKeys);

        JsonObject mainObject = new JsonObject();
        mainObject.addProperty("guid", "someguid");
        mainObject.addProperty("method", "sub");
        mainObject.add("data", dataObject);

        return mainObject;
    }

    private void handleBinaryMessage(ByteBuffer bytes) {

        try {
            MarketDataFeedV3.FeedResponse feedResponse =
                    MarketDataFeedV3.FeedResponse.parseFrom(bytes.array());

            String jsonFormat = JsonFormat.printer()
                    .print(feedResponse);

            log.info("jsonFormat:{}", jsonFormat);

        } catch (InvalidProtocolBufferException e) {
            log.error("Unparseable error", e);
        }
    }
}
