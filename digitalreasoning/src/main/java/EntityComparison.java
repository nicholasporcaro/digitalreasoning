package main.java;

import java.io.*;
import java.util.*;

public class EntityComparison {
	
	public static List<Token> tokens = new ArrayList<Token>(); //List of tokens from function parameter
	
	public List<Token> CompareEntity(List<Token> t){
		
		/*
		 * 
		 * This method takes a list of tokens, a list of named 
		 * entities, and compares the two against each other,
		 * setting flags and parameters as it goes along. It uses
		 * the ListIterator method to move through the lists, and 
		 * uses checks like hasNext() rather than try/catch blocks 
		 * 
		 */
		
		//Load final list with tokens from function call
		List<Token> tFinal = new ArrayList<Token>(); 		
		for(int tFor = 0; tFor < t.size(); tFor++){
			Token adder = new Token(t.get(tFor).tData,"",t.get(tFor).eosFlag,t.get(tFor).puncFlag,0,tFor);
			tFinal.add(adder);			
		}			
		
		String fullNE 			= "";					//Build phrase from single tokens for matching more than one word
		ListIterator<Token> NE = tokens.listIterator();	//Iteration through named entities
		int k 					= 0; 					//Preserve number of next() calls for proper index
		
		while(NE.hasNext()){
						
			Token tNE 				  = NE.next(); 	  		//Begin looping through named entities			
			ListIterator<Token> input = t.listIterator();	//Iterate through input stream
			while(input.hasNext()){
				
				Token tInput = input.next();				//Load current token to be evaluated
				
				//If the named entity is equal and only one token, update final list 
				if (tNE.tData.equals(tInput.tData) && tNE.i == 1){
					int index = input.nextIndex()-1;					
					tFinal.set(index, new Token(tNE.tData,tNE.nEnt,tInput.eosFlag,tInput.puncFlag,1,tFinal.get(index).index));
				} 
				
				//If the named entity is equal and more than one token, additional evaluation must be performed
				else if(tNE.tData.equals(tInput.tData) && tNE.i > 1){
					
					fullNE += tInput.tData + " "; //Append current matching token
					
					//Append to number of tokens in the named entity (stored in tNE.i) 
						for(k = 1; k < tNE.i; k++){	
							if(input.hasNext()){								
								tInput = input.next();
								fullNE += tInput.tData + " ";
							}
						}
					
					k--;												//Decrement k due to next() calls
					fullNE = fullNE.substring(0,fullNE.length()-1); 	//Remove trailing space
					
					//If there is a full match on the named entity, update the individual tokens
					if(tNE.nEnt.equals(fullNE)){
						for(int l = 0; l <= k; l++){
							Token adder = tFinal.get(input.previousIndex() - k + l);
							tFinal.set(input.previousIndex() - k + l, new Token(adder.tData,tNE.nEnt,adder.eosFlag,adder.puncFlag, l+1, adder.index));
						}										
					}
					fullNE = ""; //Clear string for new evaluation
				}					
			}
		}
		return tFinal;
}
	
	public void loadNamedEntities() throws IOException {
		
		/*
		 * 
		 * This class loads the named entities in NER.txt,
		 * processes them as tokens, and loads them into a
		 * list. The tokenization isn't as thorough as the
		 * one used in MainClass because NER.txt doesn't 
		 * have any punctuation in it. If there were, the
		 * methods from MainClass could easily be called
		 * 
		 */
		
		ClassLoader classLoader  = getClass().getClassLoader(); 									//Load class
		File file 				 = new File(classLoader.getResource("resources/NER.txt").getFile());//Get file resource
		FileReader fr 			 = new FileReader(file);											//Read file contents into string
		BufferedReader br 		 = new BufferedReader(fr); 											//Buffer contents		
		StreamTokenizer tokenize = new StreamTokenizer(br); 										//Tokenize using StreamTokenizer
		String s				 = "";																//Data for full named entity
		int i 					 = 0;																//Counter for full named entity
		Token t					 = new Token("","",0);												//Token for full named entity
		
		tokenize.resetSyntax();
		tokenize.wordChars(0x27, 0x27); 		// '
		tokenize.wordChars(0x2D, 0x2E); 		// - .
		tokenize.wordChars(0x30, 0x39); 		// 0-9
		tokenize.wordChars(0x41, 0x5A); 		// A-Z
		tokenize.wordChars(0x61, 0x7A); 		// a-z
		tokenize.whitespaceChars(0x00, 0x20); 	// All whitespace
		tokenize.eolIsSignificant(true);		// Alert line endings
		
		while(tokenize.nextToken() != StreamTokenizer.TT_EOF){

		    if(tokenize.ttype == StreamTokenizer.TT_WORD) {
		        
		    	//Tokenize each named entity
		    	tokens.add(new Token(tokenize.sval,"",0));
		        s += tokenize.sval + " ";
		        i++;
		        
		    } else if(tokenize.ttype == StreamTokenizer.TT_EOL) {
		        
		         //Full named entities are delimited by EOLs; append full named entity to token		        
		    	for(int j = 1; j < i + 1; j++){
		    		t = tokens.get(tokens.size() - j);
		    		t.nEnt = s.substring(0, s.length()-1);
		    		t.i = i;
		    		tokens.set(tokens.size() - j, t);	    		
		    	}
		    	
		    	//Clear variables for next run
		    	s = "";
		    	i = 0;		    	
		    }
		}	
	}
	
	public EntityComparison() throws IOException{ //Simple constructor
		loadNamedEntities();		
	}
}