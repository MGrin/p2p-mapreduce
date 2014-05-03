package ch.epfl.p2pmapreduce.nodeCore.utils;

import java.util.Random;

public class NetworkConstants {


	private NetworkConstants() {}

	public static final int N_MIN = 1;
	public static final int N_OPT = 1;
	public static final int N_MAX = 1;
	
	
	public static final int GC_MIN = 1;
	public static final int GC_OPT = 2;
	public static final int GC_MAX = 2;
	
	// number of peers fetched for selecting best peers
	public static final int CANDIDATE_SIZE = 1;
	
	// count of chunk request sent at the same time
	public static final int CHUNK_REQUEST_COUNT = 3;
	
	// #chunk per peer ~ #chunk in file * GC_OPT / (N_OPT+1)
	
	// chunk size (bytes)
	public static final int CHUNK_SIZE = 256;
	
	// just for simulation
	
	public static final int AREA_SIZE = 10;
	
	private static final int rangeInf = 8300;
	private static final int rangeSup = 8400;
	
	public static int generatePortNumber() {
		
		Random r = new Random();
		return 8300 + r.nextInt(rangeSup - rangeInf);
		
	}
}
