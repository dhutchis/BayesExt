package aima_ext;

import java.io.IOException;

public class TestMain3 {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DSRandVar rv1 = DSRandVar.loadFromFile("HTrv1.csv");
		DSRandVar rv2 = DSRandVar.loadFromFile("HTrv2.csv");
		
		DSRandVar rv12 = rv1.combineWith(rv2);
		rv12.propagateMassToBelPl();
		rv12.propagateMassToLikelihood();
		
		System.out.println(rv12.toFixedWidthString(true, true));
	}

}
