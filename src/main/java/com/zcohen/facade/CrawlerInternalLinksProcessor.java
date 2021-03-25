package com.zcohen.facade;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.zcohen.exception.CrawlerException;
import com.zcohen.model.CrawlerVO;

import lombok.extern.slf4j.Slf4j;

/**
 * A Crawler facade implementation for scanning specific web page and navigate to the depended internal pages only.
 * 
 * @author zoharC
 * Date: 03-24-2021
 */
@Slf4j
@Service
public class CrawlerInternalLinksProcessor extends AbstractCrawlerProcessor{

	private Set<String> visited;
	private Map<String,Set<String>> relaitons;
	private Map<String, Set<String>> staticAssets;
	private final int timeout;

	public CrawlerInternalLinksProcessor(@Value("${web.crawler.connection.timeout:30000}") int timeout, RestTemplate restTemplate) {

		super(restTemplate);
		this.timeout = timeout;
		this.visited = ConcurrentHashMap.newKeySet();
		this.relaitons =  new ConcurrentHashMap<>();
		this.staticAssets = new ConcurrentHashMap<>();
	}

	@Override
	public List<CrawlerVO> process(String url) throws CrawlerException {
    
		validateURL(url);
        //Clear from the previous run.
		this.visited.clear();
		this.relaitons.clear();
		this.staticAssets.clear();

		crawlerExecutor(url);
		return buildResponse(relaitons, staticAssets);
	}

	/**
	 * A multi-thread (fork Join) recursion that scans parent URL HTML content finds the dependents links 
	 * and the static assets.
	 */
	@Override
	protected void crawlerExecutor(String url) {

		log.info("----- Recived a new URL to index [{}]-----", url);
		if(visited.contains(url)) {
			return;
		} 
		visited.add(url);
		try {
			Document document = Jsoup.connect(url).timeout(timeout).get();
			//Gets the HTML links from the given URL
			Set<String> links = document.select("a[href]").parallelStream()
					.map(normilazeLink)
					.filter(link -> internalFilter.test(link, url))
					.collect(Collectors.toSet());
			//Gets the HTML static assets for the given URL
			staticAssets.put(url, document.select("img").parallelStream()
					.map(img -> img.absUrl("src"))
					.filter(link -> !link.isEmpty())
					.collect(Collectors.toSet()));
			
			relaitons.put(url, links);
			log.info("----- Link {} hass been processed successfully, found {} refreance unique links to index-----", url, links.size());
			links.stream().parallel().forEach(this::crawlerExecutor);
		} catch (IOException e) {
			log.error("An error occured during connect to: {}, error: {}", url, e.getMessage());

		}
	}

	/**
	 * A Bi-Predicate lambda expression, uses to filter/verify the given link is an internal link.
	 */
	private BiPredicate<String,String> internalFilter = (link, inputLink) -> {

		try {
			if(link.equals(inputLink)) {
				return false;
			}
			URL urlObj = new URL(link);
			String siteAuthority = urlObj.getAuthority();
			if(siteAuthority == null) {
				return false;
			}
			//validates that the given link is internal for the input domain
			return siteAuthority.endsWith(this.inputURL);
		} catch (MalformedURLException e) {
			return false;
		}
	};

}
