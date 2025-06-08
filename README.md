## Stock Scalper

**Stock Scalper** is a Java-based project for developing, analyzing, and backtesting stock scalping strategies. The project includes advanced methods for backtesting trading strategies, analyzing results, and detecting anomalies in trading data using statistical techniques such as the cumulative distribution function (CDF) and the t-Digest algorithm.

---

## Features

- **Backtesting Engine:** Simulate trading strategies on historical data to evaluate performance, including PnL, drawdown, and win rate.
- **Data Analysis:** Analyze trade results and market data for insights and strategy refinement.
- **Anomaly Detection:** Identify unusual patterns or outliers in trading data using the CDF and t-Digest for robust, scalable anomaly detection.
- **Modular Java Codebase:** Organized for easy extension and integration with trading APIs.

---

## Key Methods

### **backtest**

The `backtest` method simulates your trading strategy using historical market data. It processes each data point (e.g., OHLCV candles), applies the strategy logic, and records trades as if they were executed in real time. The method outputs key metrics such as profit and loss, equity curve, drawdown, and win rate. This process is essential for validating the effectiveness and robustness of any scalping strategy before live deployment[2][5].

**Typical Flow:**
- Load historical data
- Apply strategy logic bar by bar
- Simulate order execution and capital management
- Log trades and calculate performance metrics

### **analyze**

The `analyze` method evaluates the results of the backtest or live trades. It computes statistical and performance metrics, such as:
- Total return
- Maximum drawdown
- Sharpe ratio
- Win/loss ratio

This helps traders understand the strengths and weaknesses of their strategy, and is critical for iterative improvement[5].

### **populateTDigest**

The `populateTDigest` method collects and summarizes data points (such as returns, trade durations, or price changes) using the t-Digest algorithm. t-Digest is a probabilistic data structure that efficiently estimates quantiles and percentiles, even for large data streams. This is especially useful for financial data, which can be heavy-tailed and contain outliers[3].

**Usage:**
- Feed trade or price data into the t-Digest
- Efficiently compute quantiles (e.g., median, 95th percentile)
- Prepare for CDF-based anomaly detection

### **Anomaly Detection with CDF**

The project applies anomaly detection by leveraging the cumulative distribution function (CDF) computed from the t-Digest. For each new data point (e.g., a trade's return or a price move), its CDF value is calculated. If this value falls outside a predefined range (e.g., below 1% or above 99%), the point is flagged as an anomaly. This approach is robust to non-normal data and adapts well to changing distributions, making it suitable for financial time series[3].

**Benefits:**
- Detects rare or extreme events in trading data
- Reduces false positives compared to simple thresholding
- Scales to large datasets and real-time streams

---

## Project Structure

```
stock-scalper/
├── src/
│   └── ... (Java source files, including backtest, analyze, and t-Digest methods)
├── pom.xml
├── mvnw, mvnw.cmd
```

---

## Getting Started

**Prerequisites:**
- Java 8 or higher
- Maven

**Build:**
```sh
git clone https://github.com/ravirajb/stock-scalper.git
cd stock-scalper
./mvnw clean install
```

**Run:**
- Specify the main class in `src/` and run via your IDE or the command line.

---

## Usage Example

1. **Backtest a Strategy:**
   - Configure your strategy and data source.
   - Call `backtest()` to simulate trades and record results.

2. **Analyze Results:**
   - Use `analyze()` to compute performance metrics.

3. **Detect Anomalies:**
   - Use `populateTDigest()` to summarize data.
   - Apply CDF-based anomaly detection to flag unusual trades or market moves.

---

## Anomaly Detection Reference

> “EGADS (Extensible Generic Anomaly Detection System) is an open-source Java package to automatically detect anomalies in large scale time-series data... using density-based methods and CDF thresholds for robust detection.”[3]

---

## Contributing

Contributions are welcome. Fork the repo, create a feature branch, and submit a pull request.

---

## License

No license file is currently specified. Please add one if you intend to open source your contributions.

---

*For questions or suggestions, please open an issue in the repository.*

---
