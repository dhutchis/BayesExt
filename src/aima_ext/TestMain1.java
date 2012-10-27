/**
 * 
 */
package aima_ext;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
		BayesianNetwork BN = constructBurgAlarmNet();
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
	    }
		
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
