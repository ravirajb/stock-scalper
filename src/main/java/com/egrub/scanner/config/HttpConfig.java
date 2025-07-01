package com.egrub.scanner.config;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpConfig {

    @Bean
    public BasicCookieStore cookieStore() {
        return new BasicCookieStore();
    }

    @Bean
    public RestTemplate restTemplate() {
        int connectTimeoutMillis = 10000;
        int readTimeoutMillis = 10000;

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(200);           // Total connections
        connManager.setDefaultMaxPerRoute(50);  // Per-host connections

        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)  // <--- the key change
                .setConnectTimeout(connectTimeoutMillis)
                .setConnectionRequestTimeout(connectTimeoutMillis)
                .setSocketTimeout(readTimeoutMillis)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore())
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connManager)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }
}
