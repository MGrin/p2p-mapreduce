package ch.epfl.p2pmapreduce.index;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import ch.epfl.p2pmapreduce.exchanger.Send;

/*
 * To use for now:
 * 1.First launch the constructor to create the xml file. Change the filename (File file)
 * 2.Use metaPut, metaLs, metaRm(absolute name of the file you want to add in the DFS)
 * 
 */
public class Metadata {
	static Element racine = new Element("DFS");
	private static File file = new File("C:/Users/David/Desktop/epfl/master semestre 1 été/Big Data/projet/DFS/meta.xml");
	static Document document = new Document(racine);


    
	//Constructor for Metadata, use only by the "creator" of the DFS to create a metadata file.
	public Metadata() {
		try {
			file.createNewFile();
			racine = new Element("DFS");
			document = new Document(racine);
		    updateMeta(document, file.getAbsolutePath());

		} catch (IOException e) {
			System.out.println("Cannot create the file");
		}
		
	}
	//Methode metaPut to add a file in the xml (arborescence of the DFS)
	public static void metaPut(String fileName) {
		SAXBuilder sxb = new SAXBuilder();
	    try {
			document = sxb.build(file);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	    racine = document.getRootElement();
	    
		List<String> list = Send.tokenize(fileName, "/");
		Element current = racine;
		List<Element> currentChildren = current.getChildren();
		for (int i = 0; i <= list.size()-1; i++){
			int indice = searchIndice(currentChildren , list.get(i));
			if( indice != -1){
				current = currentChildren.get(indice);
				currentChildren = current.getChildren();
			} else {
				System.out.println("add " + current.getName());
				Element added = new Element(list.get(i));
				current.addContent(added);
				
				current =added;
				currentChildren = current.getChildren();
			}
		}
		updateMeta(document, file.getAbsolutePath());
	}

	//utility function for metaPut
	public static int searchIndice(List<Element> list , String text){
		int index = -1;
		for(int i = 0; i < list.size(); i++){
			if (list.get(i).getName().compareTo(text) == 0){
				index = i;
				
				i = list.size();
			}
		}
		return index;
	}
	
	//utility function for metaPut, update xml doc
	static void updateMeta(Document document, String file){
	   try	{
	      XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
	      sortie.output(document, new FileOutputStream(file));
	   }
	   catch (IOException e){
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
	    List<String> list = Send.tokenize(fileName, "/");
		Element current = racine;
		List<Element> currentChildren = current.getChildren();
		for (int i = 0; i <= list.size()-1; i++){
			int indice = searchIndice(currentChildren , list.get(i));
			if( indice != -1 && i == list.size()-1){
				currentChildren.remove(indice);
				i = list.size();
			} else if (indice != -1){
				current = currentChildren.get(indice);
				currentChildren = current.getChildren();
			} else {
				System.out.println("Cannot delete something that doesn't exist");
			}
		}
		updateMeta(document, file.getAbsolutePath());
	}
	
	public static void metaLs(String folder){
		SAXBuilder sxb = new SAXBuilder();
	    try {
			document = sxb.build(file);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	    racine = document.getRootElement();
	    List<String> list = Send.tokenize(folder, "/");
		Element current = racine;
		List<Element> currentChildren = current.getChildren();
		for (int i = 0; i <= list.size()-1; i++){
			int indice = searchIndice(currentChildren , list.get(i));
			if( indice != -1 && i == list.size()-1){
				List<Element> toPrint = currentChildren.get(indice).getChildren();
				if (toPrint.size() != 0){
					for(int j = 0; j<= toPrint.size()-1; j++){
						System.out.println(toPrint.get(j).getName());
					}
				}
			} else if (indice != -1){
				current = currentChildren.get(indice);
				currentChildren = current.getChildren();
			} else {
				System.out.println("Folder doesn't exist");
			}
		}
		//updateMeta(document, file.getAbsolutePath());
	}
	public static void main(String[] args){
		//Metadata meta = new Metadata();
		//metaPut("hibou/coucou.txt");
		//metaPut("hibou/bonjour.txt");
		//metaPut("poil/poilu.txt");
		//metaPut("hibou/genoux/hirondelle.txt");
		//metaRm("hibou/genoux/hirondelle.txt");
		//metaLs("hibou");
	}
}
