package ch.epfl.p2pmapreduce;

/*
 * Copyright (c) 2010 DawningStreams, Inc.  All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without 
 *  modification, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice, 
 *     this list of conditions and the following disclaimer in the documentation 
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must 
 *     include the following acknowledgment: "This product includes software 
 *     developed by DawningStreams, Inc." 
 *     Alternately, this acknowledgment may appear in the software itself, if 
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The name "DawningStreams,Inc." must not be used to endorse or promote
 *     products derived from this software without prior written permission.
 *     For written permission, please contact DawningStreams,Inc. at 
 *     http://www.dawningstreams.com.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 *  DAWNINGSTREAMS, INC OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  DawningStreams is a registered trademark of DawningStreams, Inc. in the United 
 *  States and other countries.
 *  
 */

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

public class DFSPeerAdvertisement extends Advertisement {
    
    public static final String Name = "PeerAdvertisement";

    // Advertisement elements, tags and indexables
    public final static String AdvertisementType = "jxta:CustomizedAdvertisement";
    
    private ID AdvertisementID = ID.nullID;

    //in Ko
    private int availableStorage = -1;
    
    private final static String IDTag = "MyIDTag";
    private final static String storageTag = "storageTag";
    
    private final static String[] IndexableFields = { IDTag, storageTag };

    public DFSPeerAdvertisement() {
        
        // Accepting default values

    }

    public DFSPeerAdvertisement(Element Root) {
        
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
        
        if (TheElementName.compareTo(storageTag)==0) {
            
            availableStorage = Integer.parseInt(TheTextValue);
            return;
            
        }
        
    }
    
    @Override
    public Document getDocument(MimeMediaType TheMimeMediaType) {
        
        // Creating document
        StructuredDocument TheResult = StructuredDocumentFactory.newStructuredDocument(
                TheMimeMediaType, AdvertisementType);
        
        // Adding elements
        Element MyTempElement;
        
        MyTempElement = TheResult.createElement(storageTag, Integer.toString(availableStorage));
        TheResult.appendChild(MyTempElement);
        
        return TheResult;
        
    }

    public void SetID(ID TheID) {
        AdvertisementID = TheID;
    }

    @Override
    public ID getID() {
        return AdvertisementID;
    }

    @Override
    public String[] getIndexFields() {
        return IndexableFields;
    }

    public void setStorage(int storage) {
        availableStorage = storage;
    }

    public int getStorage() {
        return availableStorage;
    }
    
    @Override
    public DFSPeerAdvertisement clone() throws CloneNotSupportedException {
        
        DFSPeerAdvertisement Result =
                (DFSPeerAdvertisement) super.clone();

        Result.AdvertisementID = this.AdvertisementID;
        Result.availableStorage = this.availableStorage;
        
        return Result;
        
    }
    
    @Override
    public String getAdvType() {
        
        return DFSPeerAdvertisement.class.getName();
        
    }
    
    public static String getAdvertisementType() {
        return AdvertisementType;
    }    
    
    public static class Instantiator implements AdvertisementFactory.Instantiator {

        public String getAdvertisementType() {
            return DFSPeerAdvertisement.getAdvertisementType();
        }

        public Advertisement newInstance() {
            return new DFSPeerAdvertisement();
        }

        public Advertisement newInstance(net.jxta.document.Element root) {
            return new DFSPeerAdvertisement(root);
        }
        
    }

}
