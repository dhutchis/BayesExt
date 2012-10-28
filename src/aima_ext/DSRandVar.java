package aima_ext;

import aima.core.probability.RandomVariable;
import aima.core.probability.domain.ArbitraryTokenDomain;
import aima.core.probability.domain.Domain;
import aima.core.probability.domain.FiniteDomain;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.rits.cloning.Cloner;

@SuppressWarnings({ "unchecked", "rawtypes" }) // this typing is really annoying! 
public class DSRandVar implements RandomVariable { // and TermProposition?
	final FiniteDomain domain; // the single events that make up this random variable
				// ultimately this can only take on one of the values in the domain
	final Map<Set, SubsetInfo> powersetMap;
	final String name;
	
	/** Creates a default powerset mapping every subset to zero mass or, if startWithVacuous=true, just the full set to 1 */
	public static  Map<Set, SubsetInfo> createDefaultMassMap(final Set singleEvents, boolean startWithVacuous) {
		Map<Set, SubsetInfo> powersetMap = new HashMap<Set, SubsetInfo>();
		System.out.println("singleEvents: "+singleEvents);
		Set<Set> PS = (Set<Set>)DSUtil.powerSet(singleEvents);
		for (Set subset : PS) {
			SubsetInfo si = new SubsetInfo();
			if (startWithVacuous && subset.equals(singleEvents))
				si.mass = 1;
			powersetMap.put(subset, si);
		}
		assert(SubsetInfo.verifyValidMass(powersetMap.values()));
		return powersetMap;
	}
	
	/** Makes a new ArbitraryTokenDomain out of the single events */
	public DSRandVar(String name, Set singleEvents, boolean startWithVacuous) {
		this(name, new ArbitraryTokenDomain(singleEvents.toArray()), startWithVacuous);
	}
	
	/** construct a default DS distribution (all prob. mass in the full set) */
	public DSRandVar(String name, FiniteDomain domain, boolean startWithVacuous) {
		this.name = name;
		this.domain = domain;
		this.powersetMap = DSRandVar.createDefaultMassMap(domain.getPossibleValues(), startWithVacuous);
		System.out.println("default powerset: "+powersetMap);
	}
	
	/** Construct from an already laid out mapping */
	public DSRandVar(String name, Map<Set, SubsetInfo> subsetMap) { // ArbitraryTokenDomain is nice
		if (!SubsetInfo.verifyValidMass(subsetMap.values()))
			throw new IllegalArgumentException("Invalide mass distribution for "+name+" on "+subsetMap);
		this.name = name;
		this.powersetMap = (new Cloner()).deepClone(subsetMap);
		// get the singleton subsets (the ones with a single event)
		Set singleVals = new HashSet<Object>();
		for (Set subset : this.powersetMap.keySet())
			if (subset.size() == 1)
				singleVals.addAll( subset);
		this.domain = new ArbitraryTokenDomain(singleVals.toArray());
	}
	
//	/* Generates a template file we can use to fill in probability masses */
//	public static void generateTemplateFile(String filename, Set singleEvents)
	// just do	DSRandVar rv = new DSRandVar("name", Set { varA, varB, ... });
	//			rv.saveToFile(filename);
	// and modify
	
	public static DSRandVar loadFromFile(String filename) throws IOException {
		FileReader fr = new FileReader(filename);
		// read the name of the random var
		String name;
		{
			StringBuffer sb = new StringBuffer();
			int c;
			while ((c = fr.read()) != '\n')
				sb.appendCodePoint(c);
			name = sb.toString();
		}
		Map<Set, SubsetInfo> subsetMap = new HashMap<Set, SubsetInfo>();
		CSVReader reader = new CSVReader(fr);
	    String [] nextLine;
	    while ((nextLine = reader.readNext()) != null) {
	        Set<String> subset = DSRandVar.stringToStringSet(nextLine[0]);
	        SubsetInfo si = new SubsetInfo();
	        si.mass = Double.parseDouble(nextLine[1]);
	        subsetMap.put(subset, si);
	    }
	    reader.close();
	    return new DSRandVar(name, subsetMap);
	}
	
	public String toFixedWidthString(boolean includeHeader, boolean includeAll) {
		// first, get the max length of the subset
		int maxSubsetLength = powersetMap.get(domain.getPossibleValues()).toString().length();
		StringBuilder sb = new StringBuilder();
	    Formatter formatter = new Formatter(sb); // Send all output to the Appendable object sb
	    String format;
	    Object[] args;
	    if (includeAll) {
	    	if (includeHeader)
	    		formatter.format("%"+maxSubsetLength+"s mass  bel   plaus\n", "subset");
	    	format = "%"+maxSubsetLength+"s %5.3f %5.3f %5.3f\n";
	    }
	    else {
	    	if (includeHeader)
	    		formatter.format("%"+maxSubsetLength+"s mass\n", "subset");
    		format = "%"+maxSubsetLength+"s %5.3f\n";
	    }			
	    
		List<Entry<Set,SubsetInfo>> sortedEntryList = new ArrayList<Entry<Set,SubsetInfo>>(powersetMap.entrySet());
		Collections.sort(sortedEntryList, new ComparatorByFirstEntry());
		for (Entry<Set,SubsetInfo> entry : sortedEntryList) {
			if (includeAll)
				args = new Object[] { DSRandVar.collectionToString(entry.getKey()), entry.getValue().mass, entry.getValue().belief, entry.getValue().plausability };
			else
				args = new Object[] { DSRandVar.collectionToString(entry.getKey()), entry.getValue().mass };
			formatter.format(format, args);
		}
		formatter.close();
		return sb.toString();
	}
	
	
	public void saveToFile(String filename) throws IOException {
		FileWriter fw = new FileWriter(filename);
		// write the name of the random var
		fw.write(this.name+"\n");
		CSVWriter writer = new CSVWriter(fw);
		List<Entry<Set,SubsetInfo>> sortedEntryList = new ArrayList<Entry<Set,SubsetInfo>>(powersetMap.entrySet());
		Collections.sort(sortedEntryList, new ComparatorByFirstEntry());
		for (Entry<Set,SubsetInfo> entry : sortedEntryList) {
			String[] row = new String[2];
			row[0] = DSRandVar.collectionToString(entry.getKey());
			row[1] = String.valueOf(entry.getValue().mass);
			writer.writeNext(row);
		}
		writer.close();
	}
	
