package ch.epfl.p2pmapreduce.CLI;

import ch.epfl.p2pmapreduce.exchanger.Send;
import ch.epfl.p2pmapreduce.index.Metadata;

import java.util.Scanner;

public class Mishell {
	public static Send sender;

	public static void main(String[] args) throws java.io.IOException {
		Scanner scanner = new Scanner(System.in);
		String line;
		String[] tok;
		sender = new Send();

		// Ctrl + C
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutdown hook ran!");
			}
		});

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
								.println("About what command do you need help ? (cat, cd, ls, get, put, rm)");
						System.out.println("Specify only one command.");
					}
				} else if (tok[0].compareTo("ls") == 0) {
					if (tok.length > 1) {
						System.out.println("handle ls");
						ls(tok[1]);
					} else {
						System.out.println("handle ls");
						ls(tok[1]);
					}
				} else if (tok[0].compareTo("cat") == 0) {
					if (tok.length == 2) {
						System.out.println("handle cat");
						cat(tok[1]);
					} else if (tok.length > 2) {
						System.out.println("Too much arguments for cat");
					} else {
						System.out
								.println("We don't cat the standard input in this CLI, please specify a file to cat");
					}
				} else if (tok[0].compareTo("cd") == 0) {
					if (tok.length == 2) {
						System.out.println("handle cd");
						cd(tok[1]);
					} else if (tok.length == 1) {
						System.out.println("handle cd");
						cd(null);
					} else {
						System.out.println("Too much arguments for cd");
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
						if (tok[1].compareTo("-r") == 0) {
							System.out.println("handle rm");
							System.out.println("on " + tok[2]);
							rm(tok[2], true);
						} else {
							System.out
									.println("Please specify a file/folder to delete");
						}
					} else {
						System.out
								.println("Please specify a file/folder to delete");
					}
				}
			}
		}
	}

	public static String ls(String input) {
		Metadata.metaLs(input);
		return "";
	}

	public static String cat(String input) {
		System.out.println("with the file : " + input);
		return "";
	}

	public static String cd(String input) {
		if (input == null) {
			System.out.println("without arguments");
			;
		} else {
			System.out.println("with path : " + input);
		}
		return "";
	}

	public static String put(String input1, String input2) {
		System.out.println("with the file (local): " + input1 + " (DFS): "
				+ input2);
		sender.put(input1, input2);
		return "";
	}

	public static String get(String input) {
		System.out.println("with the file : " + input);
		return "";
	}

	public static String rm(String input, boolean directory) {
		sender.rm(input, directory);
		return "";
	}

	public static String help(String input) {
		if (input.compareTo("cd") == 0) {
			System.out.println("Format : cd place_to_go");
			System.out.println("No options for \"cd\"");
		} else if (input.compareTo("ls") == 0) {
			System.out.println("Format : ls file_on_dfs");
			System.out.println("No options for \"ls\"");
		} else if (input.compareTo("cat") == 0) {
			System.out.println("Format : cat file_on_dfs");
			System.out.println("No options for \"cat\"");
		} else if (input.compareTo("rm") == 0) {
			System.out.println("Format : rm options file_to_delete_on_dfs ");
			System.out
					.println("Option -r for \"rm\" permits to delete a directory.");
		} else if (input.compareTo("get") == 0) {
			System.out.println("Format : get file_to_get_on_dfs");
			System.out.println("No options for \"get\"");
		} else if (input.compareTo("put") == 0) {
			System.out.println("format : put file_to_send file_on_the_dfs.");
			System.out.println("No options for \"put\".");
		}
		return "";
	}
}