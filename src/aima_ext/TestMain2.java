package aima_ext;

import java.io.IOException;

public class TestMain2 {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DSRandVar rv = DSRandVar.loadFromFile("weakenedHT.csv");
		rv.weakenByCertainty(0.6);
		rv.propagateMassToBelPl();
		rv.propagateMassToLikelihood();
		System.out.println(rv.toFixedWidthString(true, true, true));
	}

}
