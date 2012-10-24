/**
 * 
 */
package aima_ext;

import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.FiniteNode;
import aima.core.probability.bayes.impl.BayesNet;
import aima.core.probability.bayes.impl.FullCPTNode;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.util.RandVar;

/**
 * @author dhutchis
 *
 */
public class TestMain1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BayesianNetwork BN = constructBurgAlarmNet();

	}
	
	
	public static final RandVar BURGLARY_RV = new RandVar("Burglary",
			new BooleanDomain());
	public static final RandVar EARTHQUAKE_RV = new RandVar("Earthquake",
			new BooleanDomain());
	public static final RandVar ALARM_RV = new RandVar("Alarm",
			new BooleanDomain());

	private static BayesianNetwork constructBurgAlarmNet() {
		FiniteNode B = new FullCPTNode(TestMain1.BURGLARY_RV,
				new double[] { 0.0001, 0.9999 });
		FiniteNode A = new FullCPTNode(TestMain1.ALARM_RV, new double[] {
				// B=true, A=true
				0.95,
				// B=true, A=false
				0.05,
				// B=false, A=true
				0.01,
				// B=false, A=false
				0.99 
				}, B);
		
		return new BayesNet(B);
	}

}
