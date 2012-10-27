package test_aima_ext;

import static org.junit.Assert.*;
import aima_ext.DSRandVar;
import aima_ext.DSUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

public class UnitTest1 {
	static final String peter = "peter", paul = "paul", mary = "mary";
	static Set<String> singleEvents;
	static {
		singleEvents = new HashSet<String>();
		singleEvents.add(peter);
		singleEvents.add(paul);
		singleEvents.add(mary);
		singleEvents = Collections.unmodifiableSet(singleEvents);
	}

	@Test
	public void testPowerSet() {
		assertEquals(DSUtil.powerSet(null),null);
		assertEquals(DSUtil.powerSet(new HashSet<Object>()), new HashSet<Set<Object>>(Collections.singleton(new HashSet<Object>())));
		
		Set<String>  
				pa_m = new HashSet<String>(2),
				pe_m = new HashSet<String>(2),
				pe_pa = new HashSet<String>(2);
		pa_m.add(mary); pa_m.add(paul);
		pe_m.add(mary); pe_m.add(peter);
		pe_pa.add(peter); pe_pa.add(paul);
		
		Set<Set<String>> subsetSet = new HashSet<Set<String>>();
		subsetSet.add(Collections.<String> emptySet());
		subsetSet.add(Collections.singleton(paul));
		subsetSet.add(Collections.singleton(peter));
		subsetSet.add(Collections.singleton(mary));
		subsetSet.add(pa_m);
		subsetSet.add(pe_m);
		subsetSet.add(pe_pa);
		subsetSet.add(singleEvents); Assert.assertEquals(subsetSet.size(), 8);

		Set<Set<String>> powerSet = DSUtil.powerSet(singleEvents);
		Assert.assertEquals(powerSet, subsetSet);
	}
	
	@Test
	public void saveLoadFile() throws IOException {
		DSRandVar rv = new DSRandVar("Murderer", singleEvents);
		rv.saveToFile("testfile1.csv");
		DSRandVar loaded = DSRandVar.loadFromFile("testfile1.csv");
//		System.out.println("origin["+rv.hashCode()+"]: "+rv);
//		System.out.println("loaded["+loaded.hashCode()+"]: "+loaded);
//		System.out.println(rv.equals( loaded));
//		System.out.println(rv.hashCode() == loaded.hashCode());
		assertEquals(loaded, rv);
	}

}
