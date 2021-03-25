package com.zcohen.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

/**
 * A value object for application response.
 * @author zoharC
 * Date: 03-24-2021
 *
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class CrawlerVO {
	
	private final String page;
	private final Set<String> links;
	private final Set<String> staticAssets;//not immutable
}
