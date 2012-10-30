package aima_ext;

import java.util.Collection;

public class SubsetInfo {
	public double mass=0, belief=0, plausability=1;
	/** include this for singleton subsets - normally holds likelihood ratio */
	public double likely=-1;
	
	@Override
	public String toString() {
		return "{m="+mass+",bel="+belief+","+(likely != -1 ? "LIKELY="+likely+',' : "")+"pl="+plausability+'}';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(mass);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {

		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SubsetInfo))
			return false;
		SubsetInfo other = (SubsetInfo) obj;
		if (Double.doubleToLongBits(mass) != Double
				.doubleToLongBits(other.mass))
			return false;
		return true;
	}

	public static boolean verifyValidMass(Collection<? extends SubsetInfo> set) {
		double mass = 0;
		for (SubsetInfo si : set)
			mass += si.mass;
		return mass == 1.0; // tolerance?
	}

}
