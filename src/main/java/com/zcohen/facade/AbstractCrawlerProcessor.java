package com.zcohen.facade;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.zcohen.exception.CrawlerException;
import com.zcohen.model.CrawlerVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * An abstract crawler builder implementation.
 * @author zoharC
 * Date: 03-24-2021
 *
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCrawlerProcessor {

	private final RestTemplate restTemplate;
	protected String inputURL;

	/**
	 * The main processor method, invokes depended steps.
	 * @param url - input parent URL to perform the scan.
	 * @return list for site relation links and assets
	 * @throws CrawlerException
	 */
	public abstract List<CrawlerVO> process(final String url) throws CrawlerException;
	
	/**
	 * A recursive method traverse over the URL link and the depended URLs and index the relations.
	 * The traverse ends once all the depends links are visited.
	 * 
	 * @param url - an URL input for the HTML object scan.
	 */
	protected abstract void crawlerExecutor(String url);
	
	
	/**
	 * Validates that the URL is valid for further processing.
	 * 1) non empty - as it provided as path parameter.
	 * 2) match URL regular expression.
	 * 3) returns HTTP 200 status.
	 * @param url - website candidate URL address for running the crawler implementation.
	 * @throws CrawlerException - once one of the mandatory checks failed.
	 */
	protected void validateURL(String url) throws CrawlerException {

		if(url.isEmpty()) {
			log.error("The input url is blank, abort...");
			throw new IllegalArgumentException("The given URL is empty.");
		}

		boolean matches = Pattern.matches("^(http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+)?$", url);

		if(!matches) {
			log.error("The input url is not valid, abort...");
			throw new IllegalArgumentException("The given URL is not valid.");
		}

		try {
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

			if(response.getStatusCode() != HttpStatus.OK) {
				throw new CrawlerException(String.format("The URL %s is unreachable", url));
			}

			this.inputURL = new URL(url).getAuthority().replace("www.", "");
		}catch(Exception e) {
			throw new CrawlerException(String.format("An error occured during URL validation, error: %s", e.getMessage()));
		}

	}

	/**
	 * a function lambda expression, removes URL's path parameters and special characters.
	 * @param htmlElement - a HTML link content element
	 * @return clean URL string.
	 */
	
    protected Function<Element, String> normilazeLink = (htmlElement) -> {
    	
    	String href = htmlElement.attr("abs:href");
		int index = href.indexOf("?");
		if(index > 0) {
			return href.substring(0, index);
		}
		index = href.indexOf("#");
		if(index > 0) {
			return href.substring(0, index);
		}
		if(href.endsWith("/")) {
			return href.substring(0, href.length() - 1);
		}
		return href;
    	
    };
    

	/**
	 * Builds expected web application response.
	 * @param links - parent link and collection of the reference dependents links.
	 * @param staticAssets - list of the parent link and associated static assets.
	 * @return - expected web service response.
	 */
	public List<CrawlerVO> buildResponse(Map<String, Set<String>> links, Map<String, Set<String>> staticAssets) {

		return links.entrySet().stream().map(entry ->
		                                     CrawlerVO.builder().page(entry.getKey())
		                                                        .links(entry.getValue())
		                                                        .staticAssets(staticAssets.get(entry.getKey())).build())
				                         .collect(Collectors.toList());
	}

}
