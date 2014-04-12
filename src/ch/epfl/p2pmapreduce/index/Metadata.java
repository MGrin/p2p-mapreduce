package ch.epfl.p2pmapreduce.index;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom2.*;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Metadata {
	static Element racine = new Element("DFS");
	private static File file = new File("C:/Users/David/Desktop/epfl/master semestre 1 été/Big Data/projet/DFS/meta.xml");
	static Document document = new Document(racine);
	
	//Constructor for Metadata, use only by the "creator" of the DFS to create a metadata file.
	public Metadata() {
		file = new File("C:/Users/David/Desktop/epfl/master semestre 1 été/Big Data/projet/DFS/meta.xml");
		try {
			file.createNewFile();
			racine = new Element("DFS");
			document = new Document(racine);
			Element root = new Element("root");
		    racine.addContent(root);
		    
		    // tests
		    //Element contenu = new Element("coucou.txt");
		    //root.addContent(contenu);
		    
		    updateMeta(document, file.getAbsolutePath());
		    List<Element> list = racine.getChildren();
		    // tests
		    //System.out.println(list.get(0));
		} catch (IOException e) {
			System.out.println("Cannot create the file");
		}
		
	}
	public static void metaPut(String fileName) {
		StringTokenizer tokenizer = new StringTokenizer(fileName,"/");
		int i = 0;
		while(tokenizer.hasMoreElements()){
			String next = (String) tokenizer.nextElement();
			put((String) tokenizer.nextElement(), i);
			i++;
		}
	}
	
	//utility function for metaPut
	public static void put(String text, int i){
		if (racine.getChildren().size() != 0 && racine.getChildren().get(0).getName().compareTo(text) == 0 ){
			System.out.println("mamifere" + racine.getChildren().get(0).getName());	
			racine.getChildren().remove(0);
				
		} else {
			System.out.println(text);	
			Element added = new Element(text);
			racine.addContent(added);
			Element childish = new Element("prout");
			added.addContent(childish);
		}
		updateMeta(document, file.getAbsolutePath());
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
		
	}
	public static void main(String[] args){
		//Metadata meta = new Metadata();
		metaPut("root/coucou.txt");
	}
}
