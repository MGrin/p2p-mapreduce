package ch.epfl.p2pmapreduce.CLI;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import ch.epfl.p2pmapreduce.index.Metadata;
import ch.epfl.p2pmapreduce.nodeCore.peer.Peer;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;

public class Mishell {

	/*
	 * What need to be implemented for compatibility with Peer class
	 * 
	 * TODO when requesting index at connection, has to initialise Peer with
	 * data contained in meta.xml What need to be done : meta.xml ->
	 * List<nodeCore.volume.File> (already done by Jeremy ?)
	 * 
	 * TODO file overwriting does not occur in Peer. calling twice rootPut()
	 * with same parameter will result in loading same file twice under
	 * different references. Overwritting put must be controlled by miShell
	 * 
	 * TODO remotePut will have to transmit information under same format as in
	 * index initialisation (see first todo) data received from network has to
	 * be parsed and put in a nodeCore.volume.File class
	 */

	public static Peer p;

	public static void main(String[] args) throws java.io.IOException {

		String name = null;
		
		if(args.length == 0) {
			name = Integer.toString(new Random().nextInt());
		} else if (args.length == 1) {
			name = args[0];
		} else {
			System.err.println("You need to specify your name! (No spaces)");
			return;
		}

		Scanner scanner = new Scanner(System.in);
		String line;
		String[] tok;

		p = new Peer(name, 0);

		// Ctrl + C
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutdown hook ran!");
				System.out.println("Stopping Peer..");
				p.kill();
			}
		});
		System.out.println("Root of the DFS is \"DFS\"");
		System.out.println("Type \"help\" if you are lost.");

		while (true) {
			System.out.print("miShell>");
			line = scanner.nextLine();
			tok = line.split(" ");
			if (tok != null) {
				if (tok[0].compareTo("help") == 0) {
					if (tok.length == 2) {
						help(tok[1]);
					} else {
						System.out
								.println("About what command do you need help ? (connect, cat, cd, ls, get, put, rm)");
						System.out.println("Specify only one command.");
					}
				} else if (tok[0].compareTo("quit") == 0) {
					if (tok.length == 1) {
						System.out.println("handle quit");
						quit();
					} else {
						System.out.println("too much arguments for \"quit\"");
					}
				} else if (tok[0].compareTo("connect") == 0) {
					if (tok.length == 1) {
						System.out.println("handle connect");
						connect();
					} else {
						System.out
								.println("too much arguments for \"connect\"");
					}
				} else if (tok[0].compareTo("ls") == 0) {
					if (tok.length > 1) {
						System.out.println("handle ls");
						ls(tok[1]);
					} else if (tok.length == 1) {
						ls("DFS");
					} else {
						System.out.println("not enough arguments");
					}
				} else if (tok[0].compareTo("get") == 0) {
					if (tok.length == 2) {
						System.out.println("handle get");
						get(tok[1]);
					} else if (tok.length > 2) {
						System.out.println("Too much arguments for get");
					} else {
						System.out.println("Please specify a file to get");
					}
				} else if (tok[0].compareTo("put") == 0) {
					if (tok.length == 3) {
						System.out.print("handle put ");
						put(tok[1], tok[2]);
					} else if (tok.length > 3) {
						System.out.println("Too much arguments for put");
					} else {
						System.out.println("Please specify a file to put");
					}
				} else if (tok[0].compareTo("rm") == 0) {
					if (tok.length == 2) {
						System.out.println("handle rm");
						System.out.println("on " + tok[1]);
						rm(tok[1], false);
					} else if (tok.length == 3) {
						if (tok[2].compareTo("-d") == 0) {
							System.out.println("handle rm on the folder : " + tok[1]);
							rm(tok[1], true);
						}
					} else {
						System.out
								.println("Please specify a file/folder to delete");
					}
				}
			}
		}
	}

	public static void ls(String input) {
		Metadata.metaLs(input);
	}

	public static void cat(String input) {
		System.out.println("with the file : " + input);
	}

	public static void cd(String input) {
		if (input == null) {
			System.out.println("without arguments");
		} else {
			System.out.println("with path : " + input);
		}
	}

	public static void put(String osFullFilePath, String dfsPath) {
		List<String> temp = Metadata.tokenize(osFullFilePath, "/");
		
		String dfsFullFolderPath = dfsPath.concat("/" + temp.get(temp.size() - 1));
		boolean success = false;
		System.out.println("with the file (local): " + osFullFilePath
				+ " (DFS): " + dfsFullFolderPath);
		String infos = getFileInfos(osFullFilePath, dfsFullFolderPath);
		if (infos != null) {
			System.out.println("infos not null : with val : " + infos);
			File f = p.rootPut(osFullFilePath, dfsFullFolderPath);
			success = p.remotePut(f);
			
			if (success)
				Metadata.metaPut(infos);
			
			System.out.println("Succedded in putting file " + f.name + " on DFS? "
					+ success);
		}

	}

	public static void get(String input) {
		System.out.println("with the file : " + input);

		// TODO: Implement get
	}

	public static void rm(String input, boolean isDirectory) {

		System.out.println("Removing " + input + " from DFS..");

		boolean success = p.remoteRemove(new File(input, -1));
		
		if (success) {
			System.out.println("Succeeded in removing from distant file System and publishing RmIndexAdvertisement");
			
			if (isDirectory) {
				Metadata.metaRm(input, true);
			} else {
				Metadata.metaRm(input, false);
			}
			System.out.println("Succedded in removing file " + input
					+ " on DFS? " + success);
		}
	}

	public static void connect() {
		p.start();
		Metadata.create();
	}

	public static void quit() {
		Runtime.getRuntime().exit(0);
	}

	public static void help(String input) {
		if (input.compareTo("cd") == 0) {
			System.out.println("Format : cd place_to_go");
			System.out.println("No options for \"cd\"");
		} else if (input.compareTo("connect") == 0) {
			System.out.println("Format : connect");
			System.out.println("No options for \"connect\"");
		} else if (input.compareTo("quit") == 0) {
			System.out.println("Format : quit");
			System.out.println("No options for \"quit\"");
		} else if (input.compareTo("ls") == 0) {
			System.out.println("Format : ls file_on_dfs");
			System.out.println("No options for \"ls\"");
		} else if (input.compareTo("rm") == 0) {
			System.out.println("Format : rm file_to_delete_on_dfs argument");
			System.out.println("arguments : \"-d\" to delete a directory");
		} else if (input.compareTo("get") == 0) {
			System.out.println("Format : get file_to_get_on_dfs");
			System.out.println("No options for \"get\"");
		} else if (input.compareTo("put") == 0) {
			System.out.println("format : put file_to_send path_on_dfs.");
			System.out.println("No options for \"put\".");
		}
	}

	private static String getFileInfos(String osFullPath, String DFSFullPath) {
		java.io.File file = new java.io.File(osFullPath);

		if (file.exists()) {
			long fileSize = file.length();
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			String fileDate = sdf.format(file.lastModified());
			String infos = DFSFullPath + "," + fileSize + "," + fileDate;
			return infos;
		} else {
			System.err.println("File " + osFullPath + " doesn't exist.");
		}
		return null;
	}
}