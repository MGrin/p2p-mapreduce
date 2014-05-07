package ch.epfl.p2pmapreduce.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import ch.epfl.p2pmapreduce.nodeCore.network.JxtaMessageSender;

public class Metadata {
	private static Element racine = new Element("DFS");
	public static File file = new File("meta.xml");
	private static Document document = new Document(racine);

	// Constructor for Metadata, use only by the "creator" of the DFS to create
	// a metadata file.
	public static void create() {
		updateMeta(document, file.getAbsolutePath());
	}

	public static void SaveNewVersion(byte[] newFile) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(newFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Methode metaPut to add a file in the xml (arborescence of the DFS)
	public static void metaPut(String fileName) {
		if (!file.exists()) {
			Metadata.create();
		}
		SAXBuilder sxb = new SAXBuilder();
		try {
			document = sxb.build(file);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		racine = document.getRootElement();

		List<String> list = tokenize(fileName, "/");
		Element current = racine;
		List<Element> currentChildren = current.getChildren();
		for (int i = 0; i <= list.size() - 1; i++) {
			int indice = searchIndice(currentChildren, list.get(i));
			if (indice != -1) {
				current = currentChildren.get(indice);
				currentChildren = current.getChildren();
			} else {
				List<String> fileInfos = tokenize(list.get(i), ",");
				Element added = new Element(fileInfos.get(0));
				String text = "";
				if (fileInfos.size() > 1) {
					for (int j = 1; j < fileInfos.size(); j++) {
						text = text.concat(fileInfos.get(j) + ",");
					}
					added.setText(text);
				}
				current.addContent(added);
				current = added;
				currentChildren = current.getChildren();
			}
		}
		updateMeta(document, file.getAbsolutePath());
	}

	// utility function for metaPut
	public static int searchIndice(List<Element> list, String text) {
		int index = -1;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getName().compareTo(text) == 0) {
				index = i;
				i = list.size();
			}
		}
		return index;
	}

	// utility function for metaPut, update xml doc
	static void updateMeta(Document document, String file) {
		try {
			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			sortie.output(document, new FileOutputStream(file));
		} catch (IOException e) {
			System.out.println("Cannot update the file");
		}
	}

	public static void metaRm(String fileName) {
		SAXBuilder sxb = new SAXBuilder();

		try {
			document = sxb.build(file);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		racine = document.getRootElement();
		if (fileName == racine.getName()) {
			System.err.println("Cannot delete the root");
		} else {
			List<String> list = tokenize(fileName, "/");
			Element current = racine;
			List<Element> currentChildren = current.getChildren();
			for (int i = 0; i <= list.size() - 1; i++) {
				List<String> fileInfos = tokenize(list.get(i), ",");
				int indice = searchIndice(currentChildren, fileInfos.get(0));
				if (indice != -1 && i == list.size() - 1) {
					currentChildren.remove(indice);
					i = list.size();
				} else if (indice != -1) {
					current = currentChildren.get(indice);
					currentChildren = current.getChildren();
				} else {
					System.out
							.println("Cannot delete something that doesn't exist");
				}
			}
			updateMeta(document, file.getAbsolutePath());
		}
	}

	public static void metaLs(String folder) {
		SAXBuilder sxb = new SAXBuilder();
		try {
			document = sxb.build(file);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("File doesn't exist");
			e.printStackTrace();
		}

		racine = document.getRootElement();
		System.out.println("listing files in \"" + folder + "\"");
		// best case
		if (racine.getName().compareTo(folder) == 0) {
			List<Element> toPrint = racine.getChildren();
			for (int i = 0; i <= toPrint.size() - 1; i++) {
				System.out.println(toPrint.get(i).getName());
			}
		} else { // not the root
			List<String> list = tokenize(folder, "/");
			Element current = racine;
			List<Element> currentChildren = current.getChildren();
			for (int i = 0; i <= list.size() - 1; i++) {
				int indice = searchIndice(currentChildren, list.get(i));
				if (indice != -1 && i == list.size() - 1) {
					List<Element> toPrint = currentChildren.get(indice)
							.getChildren();
					if (toPrint.size() != 0) {
						for (int j = 0; j <= toPrint.size() - 1; j++) {
							System.out.println(toPrint.get(j).getName());
						}
					}
				} else if (indice != -1) {
					current = currentChildren.get(indice);
					currentChildren = current.getChildren();
				} else {
					System.err.println("Folder doesn't exist");
				}
			}
		}
	}

	public static void metaConnect() {
		JxtaMessageSender.getRawFile(file);
	}

	public static List<File> toFiles() {

		// TODO:Implement!
		return null;
	}

	public static void main(String[] args) {
		// metaRm("DFS");
		// Metadata meta = new Metadata();
		// metaRm("boite/genou");
	}

	public static List<String> tokenize(String input, String delim) {
		List<String> output = null;
		if (input != null) {
			output = new ArrayList<String>();
			StringTokenizer tok = new StringTokenizer(input, delim);
			
			while (tok.hasMoreTokens()) {
				output.add(tok.nextToken());
			}
		}
		
		return output;
	}
}
