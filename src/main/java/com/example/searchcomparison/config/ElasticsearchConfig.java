package com.example.searchcomparison.config;

import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.searchcomparison.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;

    @Value("${elasticsearch.username}")
    private String elasticsearchUsername;

    @Value("${elasticsearch.password}")
    private String elasticsearchPassword;

    @Override
    public ClientConfiguration clientConfiguration() {
        try {
            TrustStrategy trustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(null, trustStrategy)
                    .build();

            return ClientConfiguration.builder()
                    .connectedTo(elasticsearchHost)
                    .usingSsl(sslContext)
                    .withBasicAuth(elasticsearchUsername, elasticsearchPassword)
                    .withSocketTimeout(60000)
                    .withConnectTimeout(60000)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL context for Elasticsearch", e);
        }
    }
}