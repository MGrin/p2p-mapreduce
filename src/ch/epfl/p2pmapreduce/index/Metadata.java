package ch.epfl.p2pmapreduce.index;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
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
	private static Element racine = new Element("DFS");
	private static File file = new File("C:/Users/David/Desktop/epfl/master semestre 1 été/Big Data/projet/DFS/meta.xml");
	//private static File file = null;
	private static Document document = new Document(racine);


    
	//Constructor for Metadata, use only by the "creator" of the DFS to create a metadata file.
	public Metadata(String fileName) {
		file = new File(fileName);
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
				//System.out.println("add " + current.getName());
				List<String> fileInfos = Send.tokenize(list.get(i), ",");
				Element added = new Element(fileInfos.get(0));
				String text = "";
				if (fileInfos.size() > 1){
					for( int j = 1; j < fileInfos.size();j++){
						text = text.concat(fileInfos.get(j) + ",");
					}
					added.setText(text);
				}
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
			List<String> fileInfos = Send.tokenize(list.get(i), ",");
			int indice = searchIndice(currentChildren , fileInfos.get(0));
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
	}
	
	public static void metaConnect(){
		File fileToSend = file;
		Send.metaFile(fileToSend);
	}
	
	public static void main(String[] args){
		//Metadata meta = new Metadata("C:/Users/David/Desktop/epfl/master semestre 1 été/Big Data/projet/DFS/meta.xml");
		//metaPut("hibou/coucou.txt,28,20.29.20");
		//metaPut("hibou/caillou/bonjour.txt");
		//metaPut("poil/poilu.txt");
		//metaPut("hibou/genoux/hirondelle.txt");
		//metaRm("hibou/coucou.txt");
		//metaLs("hibou");
	}
}
