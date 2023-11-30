package com.rodini.contestgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * ReferendumFactory creates Referendum objects and maintains a list (referendums)
 * of unique Referendum objects.
 * 
 * @author Bob Rodini
 *
 */
public class ReferendumFactory {
	private static final Logger logger = LogManager.getLogger(ReferendumFactory.class);

	private static List<Referendum> referendums = new ArrayList<> ();
	// Disallow instances.
	private ReferendumFactory() {	
	}
	/**
	 * create is given the referendum question and text extracted from a precinct ballot.
	 * It saves a reference to unique Referendum objects along with the precinct # in which
	 * it appears.
	 * 
	 * @param refQuestion question title.
	 * @param refText text of proposition.
	 * @param muniNo precinct {@link #clone()}.
	 * 
	 * @return reference of unique Referendum object.
	 */
	public static Referendum create(String refQuestion, String refText, String muniNo) {
		Referendum temp = new Referendum(refQuestion, refText);
		int index = referendums.indexOf(temp);
		if (index < 0) {
			referendums.add(temp);
			temp.addMuniNo(muniNo);
		} else {
			Referendum ref = referendums.get(index);
			ref.addMuniNo(muniNo);
			temp = ref;
		}
		return temp;
	}
	/**
	 * getReferendums returns the list of unique Referendum objects.
	 * 
	 * @return list of unique Referendum objects.
	 */
	public static List<Referendum> getReferendums() {
		return referendums;
	}
	// for testing only
	public static void clearReferendums() {
		referendums.clear();
	}

}
