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
		//System.out.println("singleEvents: "+singleEvents);
		Set<Set> PS = (Set<Set>)DSUtil.powerSet(singleEvents);
		for (Set subset : PS) {
			SubsetInfo si = new SubsetInfo();
			if (startWithVacuous && subset.equals(singleEvents))
				si.mass = 1;
			if (subset.size() == 1)
				si.prob = 1;
			powersetMap.put(subset, si);
		}
		assert(!startWithVacuous || SubsetInfo.verifyValidMass(powersetMap.values()));
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
		//System.out.println("default powerset: "+powersetMap);
	}
	
	/** Copy constructor (does not alias) */
	public DSRandVar(DSRandVar other) {
		this(other.name, other.powersetMap);
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
		
//		SubsetInfo si = this.powersetMap.get(domain.getPossibleValues());
//		System.out.println(si);
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
	    while ((nextLine = reader.readNext()) != null && nextLine.length > 1) {
	        Set<String> subset = DSRandVar.stringToStringSet(nextLine[0]);
	        SubsetInfo si = new SubsetInfo();
	        si.mass = Double.parseDouble(nextLine[1]);
	        if (nextLine.length > 2) {
	        	if (subset.size() != 1)
	        		System.err.println("Warning: ignoring probability associated with "+subset);
	        	else
	        		si.prob = Double.parseDouble(nextLine[2]);
	        }
	        else if (subset.size() == 1)
	        	si.prob = 1;
	        subsetMap.put(subset, si);
	    }
	    reader.close();
	    return new DSRandVar(name, subsetMap);
	}
	
	public String toFixedWidthString(boolean includeHeader, boolean includeAll) {
		return toFixedWidthString(includeHeader, includeAll, false);
	}
	
	public String toFixedWidthString(boolean includeHeader, boolean includeAll, boolean latex) {
		// first, get the max length of the subset
		int maxSubsetLength = powersetMap.get(domain.getPossibleValues()).toString().length();
		StringBuilder sb = new StringBuilder();
	    Formatter formatter = new Formatter(sb); // Send all output to the Appendable object sb
	    String format;
	    Object[] args;
	    char colsep = latex ? '&' : ' ';
	    if (latex)
			sb.append(
					"\\begin{table}[htbp]\n"
					  +"\\centering\n"
					  +"%\\caption{Add caption}\n"
					    +"\\begin{tabular}{"+(includeAll ? "rllll" : "rc")+"}\n"
					+"\\toprule\n"    
					
					);
	    if (includeAll) {
	    	if (includeHeader)
	    		formatter.format("%"+maxSubsetLength+"s"+colsep+"mass "+colsep+"bel  "+colsep+"Prob "+colsep+"plaus"+(latex ? "\\\\":"")+'\n', "subset");
	    	format = "%"+maxSubsetLength+"s"+colsep+"%5.3f"+colsep+"%5.3f"+colsep+"%5.3f"+colsep+"%5.3f"+(latex ? "\\\\":"")+'\n';
	    }
	    else {
	    	if (includeHeader)
	    		formatter.format("%"+maxSubsetLength+"s"+colsep+"mass"+(latex ? "\\\\":"")+'\n', "subset");
    		format = "%"+maxSubsetLength+"s"+colsep+"%5.3f"+(latex ? "\\\\":"")+'\n';
	    }			
	    if (latex) sb.append("\\midrule\n");
		List<Entry<Set,SubsetInfo>> sortedEntryList = new ArrayList<Entry<Set,SubsetInfo>>(powersetMap.entrySet());
		Collections.sort(sortedEntryList, new ComparatorByFirstEntry());
		for (Entry<Set,SubsetInfo> entry : sortedEntryList) {
			if (includeAll)
				args = new Object[] { DSRandVar.collectionToString(entry.getKey()), entry.getValue().mass, entry.getValue().belief, entry.getValue().prob,
					entry.getValue().plausability };
			else
				args = new Object[] { DSRandVar.collectionToString(entry.getKey()), entry.getValue().mass };
			formatter.format(format, args);
		}
		formatter.close();
		if (latex)
			sb.append(
					"\\bottomrule\n"
				    +"\\end{tabular}\n"
				  +"%\\label{tab:addlabel}\n"
				+"\\end{table}\n"
					);
		if (latex) sb.replace(sb.indexOf("\\toprule"), sb.length(), 
				sb.substring(sb.indexOf("\\toprule")).replaceAll("\\[", "{[").replaceAll("\\]","]}") );
		String s = sb.toString().replaceAll("0.000", "0    ").replaceAll("-1.000", "     ").replaceAll("1.000", "1    ");
		//if (latex) s = s.replaceAll("\\[", "\\\\left[").replaceAll("\\]","\\\\right]");
		return s;
	}
	
	/** File format:
	 * VarName
	 * SubsetList,mass[,likely]
	 * ...
	 * SubsetLlist is a list of Objects ex. [Peter,Paul]
	 * mass is a double
	 * likely is a double stored only for singleton subsets (likelihood ratio; sometimes probability if they sum to 1)
	 *  */
	public void saveToFile(String filename) throws IOException {
		FileWriter fw = new FileWriter(filename);
		// write the name of the random var
		fw.write(this.name+"\n");
		CSVWriter writer = new CSVWriter(fw);
		List<Entry<Set,SubsetInfo>> sortedEntryList = new ArrayList<Entry<Set,SubsetInfo>>(powersetMap.entrySet());
		Collections.sort(sortedEntryList, new ComparatorByFirstEntry());
		for (Entry<Set,SubsetInfo> entry : sortedEntryList) {
			String[] row = entry.getKey().size() == 1 ? new String[3] : new String[2];
			row[0] = DSRandVar.collectionToString(entry.getKey());
			row[1] = String.valueOf(entry.getValue().mass);
			if (entry.getKey().size() == 1) row[2] = String.valueOf(entry.getValue().prob);
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
		if (item.equals("[]"))
			return Collections.EMPTY_SET;
		Set<String> ss = new HashSet<String>();
		assert(item.charAt(0) == '[' && item.charAt(item.length()-1) == ']');
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
	
	public Map<Set, SubsetInfo> getUnmodifiablePowersetMap() {
		return Collections.unmodifiableMap(powersetMap);
	}

	private Set getLocalUniverse() {
		Set u = new HashSet();
		u.addAll( domain.getPossibleValues() );
		return u;
	}
	
	/** Propagates the mass to the belief and plausibility values */
	public void propagateMassToBelPl() {
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
	
	/** Creates the likelihood generated by the current mass (will destroy previous likelihood) 
	 * Divide the mass in each subset by the subset size and then distribute to the singleton components.
	 * */
	public void propagateMassToLikelihood() {
		assert(SubsetInfo.verifyValidMass(powersetMap.values())); 
		Set subsetOfSingletons = domain.getPossibleValues();
		List<SubsetInfo> siSet = new LinkedList<SubsetInfo>(); // list not set to allow duplicates (items that are equal but not ==)
		double likelyTotal=0;
		for (Object singleObj : subsetOfSingletons) {
			double likelyCur=0;
			SubsetInfo si=null;
			for (Entry<Set,SubsetInfo> entry : powersetMap.entrySet()) {
				if (entry.getKey().contains(singleObj)) {
					if (entry.getKey().size() == 1)
						si = entry.getValue();
					likelyCur += entry.getValue().mass / entry.getKey().size(); // key calculation!
				}
			}
			assert (si != null);
			si.prob = likelyCur;
			siSet.add(si);
			likelyTotal += likelyCur;
		}
		// normalize - not required, but why not =)
		for (SubsetInfo si : siSet) // DON'T NEED TO NORMALIZE ANYMORE - MAKE SURE BEFORE REMOVING
			si.prob /= likelyTotal;
	}
	
	/** Change likelihoods to probabilities by dividing by the sum of likelihoods */
	public void normalizeLikelihood() {
		Set subsetOfSingletons = domain.getPossibleValues();
		List<SubsetInfo> siSet = new LinkedList<SubsetInfo>();
		double likelyTotal = 0;
		for (Object singleObj : subsetOfSingletons) {
			SubsetInfo si = powersetMap.get(Collections.singleton(singleObj));
			siSet.add(si);
			likelyTotal += si.prob;
		}
		for (SubsetInfo si : siSet)
			si.prob /= likelyTotal;
	}
	
	/** I wonder... are the likelihoods generated by the masses in sync with the likelihoods normally combined? YES - should be okay */
	public boolean checkMassLikelyCompatible() {
		// first normalize the likelihoods for comparison
		this.normalizeLikelihood();
		DSRandVar other = new DSRandVar(this);
		other.propagateMassToLikelihood();
		boolean compat = this.equals(other);
		return compat;
	}
	
	/** Multiply every mass by c.  Add (1-c) to the universe subset. */
	public void weakenByCertainty(double c) {
		Set universeSubset = this.getLocalUniverse();
		for (Entry<Set,SubsetInfo> entry : this.powersetMap.entrySet()) {
			if (entry.getKey().equals(universeSubset))
				entry.getValue().mass += 1-c;
			else
				entry.getValue().mass *= c;
		}
		assert (SubsetInfo.verifyValidMass(powersetMap.values())); 
	}
	
	/** Use Dempster's Rule of Combination - returns null if incompatible (different domains or totally conflicting evidence) */
	public DSRandVar combineWith(DSRandVar rv2) {
		assert (SubsetInfo.verifyValidMass(powersetMap.values())); 
		assert (SubsetInfo.verifyValidMass(rv2.powersetMap.values()));
		Cloner cloner = new Cloner();
		if (!this.domain.equals(rv2.domain))
			return null;
		// for each entry in powersetMap, combine it with each entry in rv2 powersetMap
		double nullvalue = 0.0;
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
					//System.out.println("inter: "+intersection+"; get: "+rvnew.powersetMap.get(intersection)+"; power before: "+rvnew.powersetMap);
					rvnew.powersetMap.get(intersection).mass += m1 * m2;
				}
			}
		}
		// now normalize masses
		for (SubsetInfo si : rvnew.powersetMap.values()) {
			si.mass /= (1-nullvalue);
		}
		
		// let's get the likelihood vector too
		Set subsetOfSingletons = domain.getPossibleValues();
		List<SubsetInfo> siListNew = new LinkedList<SubsetInfo>();
		double likelyTotal = 0;
		for (Object singleObj : subsetOfSingletons) {
			Set singleSet = Collections.singleton(singleObj);
			SubsetInfo si1 = powersetMap.get(singleSet),
					si2 = rv2.powersetMap.get(singleSet),
					sinew = rvnew.powersetMap.get(singleSet);
			sinew.prob = si1.prob * si2.prob;
			siListNew.add(sinew);
			likelyTotal += sinew.prob;
		}
		for (SubsetInfo sinew : siListNew) // normalize likelihood of sinew
			sinew.prob /= likelyTotal;
		
		assert (SubsetInfo.verifyValidMass(rvnew.powersetMap.values()));
		System.out.println("Does the combination "+rvnew.name+" has likelihood-mass compatibility: "+rvnew.checkMassLikelyCompatible());
		return rvnew;
	}
	
}
