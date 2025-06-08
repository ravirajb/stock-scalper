package com.egrub.scanner.model;

import com.tdunning.math.stats.TDigest;

public class TDigestHelper {
    public TDigest priceDigest;
    public TDigest volumeDigest;

    public TDigestHelper() {
        this.priceDigest = TDigest.createDigest(500);
        this.volumeDigest = TDigest.createDigest(500);
    }

    public void add(double price, double volume) {
        this.priceDigest.add(price);
        this.volumeDigest.add(volume);
    }

    public double getPricePercentile(double price) {
        return this.priceDigest.cdf(price) * 100;
    }

    public double getVolumePercentile(double price) {
        return this.volumeDigest.cdf(price) * 100;
    }

    public void reset() {
        this.priceDigest = TDigest.createDigest(500);
        this.volumeDigest = TDigest.createDigest(500);
    }
}
