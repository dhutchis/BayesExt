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


public class DSRandVar implements RandomVariable { // and TermProposition?
	FiniteDomain domain; // the single events that make up this random variable
				// ultimately this can only take on one of the values in the domain
	Map<Set<?>, SubsetInfo> powersetMap;
	String name;
	
	/** Makes a new ArbitraryTokenDomain out of the single events */
	public DSRandVar(String name, Set<?> singleEvents) {
		this(name, new ArbitraryTokenDomain(singleEvents.toArray()));
	}
	
	/** construct a default DS distribution (all prob. mass in the full set) */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DSRandVar(String name, FiniteDomain domain) {
		this.name = name;
		this.domain = domain;
		this.powersetMap = new HashMap<Set<?>, SubsetInfo>();
		Set singles = domain.getPossibleValues();
		Set<Set> PS = (Set<Set>)DSUtil.powerSet(singles);
		for (Set subset : PS) {
			SubsetInfo si = new SubsetInfo();
			if (subset.equals(singles))
				si.mass = 1;
			this.powersetMap.put(subset, si);
		}
	}
	
	/** Construct from an already laid out mapping */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	DSRandVar(String name, Map<Set<?>, SubsetInfo> subsetMap) { // ArbitraryTokenDomain is nice
		if (!SubsetInfo.verifyValidMass(subsetMap.values()))
			throw new IllegalArgumentException("Invalide mass distribution for "+name+" on "+subsetMap);
		this.name = name;
		this.powersetMap = subsetMap;
		// get the singleton subsets (the ones with a single event)
		Set<?> singleVals = new HashSet<Object>();
		for (Set subset : subsetMap.keySet())
			if (subset.size() == 1)
				singleVals.addAll( subset);
		this.domain = new ArbitraryTokenDomain(singleVals);
	}
	
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
		Map<Set<?>, SubsetInfo> subsetMap = new HashMap<Set<?>, SubsetInfo>();
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
	
	//
	// INSTANCE METHODS
	//
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void saveToFile(String filename) throws IOException {
		FileWriter fw = new FileWriter(filename);
		// write the name of the random var
		fw.write(this.name+"\n");
		CSVWriter writer = new CSVWriter(fw);
		List<Entry<Set<?>,SubsetInfo>> sortedEntryList = new ArrayList<Entry<Set<?>,SubsetInfo>>(powersetMap.entrySet());
		Collections.sort(sortedEntryList, new ComparatorByFirstEntry());
		for (Entry<Set<?>,SubsetInfo> entry : sortedEntryList) {
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

	
}
