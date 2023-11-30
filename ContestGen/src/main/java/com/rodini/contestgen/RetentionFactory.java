package com.rodini.contestgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * RetentionFactory creates Retention objects and maintains a list (retentions)
 * of unique Referendum objects.
 * 
 * @author Bob Rodini
 *
 */
public class RetentionFactory {
	private static final Logger logger = LogManager.getLogger(RetentionFactory.class);

	static List<Retention> retentions = new ArrayList<> ();
	// Disallow instances
	private RetentionFactory() {
	}
	/**
	 * create is given the retention office and judge name extracted from a precinct ballot.
	 * It saves a reference to unique Retention objects.
	 * 
	 * @param officeName judgeship title.
	 * @param judgeName judge name.
	 * 
	 * @return reference of unique Retention object.
	 */
	public static Retention create(String officeName, String judgeName) {
		Retention temp = new Retention(officeName, judgeName);
		int index = retentions.indexOf(temp);
		if (index < 0) {
			retentions.add(temp);
		} else {
			Retention ret = retentions.get(index);
			temp = ret;
		}
		return temp;
	}
	/**
	 * getRetentions returns the list of unique Retention objects.
	 * 
	 * @return list of unique Retention objects.
	 */
	public static List<Retention> getRetentions() {
		return retentions;
	}
	// for testing only
	public static void clearRetentions() {
		retentions.clear();
	}
}
