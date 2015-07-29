package main.java;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CreateXML {

	public CreateXML(List<Token> t){
		
		try{
			String poa    =    ""; //Punctuation or Alpha
			int sCount 	  =  	1; //Sentence counter
			Boolean start =  true; //Sentence first word flag			
			
			//Instantiate DocumentBuilder
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder 		  = docFactory.newDocumentBuilder();			
			Document doc 					  = docBuilder.newDocument();
			
			//Create parent node
			Element NLP = doc.createElement("NLP");
			doc.appendChild(NLP);			
			
			//Loop through list and create nodes for all elements
			for(Token i: t){	
				
				//Create new token node and add index
				Element token = doc.createElement("token");
				token.setAttribute("index", Integer.toString(i.index));
				NLP.appendChild(token);
				
				//Check punctuation flag and set type
				if(i.puncFlag){ poa = "punctuation";  }
				else   { poa = "alphanumeric"; }
				
				Element type = doc.createElement("type");
				type.appendChild(doc.createTextNode(poa));
				token.appendChild(type);				
			
				/*
				 * 
				 * This block performs a simple switch to the "start" boolean.
				 * It is initialized as "true" to operate properly on first run.
				 * Since there can only be one first word, it appends the XML 
				 * and switches to false. Once an "End of Sentence" flag is 
				 * detected, it switches back to true. All other operations
				 * apply the term "middle" to the XML, along with the sentence
				 * number as the property of id. In this way, the XML can be
				 * easily queried for amount of sentences, as well as the 
				 * first and last tokens in any given sentence. 
				 * 
				 */
				
				Element eos = doc.createElement("sentence");
				if(start){
					eos.appendChild(doc.createTextNode("first"));
					eos.setAttribute("id", Integer.toString(sCount));
					start = false;
					
				} else if (!start && i.eosFlag){
					eos.appendChild(doc.createTextNode("last"));
					eos.setAttribute("id", Integer.toString(sCount));
					sCount++;
					start = true;
					
				} else{
					eos.appendChild(doc.createTextNode("middle"));
					eos.setAttribute("id", Integer.toString(sCount));
				}
				
				token.appendChild(eos);			
				
				//Load token data
				Element data = doc.createElement("data");
				data.appendChild(doc.createTextNode(i.tData));
				
				//If this node matches a named identity, set attributes
				if(i.i > 0){
					data.setAttribute("namedentity", "true");
					data.setAttribute("position", Integer.toString(i.i));
					data.setAttribute("value", i.nEnt);
				}				
				token.appendChild(data);
				
			}
			
			//Create XML
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer 			  = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source 					  = new DOMSource(doc);
			File f 								  = new File("tokenized_data.xml");
			StreamResult result 				  = new StreamResult(f);
			transformer.transform(source, result);
			System.out.println("File saved to " + f.getAbsolutePath());
			
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			
		}
		catch (TransformerException te) {
			te.printStackTrace();
		}		
	}	
}