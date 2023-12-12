package com.rodini.ballotgen.placeholder;

import org.docx4j.wml.P;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** 
 * Placeholder is a simple value object that represents a found
 * placeholder reference in one of the document parts.
 * 
 * @author Bob Rodini
 */
public class Placeholder {
	private static final Logger logger = LogManager.getLogger(Placeholder.class);
	final String name;
	final P replaceParagraph;
	final PlaceholderLocation loc;
	/**
	 * constructor
	 * Note: the object is immutable.
	 * @param name of placeholder
	 * @param loc see PlaceholderLocation
	 * @param replaceParagraph paragraph (P) that contains name (Text)
	 */
	Placeholder(String name, PlaceholderLocation loc, P replaceParagraph) {
		this.name = name;
		this.replaceParagraph = replaceParagraph;
		this.loc = loc;
		logger.debug("Placeholder created: " + toString());
	}
	/**
	 * getNane gets name.
	 * @return placeholder name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * getReplaceParagraph gets the paragraph.
	 * @return placeholder paragraph (P).
	 */
	public P getReplaceParagraph() {
		return replaceParagraph;
	}
	/**
	 * getLoc gets the location.
	 * @return placeholder location.
	 */
	public PlaceholderLocation getLoc() {
		return loc;
	}
	@Override
	public String toString() {
		return "Placeholder: name:" + name + " replaceParagraph:" + replaceParagraph + " loc:" + loc;
	}


}
