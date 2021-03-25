package com.zcohen;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


/**
 * A spring boot application provides a link relations for given url/website address.
 * @author zoharC
 * Date: 03-24-2021
 *
 */
@SpringBootApplication
public class SednaWebCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SednaWebCrawlerApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(@Value("${web.crawler.connection.timeout:30000}") int timeout, RestTemplateBuilder builder) {

		return builder.setConnectTimeout(Duration.ofMillis(timeout))
				      .setReadTimeout(Duration.ofMillis(timeout))
				      .build();
	} 

}
