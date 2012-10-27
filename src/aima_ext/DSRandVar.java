package aima_ext;

import aima.core.probability.RandomVariable;
import junit.framework.Assert;
import aima.core.probability.domain.ArbitraryTokenDomain;
import aima.core.probability.domain.Domain;
import aima.core.probability.domain.FiniteDomain;
import java.util.*;

import org.junit.Test;

import com.rits.cloning.Cloner;

public class DSRandVar implements RandomVariable { // and TermProposition?
	FiniteDomain domain;
	Map<Set<String>, SubsetInfo> subsetMap;
	String name;
	
	public DSRandVar(String name, Map<Set<String>, SubsetInfo> subsetMap) { // ArbitraryTokenDomain is nice
		this.name = name;
		// get the singleton subsets (the ones with a single event)
		Set<String> singleVals = new HashSet<String>();
		for (Set<String> subset : subsetMap.keySet())
			if (subset.size() == 1)
				singleVals.addAll(subset);
		this.domain = new ArbitraryTokenDomain(singleVals);
	}
	
	@Override
	public String getName() {
		return name;
	}
	@Override
	public Domain getDomain() {
		return domain;
	}
	
	/** Returns the power set of the original set (members are deep copied) */
	public static <T> Set<Set<T>> powerSet(final Set<T> origSet) {
		if (origSet == null)
			return null;
		Set<Set<T>> PS = new HashSet<Set<T>>((int)Math.pow(2, origSet.size()));
		@SuppressWarnings("unchecked")
		final T[] list = (T[])origSet.toArray();
		for (int i = 0; i <= origSet.size(); i++)
			subComb(PS, list, new HashSet<T>(), 0, i, new Cloner());
		return PS;
	}
	
	private static <T> void subComb(Set<Set<T>> PS, final T[] list, Set<T> sofar, int pos, int numRem, final Cloner cloner) {
		if (numRem == 0)
			PS.add(cloner.deepClone(sofar)); // deep copy??
		else {
			for (int i = pos; i < list.length-(numRem-1); i++) {
				sofar.add(list[i]);
				subComb(PS, list, sofar, i+1, numRem-1, cloner);
				sofar.remove(list[i]);
			}
		}
	}
	
}
