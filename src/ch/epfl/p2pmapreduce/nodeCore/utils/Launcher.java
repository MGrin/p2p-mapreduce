package ch.epfl.p2pmapreduce.nodeCore.utils;

import java.util.Scanner;


public class Launcher {
	/**
	 * @param args
	 */
	private final static Scanner IN = new Scanner(System.in);
	private final static PeerManager PM = PeerManager.getInstance();
	private final static String PEER_PREFIX = "peer_";
	
	public static void main(String[] args) {
		
		int peerCount = 2;//Integer.parseInt(args[0]);
		
		for (int i = 0; i < peerCount; i++) {
			PM.newPeer(PEER_PREFIX + i);
		}
		
		
		PM.startAll();
		
		promptCommands();
	}
	
	private static void promptCommands() {
		boolean running = true;
		while (running) {
			System.out.println("Enter a command :");
			String command = IN.next();
			if (command.equals("kill")) {
				int id = IN.nextInt();
				PM.kill(id);
			} else if (command.equals("start")) {
				int id = IN.nextInt();
				PM.start(id);
			} else if (command.equals("exit")) {
				PM.killAll();
				running = false;
			} else if (command.equals("connect")) {
				int id = IN.nextInt();
				PM.connect(id);
			} else if (command.equals("neighbors")) {
				int id = IN.nextInt();
				PM.neighbors(id);
			} else if (command.equals("init")) {
				PM.init();
			} else if (command.equals("neighbors-all")) {
				PM.neighborsAll();
			} else if (command.equals("+verbose")) {
				int id = IN.nextInt();
				PM.setVerbose(id, true);
			} else if (command.equals("-verbose")){
				int id = IN.nextInt();
				PM.setVerbose(id, false);
			} else if (command.equals("put")) {
				int id = IN.nextInt();
				String fileName = IN.next();
				int chunkCount = IN.nextInt();
				PM.put(id, fileName, chunkCount);
			}
			IN.reset();
		}
	}
}
