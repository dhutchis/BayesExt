/**
 * 
 */
package aima_ext;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Formatter;

import aima.core.probability.CategoricalDistribution;
import aima.core.probability.ProbabilityModel;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesInference;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.FiniteNode;
import aima.core.probability.bayes.exact.EnumerationAsk;
import aima.core.probability.bayes.impl.BayesNet;
import aima.core.probability.bayes.impl.FullCPTNode;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.example.ExampleRV;
import aima.core.probability.proposition.AssignmentProposition;
import aima.core.probability.util.RandVar;
import au.com.bytecode.opencsv.CSVReader;

/**
 * @author dhutchis
 *
 */
public class TestMain1 {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/*BayesianNetwork BN = constructBurgAlarmNet();
		BayesInference bayesInference = new EnumerationAsk();
		CategoricalDistribution d = bayesInference
				.ask(new RandomVariable[] { ExampleRV.BURGLARY_RV },
						new AssignmentProposition[] {
								new AssignmentProposition(
										TestMain1.ALARM_RV, true) }, BN);

		System.out.println("P(Burglary | A)=" + d);
		
		CSVReader reader = new CSVReader(new FileReader("yourfile.csv"));
	    String [] nextLine;
	    while ((nextLine = reader.readNext()) != null) {
	        // nextLine[] is an array of values from the line
	        System.out.println(nextLine[0] + nextLine[1] + "etc...");
	    }*/
		
		/** This will output the 3 probabilities of peter, paul and mary as we vary our level of certainty in the evidence given in load.csv */
		DSRandVar load = DSRandVar.loadFromFile("load.csv");
		load.propagateMassToBelPl();
		load.propagateMassToLikelihood();
		System.out.println(load.toFixedWidthString(true, true));
		
		StringBuffer sb = new StringBuffer();
		Formatter formatter = new Formatter(sb);
		for (double c = 0; c <= 1; c += 0.1) {
			DSRandVar clone = new DSRandVar(load);
			clone.weakenByCertainty(c);
			clone.propagateMassToLikelihood();
			formatter.format("%3.1f %5.3f %5.3f %5.3f\n", c,
					clone.getUnmodifiablePowersetMap().get(Collections.singleton("peter")).likely,
					clone.getUnmodifiablePowersetMap().get(Collections.singleton("paul")).likely,
					clone.getUnmodifiablePowersetMap().get(Collections.singleton("mary")).likely);
		}
		formatter.close();
		System.out.println(sb.toString());
	}
	
	
	
	
	public static final RandVar BURGLARY_RV = new RandVar("Burglary",
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
