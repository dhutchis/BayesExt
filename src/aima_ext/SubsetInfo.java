package aima_ext;

public class SubsetInfo {
	public double mass=0, belief=0, plausability=1;
	
	@Override
	public String toString() {
		return "{m="+mass+",bel="+belief+",pl="+plausability+'}';
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
}