	static String collectionToString(Collection<?> col) {
		StringBuffer sb = new StringBuffer("[");
		for (Iterator<?> iterator = col.iterator(); iterator.hasNext(); ) {
			Object o = iterator.next();
			sb.append(o.toString());
			if (iterator.hasNext())
				sb.append(", ");
		}
		sb.append(']');
		return sb.toString();
	}
	
	// Don't deal with this now.  Too generic!  And java can't use function pointers.
	// Settle with just parsing string sets from the file.
//	static interface stringParser<T> {
//		public T parseString
//	}
//	static <T> T stringToObjectCollection(
	
	static Set<String> stringToStringSet(String item) {
		Set<String> ss = new HashSet<String>();
		assert(item.charAt(0) == '[' && item.charAt(item.length()) == ']');
		String middle = item.substring(1, item.length()-1);
		String[] parts = middle.split(",");
		for (String part : parts) {
			ss.add(part.trim());
		}
		return ss;
	}
	
	static class ComparatorByCardinality<T extends Collection<?>> implements Comparator<T> {
		@Override
		public int compare(T o1, T o2) {
			return o1.size() - o2.size();
		}
	}
	
	static class ComparatorByFirstEntry<T extends Entry<? extends Collection<?>,?>> implements Comparator<T> {
		@Override
		public int compare(T o1, T o2) {
			return o1.getKey().size() - o2.getKey().size();
		}		
	}
	
	
	
	@Override
	public String getName() {
		return name;
	}
	@Override
	public Domain getDomain() {
		return domain;
	}
	@Override
	public String toString() {
		return "DSRandVar["+name+"]"+(powersetMap);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((powersetMap == null) ? 0 : powersetMap.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DSRandVar))
			return false;
		DSRandVar other = (DSRandVar) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (powersetMap == null) {
			if (other.powersetMap != null)
				return false;
		} else if (powersetMap.hashCode() != (other.powersetMap.hashCode())) // ??
			return false;
		return true;
	}

	private Set getLocalUniverse() {
		Set u = new HashSet();
		u.addAll( domain.getPossibleValues() );
		return u;
	}
	
	/** Propagates the mass to the belief and plausibility values */
	public void propagateMass() {
		List<Entry<Set,SubsetInfo>> sortedEntryList = new ArrayList<Entry<Set,SubsetInfo>>(powersetMap.entrySet());
		Collections.sort(sortedEntryList, new ComparatorByFirstEntry());
		
		// belief
		for (int i = 0; i < sortedEntryList.size(); i++) {
			Entry<Set,SubsetInfo> entryToProp = sortedEntryList.get(i);
			double mass = entryToProp.getValue().mass;
			if (mass == 0) // nothing to propagate
				continue;
			Set setToProp = entryToProp.getKey();
			// propagate the mass in this set to all supersets (don't have to look at past sets because we order by cardinality)
			for (int j = i; j < sortedEntryList.size(); j++) {
				Entry<Set,SubsetInfo> entry2 = sortedEntryList.get(j);
				if (entry2.getKey().containsAll(setToProp))
					entry2.getValue().belief += mass;
			}
		}
		
		// plausibility
		for (Entry<Set,SubsetInfo> entry : sortedEntryList) {
			Set complement = this.getLocalUniverse();
			complement.removeAll( entry.getKey() );
			entry.getValue().plausability = 1 - powersetMap.get(complement).belief;
		}
	}
	
	/** Use Dempster's Rule of Combination - returns null if incompatible (different domains or totally conflicting evidence) */
	public DSRandVar combineWith(DSRandVar rv2) {
		Cloner cloner = new Cloner();
		if (!this.domain.equals(rv2.domain))
			return null;
		// for each entry in powersetMap, combine it with each entry in rv2 powersetMap
		double nullvalue = 0.0;
		System.out.println("make rvnew");
		DSRandVar rvnew = new DSRandVar(this.name+"+"+rv2.name, this.domain, false);
		for (Entry<Set,SubsetInfo> entry1 : this.powersetMap.entrySet()) {
			double m1 = entry1.getValue().mass;
			if (m1 == 0) continue;
			for (Entry<Set,SubsetInfo> entry2 : rv2.powersetMap.entrySet()) {
				double m2 = entry2.getValue().mass;
				if (m2 == 0) continue;
				Set intersection = cloner.deepClone( entry1.getKey() );
				intersection.retainAll(entry2.getKey());
				if (intersection.size() == 0)
					nullvalue += m1 * m2;
				else {
					System.out.println("inter: "+intersection+"; get: "+rvnew.powersetMap.get(intersection)+"; power before: "+rvnew.powersetMap);
					rvnew.powersetMap.get(intersection).mass += m1 * m2;
				}
			}
		}
		// now normalize
		for (SubsetInfo si : rvnew.powersetMap.values()) {
			si.mass /= (1-nullvalue);
		}
		assert (SubsetInfo.verifyValidMass(rvnew.powersetMap.values()));
		return rvnew;
	}
	
}
