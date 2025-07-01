package com.egrub.scanner.controller;

import com.egrub.scanner.model.ScripListRequest;
import com.egrub.scanner.model.StockAnalyzerRequest;
import com.egrub.scanner.model.eod.TickerRequest;
import com.egrub.scanner.model.nse.DateDeliveryPercent;
import com.egrub.scanner.service.AnalyzerService;
import com.egrub.scanner.service.EodHdService;
import com.egrub.scanner.service.UpstoxWSService;
import com.egrub.scanner.service.ValidatorService;
import com.egrub.scanner.utils.Constants;
import com.upstox.ApiException;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.egrub.scanner.utils.Constants.*;

@RestController
@Log4j2
public class StockController {
    private final AnalyzerService analyzerService;
    private final UpstoxWSService upstoxWSService;
    private final EodHdService eodHdService;
    private final ValidatorService validatorService;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledTask;

    public StockController(AnalyzerService analyzerService,
                           UpstoxWSService upstoxWSService,
                           ValidatorService validatorService,
                           EodHdService eodHdService) {
        this.analyzerService = analyzerService;
        this.upstoxWSService = upstoxWSService;
        this.eodHdService = eodHdService;
        this.validatorService = validatorService;
    }

    @PostMapping("/api/v1/exchange-symbols")
    public String fetchSymbols(@RequestBody TickerRequest tickerRequest) {

        this.eodHdService.getAllSymbols(tickerRequest.getExchangeToken(),
                tickerRequest.getApiToken());

        return "true";
    }

    @PostMapping("/api/v1/analyse-us-stocks")
    public String analyseUsStocks(@RequestBody TickerRequest tickerRequest)
            throws IOException {
        analyzerService.writeToFile(
                this.eodHdService.getDailyCandles(tickerRequest.getExchangeToken(),
                        tickerRequest.getApiToken()), "us_23Jun.csv");
        return "true";
    }


    @PostMapping("/api/v1/listen")
    public String listen(@RequestBody StockAnalyzerRequest request) throws ApiException {
        upstoxWSService.listenWS(request.getAccessToken());
        return "true";
    }

    @PostMapping("/api/v1/potential-stocks")
    public String potentialStockBuilder(@RequestBody ScripListRequest request) {

        LocalDate date =
                LocalDate.parse(request.getLookupDate(), DATE_FORMATTER);

        List<LocalDate> workingDays = new ArrayList<>();
        while (workingDays.size() < 8) {
            date = date.minus(1, ChronoUnit.DAYS);
            if (!(date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    date.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                workingDays.add(date);
            }
        }

        workingDays.forEach(day -> {
            try {
                analyzerService.writeToFile(
                        analyzerService.getPotentialStocks(
                                day.format(DATE_FORMATTER),
                                request.getAccessToken(),
                                VALID_INSTRUMENT,
                                request.getLookBackPeriod()),
                        request.getFileName() + "_" + day + ".csv");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return "true";
    }

    @GetMapping("/trade-info/{symbol}/{toDate}/{period}")
    public Map<String, List<DateDeliveryPercent>> getTradeInfo(@PathVariable String symbol,
                                                               @PathVariable String toDate,
                                                               @PathVariable int period) {
        Map<String, List<DateDeliveryPercent>> results = new HashMap<>();
        VALID_INSTRUMENT.forEach(
                instrument -> {
                    results.put(instrument.getSymbol(),
                            validatorService.getDeliverablePercentage(instrument.getSymbol(),
                                    toDate,
                                    period));
                }
        );
        return results;
    }

    @PostMapping("/api/v1/backtest")
    public String backtest(@RequestBody StockAnalyzerRequest request) throws IOException {

        VALID_INSTRUMENT
                .forEach(instrument -> {
                    if (!instrument.getSymbol().contains("ETF")) {
                        analyzerService.populateDigests(
                                instrument.getInstrumentKey(),
                                instrument.getSymbol(),
                                request.getStartDate(),
                                request.getAccessToken(),
                                request.getBoxPeriod(),
                                request.getLookBackPeriod()
                        );

                        analyzerService.backtest(
                                instrument.getInstrumentKey(),
                                instrument.getSymbol(),
                                request.getStartDate(),
                                request.getAccessToken());
                    }
                });

        rungc();

        // analyzerService.writeToFile();

        return "true";
    }

    @PostMapping("/api/v1/analyze")
    public String analyzeStocks(@RequestBody StockAnalyzerRequest request) {

        log.info("Instrument Size:{}", VALID_INSTRUMENT.size());

        VALID_INSTRUMENT
                .forEach(instrument -> {
                    analyzerService.populateDigests(
                            instrument.getInstrumentKey(),
                            instrument.getSymbol(),
                            LocalDate.now().format(Constants.DATE_FORMATTER),
                            request.getAccessToken(),
                            request.getBoxPeriod(),
                            request.getLookBackPeriod()
                    );
                });

        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            return "Task is already scheduled.";
        }

        rungc();

        Runnable task = () -> {
            VALID_INSTRUMENT
                    .forEach(instrument -> {
                        log.info("Analyzing for instrument:{}", instrument.getSymbol());
                        analyzerService.analyze(
                                instrument.getInstrumentKey(),
                                instrument.getSymbol(),
                                request.getAccessToken()
                        );
                    });
        };

        long initialDelay = computeDelayUntilNext5MinuteMark();
        long period = TimeUnit.MINUTES.toSeconds(5); // 5 minutes

        System.out.println("Task will first run in " + initialDelay + " seconds");

        scheduledTask = scheduler.scheduleAtFixedRate(
                task,
                initialDelay,
                period,
                TimeUnit.SECONDS);

        return "Task scheduled to run at every 5th minute of the hour.";
    }

    @GetMapping("/api/v1/analyze")
    public String analyze() {
        return "true";
    }


}
