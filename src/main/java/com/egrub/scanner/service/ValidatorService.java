package com.egrub.scanner.service;

import com.egrub.scanner.config.HttpConfig;
import com.egrub.scanner.model.nse.*;
import com.egrub.scanner.model.upstox.FullD30;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upstox.feeder.MarketUpdateV3.*;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.egrub.scanner.utils.Constants.getStartDate;

@Service
@Log4j2
public class ValidatorService {

    private final MongoTemplate mongoTemplate;
    private RestTemplate restTemplate;

    @Autowired
    private HttpConfig httpConfig;

    private static final String AUTH_TOKEN =
            "Bearer abc";

    private static final String BASE_DELIVERY_URL =
            "https://api.stockedge.com/Api/ListingDashboardApi/GetAdjDeliveriesByTimeSpan/%d?timeSpan=1&lang=en";

    private static final String BASE_URL =
            "https://api.stockedge.com/Api/SecurityDashboardApi/GetLatestSecurityInfo/";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ValidatorService(MongoTemplate mongoTemplate, RestTemplate restTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.restTemplate = restTemplate;
    }

    private HistoricalData getTodaysDeliveryPercentage(String symbol,
                                                       HttpHeaders baseHeaders,
                                                       String toDate) {
        String todayAPIUrl = "https://www.nseindia.com/api/quote-equity?symbol="
                + symbol + "&section=trade_info";
        try {
            HttpEntity<String> apiEntity = new HttpEntity<>(baseHeaders);

            HttpEntity<String> todaysResponse = restTemplate
                    .exchange(todayAPIUrl, HttpMethod.GET, apiEntity, String.class);

            NseApiResponse result = objectMapper
                    .readValue(todaysResponse.getBody(), NseApiResponse.class);

            double delivery = result.getSecurityWiseDP().getDeliveryPercent();

            return new HistoricalData(toDate,
                    "EQ",
                    String.valueOf(delivery),
                    0d,
                    result.getSecurityWiseDP().getQuantityTraded(),
                    result.getSecurityWiseDP().getDeliveryQuantity());

        } catch (Exception ex) {
            log.error("error while fetching the today's data for:{}, error:{}",
                    symbol, ex.getLocalizedMessage());
        }

        return new HistoricalData();
    }

    public List<DateDeliveryPercent> getDeliverablePercentage(String symbol,
                                                              String toDate,
                                                              int period) {
        //restTemplate = httpConfig.restTemplate();

        try {
            String url = "https://www.nseindia.com/get-quotes/equity?symbol=" + symbol;
            String apiQuoteUrl = "https://www.nseindia.com/api/quote-equity?symbol="
                    + symbol;

            Connection.Response response = Jsoup
                    .connect(url)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
                            "Chrome/122.0.0.0 Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate")  // no br
                    .method(Connection.Method.GET)
                    .execute();

            // Step 4: Get cookies
            Map<String, String> cookies = response.cookies();
            HttpHeaders baseHeaders = getDefaultHeaders();

            String apiUrl = UriComponentsBuilder
                    .fromHttpUrl("https://www.nseindia.com/api/historicalOR/" +
                            "generateSecurityWiseHistoricalData")
                    .queryParam("from", getStartDate(toDate, period, true))
                    .queryParam("to", toDate)
                    .queryParam("symbol", symbol)
                    .queryParam("type", "priceVolumeDeliverable")
                    .queryParam("series", "EQ").build().toUriString();

            baseHeaders.set("Referer", "https://www.nseindia.com/api/quote-equity?symbol="
                    + symbol);

            String cookieHeader = cookies
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey()
                            + "=" + entry.getValue())
                    .collect(Collectors.joining("; "));

            baseHeaders.set("Cookie", cookieHeader);

            HttpEntity<String> apiEntity = new HttpEntity<>(baseHeaders);

            ResponseEntity<String> baseResponse = restTemplate
                    .exchange(apiUrl, HttpMethod.GET, apiEntity, String.class);

            HistoricalResponse historicalResponse = objectMapper
                    .readValue(baseResponse.getBody(), HistoricalResponse.class);

