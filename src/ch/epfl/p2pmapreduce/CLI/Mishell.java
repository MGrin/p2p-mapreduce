package ch.epfl.p2pmapreduce.CLI;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import net.jxta.platform.NetworkManager;
import ch.epfl.p2pmapreduce.index.Metadata;
import ch.epfl.p2pmapreduce.networkCore.JxtaCommunicator;
import ch.epfl.p2pmapreduce.nodeCore.peer.Peer;
import ch.epfl.p2pmapreduce.nodeCore.utils.FileManagerConstants;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;

/**
 * 
 * @author marguet
 * 
 *         Basic CLI in order to connect to the DFS. command help permits to
 *         check the instructions. It is connected to a Peer (the user)
 */
public class Mishell {

	/*
	 * What need to be implemented for compatibility with Peer class
	 * 
	 * TODO file overwriting does not occur in Peer. calling twice rootPut()
	 * with same parameter will result in loading same file twice under
	 * different references. Overwritting put must be controlled by miShell
	 */

	public static Peer p;

	/*
	 * Function to launch : program access
	 */
	public static void main(String[] args) throws java.io.IOException {

		String name = Integer.toString(new Random().nextInt());;

		if(args.length != 0) {
			if (args.length == 1) {
				JxtaCommunicator.SERVER_ADDRESS = args[0];
			} else {
				System.err.println("Too many arguments! Aborting..");
				return;
			}
		}

		Scanner scanner = new Scanner(System.in);
		String line;
		String[] tok;

		java.io.File dataFolder = new java.io.File(FileManagerConstants.DFS_DIR);
		boolean folderExists = (dataFolder.exists() && dataFolder.isDirectory());
		System.out.println("raidfs data already exists? .... " + folderExists);

		if (folderExists) {
			NetworkManager.RecursiveDelete(dataFolder);
		}

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

		// loop over the user inputs
		while (true) {
			System.out.print("miShell>");
			line = scanner.nextLine();
			tok = line.split(" ");
			// only able to help or connect if Peer isn't ready
			if (tok != null) {
				if (tok[0].compareTo("help") == 0) {
					if (tok.length == 2) {
						help(tok[1]);
					} else {
						System.out
						.println("About what command do you need help ? (connect, ls, get, put, rm)");
						System.out.println("Specify only one command.");
					}
				} else if (tok[0].compareTo("connect") == 0) {
					if (tok.length == 1) {
						// System.out.println("handle connect");
						connect();
					} else {
						System.out
						.println("too much arguments for \"connect\"");
					}

				} else if (tok[0].compareTo("quit") == 0) {
					if (tok.length == 1) {
						// System.out.println("handle quit");
						quit();
					} else {
						System.out
						.println("too much arguments for \"quit\"");
					}
				} else if (p.isReadyForActions()) {

					if (tok[0].compareTo("ls") == 0) {
						if (tok.length > 1) {
							// System.out.println("handle ls");
							ls(tok[1]);
						} else if (tok.length == 1) {
							ls("DFS");
						} else {
							System.out.println("not enough arguments");
						}
					} else if (tok[0].compareTo("get") == 0) {
						if (tok.length == 3) {
							// System.out.println("handle get");
							get(tok[1], tok[2]);
						} else if (tok.length > 2) {
							System.out.println("Too much arguments for get");
						} else {
							System.out.println("Please specify a file to get");
						}
					} else if (tok[0].compareTo("put") == 0) {
						if (tok.length == 3) {
							// System.out.print("handle put ");
							put(tok[1], tok[2]);
						} else if (tok.length > 3) {
							System.out.println("Too much arguments for put");
						} else {
							System.out.println("Please specify a file to put");
						}
					} else if (tok[0].compareTo("rm") == 0) {
						if (tok.length == 2) {
							// System.out.println("handle rm");
							// System.out.println("on " + tok[1]);
							rm(tok[1], false);
						} else if (tok.length == 3) {
							if (tok[1].compareTo("-d") == 0) {
								// System.out.println("handle rm on the folder : "
								// + tok[2]);
								rm(tok[2], true);
							}
						} else {
							System.out
							.println("Please specify a file/folder to delete");
						}
					}
				} else {
					System.out
					.println("Peer did not boot the DFS index yet! Try later...");
				}
			}
		}
	}

