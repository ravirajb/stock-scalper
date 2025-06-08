package com.egrub.scanner.service;

import com.egrub.scanner.model.upstox.Instrument;
import com.egrub.scanner.model.upstox.MarketCapEntry;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Log4j2
public class InstrumentsLoader {
    private static final String CSV_FILE_NAME = "nse_with_sector.csv"; // Ensure this matches your file name
    private static final String MARKET_CAP_CSV_FILE_NAME = "MW-NIFTY-TOTAL-MARKET.csv";

    /**
     * Reads a CSV file from the resources folder and populates a list of Instrument objects.
     * It uses Jackson CSV for parsing and handles nulls/parsing failures silently.
     *
     * @return A List of Instrument objects. Returns an empty list if the file cannot be read or parsed.
     */

    private static Map<String, Double> loadMarketCapitalData() {
        Map<String, Double> marketCaps = new HashMap<>();
        CsvMapper csvMapper = new CsvMapper();

        // Configure the mapper to allow unquoted field values that might contain delimiters
        csvMapper.enable(CsvParser.Feature.EMPTY_UNQUOTED_STRING_AS_NULL);
        // Also useful for robustness against extra unexpected columns
        csvMapper.enable(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE);
        csvMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Schema for MW-NIFTY-TOTAL-MARKET.csv
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        ObjectReader objectReader = csvMapper.readerFor(MarketCapEntry.class).with(schema);

        URL resource = InstrumentsLoader.class.getClassLoader().getResource(MARKET_CAP_CSV_FILE_NAME);
        if (resource == null) {
            System.err.println("Error: Market Cap CSV file not found in resources: " + MARKET_CAP_CSV_FILE_NAME);
            return Collections.emptyMap();
        }

        try (InputStream inputStream = resource.openStream()) {
            MappingIterator<MarketCapEntry> iterator = objectReader.readValues(inputStream);
            while (iterator.hasNext()) {
                MarketCapEntry entry = iterator.next();
                String symbol = entry.getSymbol();
                String valueStr = entry.getValue();

                if (symbol != null && valueStr != null && !symbol.isEmpty() && !valueStr.isEmpty()) {
                    try {
                        // Remove commas before parsing to Double
                        Double marketCap = Double.parseDouble(valueStr.replace(",", ""));
                        marketCaps.put(symbol, marketCap);
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse market capital value for Symbol '" + symbol + "': " + valueStr + ". Error: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading Market Cap CSV file: " + MARKET_CAP_CSV_FILE_NAME + " - " + e.getMessage());
            return Collections.emptyMap();
        }
        return marketCaps;
    }

    public static List<Instrument> loadInstrumentsFromCsv() {
        List<Instrument> instruments = new ArrayList<>();
        CsvMapper csvMapper = new CsvMapper();

        // Load market capital data once
        Map<String, Double> marketCapData = loadMarketCapitalData();
        if (marketCapData.isEmpty()) {
            System.err.println("Market Capital data could not be loaded or is empty. Instruments will not have market capital.");
        }

        // Configure CSV schema for headers
        // Use 'tradingsymbol' as 'Symbol' if that's the column name in the CSV
        // Ensure column names in CsvSchema match your CSV header exactly.
        CsvSchema schema = CsvSchema.builder()
                .addColumn("instrument_key")
                .addColumn("exchange_token", CsvSchema.ColumnType.NUMBER)
                .addColumn("Symbol") // Make sure this matches the CSV column name
                .addColumn("name")
                .addColumn("last_price", CsvSchema.ColumnType.NUMBER)
                .addColumn("expiry")
                .addColumn("strike", CsvSchema.ColumnType.NUMBER)
                .addColumn("tick_size", CsvSchema.ColumnType.NUMBER)
                .addColumn("lot_size", CsvSchema.ColumnType.NUMBER)
                .addColumn("instrument_type")
                .addColumn("option_type")
                .addColumn("exchange")
                .addColumn("Industry") // New column
                .setUseHeader(true)
                .build();

        ObjectReader objectReader = csvMapper.readerFor(Instrument.class).with(schema);

        // Try to load the CSV from resources
        // Get the URL for the CSV file from the classpath
        URL resource = InstrumentsLoader.class.getClassLoader().getResource(CSV_FILE_NAME);

        if (resource == null) {
            System.err.println("Error: CSV file not found in resources: " + CSV_FILE_NAME);
            return Collections.emptyList();
        }

        try (InputStream inputStream = resource.openStream()) {
            MappingIterator<Instrument> instrumentIterator = objectReader.readValues(inputStream);

            while (instrumentIterator.hasNext()) {
                try {
                    Instrument instrument = instrumentIterator.next();
                    // Filter for Equity and Index types from NSE_EQ and NSE_INDEX as requested
                    if (("EQUITY".equalsIgnoreCase(instrument.getInstrumentType())
                            && "NSE_EQ".equalsIgnoreCase(instrument.getExchange())) ||
                            ("INDEX".equalsIgnoreCase(instrument.getInstrumentType())
                                    && "NSE_INDEX".equalsIgnoreCase(instrument.getExchange()))) {

                        Double marketCap = marketCapData.get(instrument.getSymbol());
                        instrument.setMarketCapitalInCrores(marketCap); // Will be null if not found

                        instruments.add(instrument);
                    }
                } catch (Exception e) {
                    // Silently handle parsing errors for individual records
                    System.err.println("Warning: Could not parse record. Skipping. Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + CSV_FILE_NAME + " - " + e.getMessage());
            return Collections.emptyList();
        }

        return instruments;
    }
}