            Pattern ddMMyyyyPattern = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$");
            String formattedTarget;
            if (ddMMyyyyPattern.matcher(toDate).matches()) {
                DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                DateTimeFormatter matchFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate parsed = LocalDate.parse(toDate, inputFormat);
                formattedTarget = parsed.format(matchFormat);
            } else {
                formattedTarget = toDate;
            }

            ResponseEntity<TradeInfoResponse> tradeInfoResponse =
                    restTemplate.exchange(
                            "https://www.nseindia.com/api/quote-equity?symbol="
                                    + symbol
                                    + "&section=trade_info",
                            HttpMethod.GET,
                            apiEntity,
                            TradeInfoResponse.class
                    );

            TradeInfoResponse tradeInfo = tradeInfoResponse.getBody();
            double sectorPe = 0, symbolPe = 0;

            /*try {
                ResponseEntity<QuoteResponse> quote = restTemplate.exchange(
                        apiQuoteUrl,
                        HttpMethod.GET,
                        apiEntity,
                        QuoteResponse.class
                );

                QuoteResponse quoteResponse = quote.getBody();

                if (quoteResponse != null && quoteResponse.getMetadata() != null) {
                    Metadata quoteMetadata = quoteResponse.getMetadata();
                    sectorPe = quoteMetadata.getPdSectorPe();
                    symbolPe = quoteMetadata.getPdSymbolPe();
                } else {
                    sectorPe = 0;
                    symbolPe = 0;
                }
            } catch (Exception ex) {
                log.error("Exception symbol: {}, ex:{}", symbol,
                        ex.getLocalizedMessage());
                sectorPe = 0;
                symbolPe = 0;
            }*/

            String activeSeries;

            double marketCap, freeflaot;
            double dailyVolatility, annualVolatility;

            if (tradeInfo != null && tradeInfo.getMarketDeptOrderBook() != null
                    &&
                    tradeInfo.getMarketDeptOrderBook()
                            .getTradeInfo() != null) {
                TradeInfo scrpInfo = tradeInfo.getMarketDeptOrderBook()
                        .getTradeInfo();

                activeSeries = tradeInfo.getMarketDeptOrderBook()
                        .getTradeInfo()
                        .getActiveSeries();
                marketCap = scrpInfo.getTotalMarketCap();
                freeflaot = scrpInfo.getFfmc();
                dailyVolatility = scrpInfo.getCmDailyVolatility();
                annualVolatility = scrpInfo.getCmAnnualVolatility();
            } else {
                marketCap = 0;
                freeflaot = 0;
                dailyVolatility = 0;
                annualVolatility = 0;
                activeSeries = "";
            }

            if (historicalResponse
                    .getData()
                    .stream()
                    .noneMatch(data ->
                            formattedTarget.equals(data.getDate()))) {
                historicalResponse
                        .getData()
                        .add(getTodaysDeliveryPercentage(symbol, baseHeaders, toDate));
            }

            double finalSymbolPe = symbolPe;
            double finalSectorPe = sectorPe;

            return historicalResponse
                    .getData()
                    .stream()
                    .map(d ->
                            DateDeliveryPercent
                                    .builder()
                                    .date(d.getDate())
                                    .activeSeries(activeSeries)
                                    .annualVolatility(annualVolatility)
                                    .dailyVolatility(dailyVolatility)
                                    .tickerPe(finalSymbolPe)
                                    .sectorPe(finalSectorPe)
                                    .freeFloat(freeflaot)
                                    .marketCap(marketCap)
                                    .percent(safeParseDouble
                                            (d.getDeliverablePercentage()))
                                    .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Exception while parsing NSE: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    private double safeParseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 100.0;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }


