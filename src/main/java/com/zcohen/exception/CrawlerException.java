package com.zcohen.exception;

/**
 * A checked exception extension
 * @author zoharC
 * Date: 03-24-2021
 *
 */
public class CrawlerException extends Exception{

	private static final long serialVersionUID = 8927061576685488695L;
	
	public CrawlerException(String msg) {
		super(msg);
	}
}
