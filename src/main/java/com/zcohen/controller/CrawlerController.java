package com.zcohen.controller;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zcohen.exception.CrawlerException;
import com.zcohen.facade.CrawlerInternalLinksProcessor;
import com.zcohen.model.CrawlerVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A controller end point implementation for the crawler application
 * @author zoharC
 *
 */

@Slf4j
@RestController
@RequestMapping("/web-crawler/v1")
@RequiredArgsConstructor
public class CrawlerController {

	private final CrawlerInternalLinksProcessor crawlerService;

	/**
	 * Application end point for performing the crawler scan.
	 * @param url - request parameter, represents the URL to perform the crawler scan.
	 * @param actvId - given header activity id for tracing
	 * @return - relation map between each internal link in the given URL.
	 */
	@GetMapping("/scan")
	public ResponseEntity<Object> buildSiteMap(@RequestParam String url, @RequestHeader(value = "activity-id", required = false) String actvId){

		String activityId = actvId == null ? UUID.randomUUID().toString() : actvId;
		log.info("Recived a crawler index request, activityId {}", activityId);

		long startTime = System.nanoTime();
		List<CrawlerVO> response = null;

		try {
			response = crawlerService.process(url);
			
		} catch (CrawlerException e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
		}

		log.info("Evaluation has been finished in {} seconds", SECONDS.convert(System.nanoTime() - startTime, NANOSECONDS));
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