    private HttpHeaders getDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/122.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");
        headers.set("Accept-Language", "en-US,en;q=0.5");
        headers.set("Accept-Encoding", "gzip, deflate");
        headers.set("Connection", "keep-alive");
        headers.set("Sec-Fetch-Dest", "empty");
        headers.set("Sec-Fetch-Mode", "cors");
        headers.set("Sec-Fetch-Site", "same-origin");
        headers.set("Origin", "https://www.nseindia.com");
        return headers;
    }

    public void fetchAndSaveDeliveryData() {

        int listingId = 1;

        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("Type").is("Equity").and("IsInactive").is(false));

            List<Document> results = mongoTemplate.find(query, Document.class, "securities");

            log.info("size:{}", results.size());

            for (Document doc1 : results) {
                try {
                    Object fetchedId = doc1.get("DefaultListingID");
                    List<Document> listings = (List<Document>) doc1.get("Listings");

                    String listingSymbol = (String) listings.get(0).get("ListingSymbol");
                    String exchangeName = (String) listings.get(0).get("ExchangeName");


                    listingId = (int) fetchedId;

                    String url = String.format(BASE_DELIVERY_URL, listingId);

                    HttpHeaders headers = new HttpHeaders();
                    headers.set("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64)");
                    headers.set("Accept", "application/json, text/plain, */*");
                    headers.set("Authorization", AUTH_TOKEN);
                    headers.set("Referer", "https://web.stockedge.com/");
                    headers.set("Origin", "https://web.stockedge.com");

                    HttpEntity<Void> entity = new HttpEntity<>(headers);

                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        String body = response.getBody();
                        JsonNode rootNode = objectMapper.readTree(body);

                        if (rootNode.isArray()) {
                            List<Document> documents = new ArrayList<>();

                            for (JsonNode item : rootNode) {
                                Document doc = Document.parse(item.toString());
                                doc.append("ListingID", listingId);
                                doc.append("FetchedAt", System.currentTimeMillis());
                                doc.append("listingSymbol", listingSymbol);
                                doc.append("exchangeName", exchangeName);

                                documents.add(doc);
                            }

                            if (!documents.isEmpty()) {
                                mongoTemplate.insert(documents, "adj_deliveries");
                                log.info("Inserted :{}, documents for ListingID:{} ", documents.size(), listingId);
                            } else {
                                log.error("No data to insert for ListingID:{} ", listingId);
                            }
                        } else {
                            log.error("Expected array for ListingID :{},  but got{}  ", listingId, rootNode.getNodeType());
                        }
                    } else {
                        log.error("Failed HTTP response for ListingID " + listingId + ": " + response.getStatusCodeValue());
                    }

                    Thread.sleep(300); // avoid rate-limiting
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            log.error("Error fetching delivery data for ListingID {}: {}", listingId, e.getMessage());
        }
    }

    public void saveSotckEdgeId() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64)");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Authorization", AUTH_TOKEN);
        headers.set("Referer", "https://web.stockedge.com/");
        headers.set("Origin", "https://web.stockedge.com");

        for (int id = 20001; id <= 30000; id++) {
            try {
                String url = BASE_URL + id + "?lang=en";
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    String body = response.getBody();
                    JsonNode rootNode = objectMapper.readTree(body);

                    Document doc = Document.parse(body);
                    doc.append("FetchedID", id);

                    mongoTemplate.insert(doc, "securities");

                    log.info("Stored ID {} - {}", id, rootNode.path("Name").asText());
                } else {
                    log.warn("Skipped ID {} - HTTP {}", id, response.getStatusCodeValue());
                }

                Thread.sleep(200); // avoid rate-limiting

            } catch (Exception ex) {
                log.error("Error fetching ID {}: {}", id, ex.getMessage());
            }
        }
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
                List<Quote> quotes = Optional.ofNullable(tick.getFeed()).map(Feed::getFullFeed).map(FullFeed::getMarketFF).map(MarketFullFeed::getMarketLevel).map(MarketLevel::getBidAskQuote).orElse(Collections.emptyList());

                for (Quote q : quotes) {
                    totalBidQty += q.getBidQ();
                    totalAskQty += q.getAskQ();

                    bidCluster.merge(q.getBidP(), q.getBidQ(), Long::sum);
                    askCluster.merge(q.getAskP(), q.getAskQ(), Long::sum);
                }
            }

            double bidAskRatio = totalAskQty == 0 ? 0.0 : (double) totalBidQty / totalAskQty;

            icebergBid = bidCluster.values().stream().filter(qty -> qty > 3000).count();
            icebergAsk = askCluster.values().stream().filter(qty -> qty > 3000).count();

            LocalDateTime ldt = Instant.ofEpochMilli(bucketTs).atZone(ZoneId.of("Asia/Kolkata")).toLocalDateTime();

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



            // ALERT OUTPUT
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
