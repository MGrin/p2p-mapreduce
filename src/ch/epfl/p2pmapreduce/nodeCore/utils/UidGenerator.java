package ch.epfl.p2pmapreduce.nodeCore.utils;
/**
 * generator of new Ids
 *
 */
public class UidGenerator {
	private static int counter = 0;
	
	private UidGenerator() {}
	
	public static int freshId() {
		return counter++;
	}
}
