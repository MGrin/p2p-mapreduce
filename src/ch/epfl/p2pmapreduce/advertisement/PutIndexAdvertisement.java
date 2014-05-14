package ch.epfl.p2pmapreduce.advertisement;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

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

public class PutIndexAdvertisement extends Advertisement {
	public static final String name = "PutIndexAdvertisement";
	public final static String advertisementType = "jxta:PutIndexAdvertisement";

	private ID advertisementID = ID.nullID;
	private String dfsFileName = "";
	private String fileSize = "";
	private String fileCreationTime = "";

	private final static String nameTag = "MyNameTag";
	private final static String idTag = "MyIDTag";
	private final static String dateTag = "MyDateTag";
	private final static String sizeTag = "MySizeTag";

	private final static String[] indexableFields = { nameTag, idTag, dateTag };

	public PutIndexAdvertisement(Element Root) {

		// Retrieving the elements
		TextElement MyTextElement = (TextElement) Root;

		Enumeration TheElements = MyTextElement.getChildren();

		while (TheElements.hasMoreElements()) {

			TextElement TheElement = (TextElement) TheElements.nextElement();

			processElement(TheElement);
		}
	}

	public PutIndexAdvertisement() {

		// Accepting default values
	}

	private void processElement(TextElement TheElement) {
		String theElementName = TheElement.getName();
		String theTextValue = TheElement.getTextValue();

		if (theElementName.compareTo(idTag) == 0) {
			try {

				URI ReadID = new URI(theTextValue);
				advertisementID = IDFactory.fromURI(ReadID);
				return;
			} catch (URISyntaxException Ex) {

				// Issue with ID format
				Ex.printStackTrace();
			} catch (ClassCastException Ex) {

				// Issue with ID type
				Ex.printStackTrace();
			}
		}

		if (theElementName.compareTo(nameTag) == 0) {
			dfsFileName = theTextValue;
			return;
		}

		if (theElementName.compareTo(dateTag) == 0) {
			fileCreationTime = theTextValue;
			return;
		}

		if (theElementName.compareTo(sizeTag) == 0) {
			fileSize = theTextValue;
			return;
		}
	}

	@Override
	public Document getDocument(MimeMediaType theMimeMediaType) {

		// Creating document
		StructuredDocument theResult = StructuredDocumentFactory
				.newStructuredDocument(theMimeMediaType, advertisementType);

		// Adding elements
		Element myTempElement;

		myTempElement = theResult.createElement(dateTag, fileCreationTime);
		theResult.appendChild(myTempElement);

		myTempElement = theResult.createElement(sizeTag,
				fileSize);
		theResult.appendChild(myTempElement);

		myTempElement = theResult.createElement(nameTag, dfsFileName);
		theResult.appendChild(myTempElement);

		return theResult;
	}

	public void setID(ID theID) {
		advertisementID = theID;
	}

	@Override
	public ID getID() {
		return advertisementID;
	}

	@Override
	public String[] getIndexFields() {
		return indexableFields;
	}

	public static String getAdvertisementType() {
		return advertisementType;
	}

	public long getFileSize() {
		return Long.parseLong(fileSize);
	}

	public void setFileSize(long size) {
		fileSize = Long.toString(size);
	}

	public long getFileCreationTime() {
		return Long.parseLong(fileCreationTime);
	}

	public void setFileCreationTime(long time) {
		fileCreationTime = Long.toString(time);
	}

	public String getDFSFileName() {
		return dfsFileName;
	}

	public void setDFSFileName(String name) {
		dfsFileName = name;
	}

	public static class Instantiator implements
			AdvertisementFactory.Instantiator {

		public String getAdvertisementType() {
			return PutIndexAdvertisement.getAdvertisementType();
		}

		public Advertisement newInstance() {
			return new PutIndexAdvertisement();
		}

		public Advertisement newInstance(net.jxta.document.Element root) {
			return new PutIndexAdvertisement(root);
		}
	}

	@Override
	public PutIndexAdvertisement clone() throws CloneNotSupportedException {
		PutIndexAdvertisement result = (PutIndexAdvertisement) super.clone();

		result.advertisementID = this.advertisementID;
		result.dfsFileName = this.dfsFileName;
		result.fileCreationTime = this.fileCreationTime;
		result.fileSize = this.fileSize;

		return result;
	}

	@Override
	public String getAdvType() {
		return PutIndexAdvertisement.class.getName();
	}
}
