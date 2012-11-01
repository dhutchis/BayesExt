package test_aima_ext;

import static org.junit.Assert.*;
import aima_ext.DSRandVar;
import aima_ext.DSUtil;
import aima_ext.SubsetInfo;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
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
	public void testSaveLoadFile() throws IOException {
		DSRandVar rv = new DSRandVar("Murderer", singleEvents, true);
		rv.saveToFile("testfile1.csv");
		DSRandVar loaded = DSRandVar.loadFromFile("testfile1.csv");
//		System.out.println("origin["+rv.hashCode()+"]: "+rv);
//		System.out.println("loaded["+loaded.hashCode()+"]: "+loaded);
//		System.out.println(rv.equals( loaded));
//		System.out.println(rv.hashCode() == loaded.hashCode());
		assertEquals(loaded, rv);
	}
	
	@Test
	public void testPropagateMassToLikelihoodAndBeliefPlausability() {
		Map<Set, SubsetInfo> map1 = DSRandVar.createDefaultMassMap(singleEvents, false);
		map1.get(DSUtil.formSetFromObjects(peter)).mass = 0.2;
		map1.get(DSUtil.formSetFromObjects(peter,paul)).mass = 0.3;
		map1.get(singleEvents).mass = 0.5;
		DSRandVar rv1 = new DSRandVar("RVtl",map1);
		rv1.propagateMassToBelPl();
		rv1.propagateMassToLikelihood();
		
		Map<Set, SubsetInfo> map2 = rv1.getUnmodifiablePowersetMap();
		assertEquals(map2.get(Collections.EMPTY_SET).likely, -1, 0.00001);
		assertEquals(map2.get(Collections.EMPTY_SET).mass, 0, 0.00001);
		assertEquals(map2.get(Collections.EMPTY_SET).belief, 0, 0.00001);
		assertEquals(map2.get(Collections.EMPTY_SET).plausability, 0.0, 0.00001);
		
		assertEquals(map2.get(Collections.singleton(peter)).likely, .2+.15+.5/3, 0.00001);
		assertEquals(map2.get(Collections.singleton(peter)).mass, 0.2, 0.00001);
		assertEquals(map2.get(Collections.singleton(peter)).belief, 0.2, 0.00001);
		assertEquals(map2.get(Collections.singleton(peter)).plausability, 1.0, 0.00001);

		assertEquals(map2.get(Collections.singleton(paul)).likely, .15+.5/3, 0.00001);
		assertEquals(map2.get(Collections.singleton(paul)).mass, 0.0, 0.00001);
		assertEquals(map2.get(Collections.singleton(paul)).belief, 0.0, 0.00001);
		assertEquals(map2.get(Collections.singleton(paul)).plausability, 0.8, 0.00001);
		
		assertEquals(map2.get(Collections.singleton(mary)).likely, .5/3, 0.00001);
		assertEquals(map2.get(Collections.singleton(mary)).mass, 0.0, 0.00001);
		assertEquals(map2.get(Collections.singleton(mary)).belief, 0.0, 0.00001);
		assertEquals(map2.get(Collections.singleton(mary)).plausability, 0.5, 0.00001);
		
		assertEquals(map2.get(DSUtil.formSetFromObjects(peter,paul)).likely, -1, 0.00001);
		assertEquals(map2.get(DSUtil.formSetFromObjects(peter,paul)).mass, 0.3, 0.00001);
		assertEquals(map2.get(DSUtil.formSetFromObjects(peter,paul)).belief, 0.5, 0.00001);
		assertEquals(map2.get(DSUtil.formSetFromObjects(peter,paul)).plausability, 1.0, 0.00001);
		
		assertEquals(map2.get(DSUtil.formSetFromObjects(paul,mary)).likely, -1, 0.00001);
		assertEquals(map2.get(DSUtil.formSetFromObjects(paul,mary)).mass, 0.0, 0.00001);
		assertEquals(map2.get(DSUtil.formSetFromObjects(paul,mary)).belief, 0.0, 0.00001);
		assertEquals(map2.get(DSUtil.formSetFromObjects(paul,mary)).plausability, 0.8, 0.00001);
		
		assertEquals(map2.get(DSUtil.formSetFromObjects(peter,mary)).likely, -1, 0.00001);
		assertEquals(map2.get(DSUtil.formSetFromObjects(peter,mary)).mass, 0.0, 0.00001);
		assertEquals(map2.get(DSUtil.formSetFromObjects(peter,mary)).belief, 0.2, 0.00001);
		assertEquals(map2.get(DSUtil.formSetFromObjects(peter,mary)).plausability, 1.0, 0.00001);
		
		assertEquals(map2.get(singleEvents).likely, -1, 0.00001);
		assertEquals(map2.get(singleEvents).mass, 0.5, 0.00001);
		assertEquals(map2.get(singleEvents).belief, 1.0, 0.00001);
		assertEquals(map2.get(singleEvents).plausability, 1.0, 0.00001);
	}
	
	@Test
	public void testCombineEvidence() throws IOException {
		// setup rv1
		Map<Set, SubsetInfo> map1 = DSRandVar.createDefaultMassMap(singleEvents, false);
		map1.get(Collections.singleton(mary)).mass = 0.5;
		map1.get(DSUtil.formSetFromObjects(peter,paul)).mass = 0.5;
		
		// setup rv2
		Map<Set, SubsetInfo> map2 = DSRandVar.createDefaultMassMap(singleEvents, false);
		map2.get(DSUtil.formSetFromObjects(paul,mary)).mass = 1.0;
		
		DSRandVar rv1 = new DSRandVar("E1", map1); rv1.propagateMassToLikelihood();
		DSRandVar rv2 = new DSRandVar("E2", map2); rv2.propagateMassToLikelihood();
		
		DSRandVar rv12 = rv1.combineWith(rv2);
		
		// check it
		Map<Set, SubsetInfo> map12 = DSRandVar.createDefaultMassMap(singleEvents, false);
		map12.get(Collections.singleton(paul)).mass = 0.5;
		map12.get(Collections.singleton(mary)).mass = 0.5;
		assertEquals(rv12, new DSRandVar("E1+E2",map12));
		
		rv1.saveToFile("rv1.csv");
		rv2.saveToFile("rv2.csv");
		rv12.saveToFile("rv12.csv");
		
		rv12.propagateMassToBelPl();
		System.out.println(rv12.toFixedWidthString(true, true));
		
		Map<Set, SubsetInfo> map12_combo = rv12.getUnmodifiablePowersetMap();
		assertEquals(map12_combo.get(Collections.EMPTY_SET).likely, -1, 0.00001);
		assertEquals(map12_combo.get(Collections.EMPTY_SET).mass, 0, 0.00001);
		assertEquals(map12_combo.get(Collections.EMPTY_SET).belief, 0, 0.00001);
		assertEquals(map12_combo.get(Collections.EMPTY_SET).plausability, 0.0, 0.00001);
		
		assertEquals(map12_combo.get(Collections.singleton(peter)).likely, 0.0, 0.00001);
		assertEquals(map12_combo.get(Collections.singleton(peter)).mass, 0.0, 0.00001);
		assertEquals(map12_combo.get(Collections.singleton(peter)).belief, 0.0, 0.00001);
		assertEquals(map12_combo.get(Collections.singleton(peter)).plausability, 0.0, 0.00001);

		assertEquals(map12_combo.get(Collections.singleton(paul)).likely,1.0/3, 0.00001);
		assertEquals(map12_combo.get(Collections.singleton(paul)).mass, 0.5, 0.00001);
		assertEquals(map12_combo.get(Collections.singleton(paul)).belief, 0.5, 0.00001);
		assertEquals(map12_combo.get(Collections.singleton(paul)).plausability, 0.5, 0.00001);
		
		assertEquals(map12_combo.get(Collections.singleton(mary)).likely, 2.0/3, 0.00001);
		assertEquals(map12_combo.get(Collections.singleton(mary)).mass, 0.5, 0.00001);
		assertEquals(map12_combo.get(Collections.singleton(mary)).belief, 0.5, 0.00001);
		assertEquals(map12_combo.get(Collections.singleton(mary)).plausability, 0.5, 0.00001);
		
		assertEquals(map12_combo.get(DSUtil.formSetFromObjects(peter,paul)).likely, -1, 0.00001);
		assertEquals(map12_combo.get(DSUtil.formSetFromObjects(peter,paul)).mass, 0.0, 0.00001);
		assertEquals(map12_combo.get(DSUtil.formSetFromObjects(peter,paul)).belief, 0.5, 0.00001);
		assertEquals(map12_combo.get(DSUtil.formSetFromObjects(peter,paul)).plausability, 0.5, 0.00001);
		
		assertEquals(map12_combo.get(DSUtil.formSetFromObjects(paul,mary)).likely, -1, 0.00001);
		assertEquals(map12_combo.get(DSUtil.formSetFromObjects(paul,mary)).mass, 0.0, 0.00001);
		assertEquals(map12_combo.get(DSUtil.formSetFromObjects(paul,mary)).belief, 1.0, 0.00001);
		assertEquals(map12_combo.get(DSUtil.formSetFromObjects(paul,mary)).plausability, 1.0, 0.00001);
		
		assertEquals(map12_combo.get(DSUtil.formSetFromObjects(peter,mary)).likely, -1, 0.00001);
		assertEquals(map12_combo.get(DSUtil.formSetFromObjects(peter,mary)).mass, 0.0, 0.00001);
		assertEquals(map12_combo.get(DSUtil.formSetFromObjects(peter,mary)).belief, 0.5, 0.00001);
		assertEquals(map12_combo.get(DSUtil.formSetFromObjects(peter,mary)).plausability, 0.5, 0.00001);
		
		assertEquals(map12_combo.get(singleEvents).likely, -1, 0.00001);
		assertEquals(map12_combo.get(singleEvents).mass, 0.0, 0.00001);
		assertEquals(map12_combo.get(singleEvents).belief, 1.0, 0.00001);
		assertEquals(map12_combo.get(singleEvents).plausability, 1.0, 0.00001);
	}
	
	@Test
	public void testWeakenEvidence() throws IOException {
		// setup rv1
		Map<Set, SubsetInfo> map1 = DSRandVar.createDefaultMassMap(singleEvents, false);
		map1.get(Collections.singleton(mary)).mass = 0.25;
		map1.get(DSUtil.formSetFromObjects(peter,paul)).mass = 0.75;
		
		DSRandVar rv1 = new DSRandVar("E1", map1);
		rv1.weakenByCertainty(0.6);
		rv1.propagateMassToLikelihood();
		
		Map<Set,SubsetInfo> map_weak = rv1.getUnmodifiablePowersetMap();
		assertEquals(0.15, map_weak.get(Collections.singleton(mary)).mass, 0.00001);
		assertEquals(0.45, map_weak.get(DSUtil.formSetFromObjects(peter, paul)).mass, 0.00001);
		assertEquals(0.4, map_weak.get(singleEvents).mass, 0.00001);
		
		assertEquals(17.0/60, map_weak.get(Collections.singleton(mary)).likely, 0.00001);
		assertEquals(43.0/120, map_weak.get(Collections.singleton(peter)).likely, 0.00001);
		assertEquals(43.0/120, map_weak.get(Collections.singleton(paul)).likely, 0.00001);
		
		
	}

}
