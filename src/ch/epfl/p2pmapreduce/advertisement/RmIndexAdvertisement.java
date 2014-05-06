package ch.epfl.p2pmapreduce.advertisement;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import Examples.E_Messages_And_Advertisements._500_Customized_Advertisement_Example;

import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.TextElement;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;

public class RmIndexAdvertisement extends Advertisement {
	
	public static final String Name = "RmIndexAdvertisement";
	public final static String AdvertisementType = "jxta:CustomizedAdvertisement";
	private ID AdvertisementID = ID.nullID;
	
	private String fileName = "";
    private final static String IDTag = "MyIDTag";
    private final String fileNameTag = "MyfileNameTag";
 
    //TODO: Add DATE!
    
    private final static String[] IndexableFields = { IDTag};
    
    public RmIndexAdvertisement() {

		// Accepting default values

	}
    public RmIndexAdvertisement(Element Root) {
        
        // Retrieving the elements
        TextElement MyTextElement = (TextElement) Root;

        Enumeration TheElements = MyTextElement.getChildren();
        
        while (TheElements.hasMoreElements()) {
            
            TextElement TheElement = (TextElement) TheElements.nextElement();
            
            ProcessElement(TheElement);
            
        }        

    }
    public void ProcessElement(TextElement TheElement) {
        
        String TheElementName = TheElement.getName();
        String TheTextValue = TheElement.getTextValue();
        
        if (TheElementName.compareTo(IDTag)==0) {
            
            try {
                
                URI ReadID = new URI(TheTextValue);
                AdvertisementID = IDFactory.fromURI(ReadID);
                return;
                
            } catch (URISyntaxException Ex) {
                
                // Issue with ID format
                Ex.printStackTrace();
                
            } catch (ClassCastException Ex) {
                
                // Issue with ID type
                Ex.printStackTrace();
                
            }
            
        }
        
        if (TheElementName.compareTo(fileNameTag)==0) {
            
            fileName = TheTextValue;
            return;
            
        }
    }
	
	public Document getDocument(MimeMediaType TheMimeMediaType) {
    
		// Creating document
		StructuredDocument TheResult = StructuredDocumentFactory.newStructuredDocument(
        TheMimeMediaType, AdvertisementType);
    
		// Adding elements
		Element MyTempElement;
    
		MyTempElement = TheResult.createElement(fileNameTag, fileName);
		TheResult.appendChild(MyTempElement);

		return TheResult;
    
	}
	
	public void SetID(ID TheID) {
        AdvertisementID = TheID;
    }
	
	public ID getID() {
        return AdvertisementID;
    }
	
	public String[] getIndexFields() {
		return IndexableFields;
	}
	
	public void SetName(String InName) {
        fileName = InName;
    }
	
	public String GetfileName() {
        return fileName;
    }
	@Override
    public RmIndexAdvertisement clone() throws CloneNotSupportedException {
        
        RmIndexAdvertisement Result =
                (RmIndexAdvertisement) super.clone();

        Result.AdvertisementID = this.AdvertisementID;
        Result.fileName = this.fileName;
        
        return Result;
        
    }
    
    @Override
    public String getAdvType() {
        
        return RmIndexAdvertisement.class.getName();
        
    }
    
    public static String getAdvertisementType() {
        return AdvertisementType;
    }    
    
    public static class Instantiator implements AdvertisementFactory.Instantiator {

        public String getAdvertisementType() {
            return RmIndexAdvertisement.getAdvertisementType();
        }

        public Advertisement newInstance() {
            return new RmIndexAdvertisement();
        }

        public Advertisement newInstance(net.jxta.document.Element root) {
            return new RmIndexAdvertisement(root);
        }
        
    }

}
