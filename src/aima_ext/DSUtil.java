package aima_ext;

import java.util.HashSet;
import java.util.Set;

import com.rits.cloning.Cloner;

public class DSUtil {

	/** Returns the power set of the original set (members are deep copied) */
	public static <T> Set<Set<T>> powerSet(final Set<T> origSet) {
		//System.out.println("powerSet input: "+origSet);
		if (origSet == null)
			return null;
		Set<Set<T>> PS = new HashSet<Set<T>>((int)Math.pow(2, origSet.size()));
		@SuppressWarnings("unchecked")
		final T[] list = (T[])origSet.toArray();
		for (int i = 0; i <= origSet.size(); i++)
			DSUtil.subComb(PS, list, new HashSet<T>(), 0, i, new Cloner());
		//System.out.println("powerSet output: "+PS);
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
	
	public static <T> Set<T> formSetFromObjects(T... objs) {
		Set<T> s = new HashSet<T>(objs.length);
		for( T object : objs)
			s.add(object);
		return s;
	}

}
