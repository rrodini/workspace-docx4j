package com.rodini.contestgen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.ballotutils.Utils;

/**
 * MuniTextExtractor "rips" the text of each page of a 
 * municipal ballot and creates a muniContestsExtractor object.
 * 
 * ATTENTION: This class is highly sensitive to structure of 
 * the municipal ballot.
 * 
 * @author Bob Rodini
 *
 */
public class MuniTextExtractor {
	private static final Logger logger = LoggerFactory.getLogger(MuniTextExtractor.class);
	
	private final String muniName;  // e.g. 640_TREDYFFRIN_W-3
	private final String muniText;  // ballot text for 640_TREDYFFRIN_W-3
	/**
	 * constructor
	 * @param muniName name of municipality.
	 * @param muniText complete municipal ballot text.
	 */
	public MuniTextExtractor(String muniName, String muniText) {
		this.muniName = muniName;
		this.muniText = muniText;
		logger.info("MuniTextExtractor: " + muniName);
		int len = muniText.length();
		logger.info("MuniTextExtractor: " + muniText.substring(0,32) + "..." + muniText.substring(len-32,len));
	}
	/**
	 * Extract the contest(s) text as per the given pattern.
	 * @param pattern compiled pattern for the page.
	 * @return
	 */
	String extractPageText(Pattern pattern) {
		String text = "";
		Matcher m = pattern.matcher(muniText);
		if (!m.find()) {
			String msg = String.format("no match for municipal page. muniName: %s regex: %s",muniName, pattern.pattern());
			logger.error(msg);
			msg = String.format("muniText: %s%n", muniText);
			Utils.logFatalError(msg);
			
		} else {
			try {
				text = m.group("page");
			} catch (Exception e) {
				String msg = e.getMessage();
				logger.error(msg);
			}
		}
		// necessary due to anomolies in VS ballot construction.
		if (!text.isBlank() && !text.endsWith(ContestGen.WRITE_IN)) {
			text = text + ContestGen.WRITE_IN;
		}
		return text;
	}
	/**
	 * extract "rips" the contest(s) text from the municipal ballot text.
	 * @return a MunicipalContestsExtractor object.
	 */
	MuniContestsExtractor extract() {
		String page1Text = "";
		String page2Text = "";
		page1Text = extractPageText(MuniTextMarkers.getPage1Pattern());
		if (MuniTextMarkers.getPageCount() == 2) {
			page2Text = extractPageText(MuniTextMarkers.getPage2Pattern());
		}
		return new MuniContestsExtractor(muniName, page1Text, page2Text);
	}
	/**
	 * getMuniName returns the municipality name.
	 * @return the municipality name.
	 */
	String getMuniName() {
		return muniName;
	}
	
	
}