	/**
	 * list the files in Metadata
	 * 
	 * @param input
	 *            : folder where we want to ls
	 */
	public static void ls(String input) {
		Metadata.metaLs(input);
	}

	/**
	 * When we want to put a new file, we need to add its indexation in
	 * Metadata. will send PutIndexAdvertisement
	 * 
	 * @param osFullFilePath
	 *            : path of the existing file
	 * @param dfsPath
	 *            : path we want to use on the DFS
	 */
	public static void put(String osFullFilePath, String dfsPath) {
		List<String> temp = Metadata.tokenize(osFullFilePath, "/");

		String dfsFullFolderPath = "";
		if (dfsPath.compareTo("/") == 0) {
			dfsFullFolderPath = dfsPath.concat(temp.get(temp.size() - 1));
		} else {
			dfsFullFolderPath = dfsPath.concat("/" + temp.get(temp.size() - 1));
		}
		boolean success = false;

		// System.out.println("with the file (local): " + osFullFilePath
		// + " (DFS): " + dfsFullFolderPath);

		File f = p.rootPut(osFullFilePath, dfsFullFolderPath);
		if (f != null) {
			String infos = getFileInfos(osFullFilePath, dfsFullFolderPath, f);
			// System.out.println("infos not null : with val : " + infos);
			success = p.remotePut(f);

			if (success) {
				Metadata.metaPut(infos);
			} else
				System.out.println("could not put the file on the DFS..");

			System.out.println("Succeeded in putting file " + f.name
					+ " on DFS? " + success);
		}
	}

	/**
	 * When an item is present on the DFS, we want to get it, not only the
	 * chunks.
	 * 
	 * @param input
	 *            : name of the item
	 * @param outPath
	 *            : path where we want to get the file (locally)
	 */
	public static void get(String input, String outPath) {
		// System.out.println("with the file : " + input +
		// " to go on the os as " + outPath);
		if (Metadata.metaExist(input)) {
			System.out.println("file: " + input + " exists");
			p.get(input, outPath);
		}
	}

	/**
	 * Delete the specified file, and delete it on the index -> will send an
	 * RmIndexAdvertisement
	 * 
	 * @param input
	 * @param isDirectory
	 */
	public static void rm(String input, boolean isDirectory) {
		// System.out.println("Removing " + input + " from DFS..");

		if (Metadata.metaExist(input)) {
			boolean success = p.remoteRemove(new File(input, -1));

			if (success) {
				System.out
				.println("Succeeded in removing from distant file System and publishing RmIndexAdvertisement");

				if (isDirectory) {
					Metadata.metaRm(input, true);
				} else {
					Metadata.metaRm(input, false);
				}
			}
		}
	}

	/**
	 * connection to the DFS, permits to the peer to initialize the Metadata
	 * file and permits to discover other peers in ordwer to update this
	 * Metadata file.
	 */
	public static void connect() {
		p.start();
		Metadata.create();
	}

	// disconnect from the DFS and kill the peer
	public static void quit() {
		Runtime.getRuntime().exit(0);
	}

	public static void help(String input) {
		if (input.compareTo("connect") == 0) {
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
			System.out.println("Format : get file_to_get_on_dfs path_on_os");
			System.out.println("No options for \"get\"");
		} else if (input.compareTo("put") == 0) {
			System.out.println("format : put file_to_send path_on_dfs.");
			System.out.println("No options for \"put\".");
		}
	}

	// UTILITY

	private static String getFileInfos(String osFullPath, String DFSFullPath,
			File f) {
		java.io.File file = new java.io.File(osFullPath);

		if (file.exists()) {
			long fileSize = f.chunkCount;
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			String fileDate = sdf.format(file.lastModified());
			String infos = DFSFullPath + "," + fileSize + "," + fileDate;
			return infos;
		} else {
			System.err.println("File " + osFullPath + " doesn't exist");
		}
		return null;
	}
}