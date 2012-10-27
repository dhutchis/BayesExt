package test_aima_ext;

import static org.junit.Assert.*;
import aima_ext.DSRandVar;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

public class UnitTest1 {

	@Test
	public void testPowerSet() {
		assertEquals(DSRandVar.powerSet(null),null);
		assertEquals(DSRandVar.powerSet(new HashSet<Object>()), new HashSet<Set<Object>>(Collections.singleton(new HashSet<Object>())));
		
		Set<String> peter = Collections.singleton("Peter"),
				paul = Collections.singleton("Paul"),
				mary = Collections.singleton("Mary"),
				pa_m = new HashSet<String>(2),
				pe_m = new HashSet<String>(2),
				pe_pa = new HashSet<String>(2),
				all = new HashSet<String>(3);
		pa_m.addAll(mary); pa_m.addAll(paul);
		pe_m.addAll(mary); pe_m.addAll(peter);
		pe_pa.addAll(peter); pe_pa.addAll(paul);
		all.addAll(mary); all.addAll(paul); all.addAll(peter);
		
		Set<Set<String>> subsetSet = new HashSet<Set<String>>();
		subsetSet.add(new HashSet<String>()); // empty
		subsetSet.add(paul);
		subsetSet.add(peter);
		subsetSet.add(mary);
		subsetSet.add(pa_m);
		subsetSet.add(pe_m);
		subsetSet.add(pe_pa);
		subsetSet.add(all); Assert.assertEquals(subsetSet.size(), 8);

		Set<Set<String>> powerSet = DSRandVar.powerSet(all);
		System.out.println(powerSet);
		Assert.assertEquals(powerSet, subsetSet);
	}

}
