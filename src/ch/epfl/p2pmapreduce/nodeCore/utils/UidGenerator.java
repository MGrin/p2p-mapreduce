package ch.epfl.p2pmapreduce.nodeCore.utils;

public class UidGenerator {
	private static int counter = 0;
	
	private UidGenerator() {}
	
	public static int freshId() {
		return counter++;
	}
}
