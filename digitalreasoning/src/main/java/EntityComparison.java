package main.java;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class EntityComparison {
	
	public static List<Token> tokens = new ArrayList<Token>(); //List of tokens from function parameter
	public static List<Token> tFinal = new ArrayList<Token>(); //Final list of tokens to be passed to CreateXML

	public void CompareEntity(List<Token> t){
		
		//Load final list with tokens from function call
		
		for(int tFor = 0; tFor < t.size(); tFor++){
			Token adder = new Token(t.get(tFor).tData,"",t.get(tFor).eosFlag,t.get(tFor).puncFlag,0,tFor);
			tFinal.add(adder);			
		}			
		
		String fullNER 			= "";						//Build phrase from single tokens for matching more than one word
		ListIterator<Token> NER = tokens.listIterator();	//Iteration through named entities
		int k 					= 0; 						//Preserve number of next() calls for proper index
		
		while(NER.hasNext()){
						
			Token tNER 				  = NER.next(); 	  	//Begin looping through named entities			
			ListIterator<Token> input = t.listIterator();	//Iterate through input stream
			
			while(input.hasNext()){
				
				Token tInput = input.next();
				
				//If the named entity is equal and only one token, update final list 
				if (tNER.tData.equals(tInput.tData) && tNER.i == 1){
					int index = input.nextIndex()-1;					
					tFinal.set(index, new Token(tNER.tData,tNER.nEnt,tInput.eosFlag,tInput.puncFlag,1,tFinal.get(index).index));
				} 
				
				//If the named entity is equal and more than one token, additional evaluation must be performed
				else if(tNER.tData.equals(tInput.tData) && tNER.i > 1){
					
					fullNER += tInput.tData + " "; //Append current matching token
					
					//Append to amount of words in the named entity (stored in tNER.i) 
						for(k = 1; k < tNER.i; k++){	
							if(input.hasNext()){								
								tInput = input.next();
								fullNER += tInput.tData + " ";
							}							
						}
					
					k--; //Decrement k due to next() calls; used below
					fullNER = fullNER.substring(0,fullNER.length()-1); //Remove trailing space
					
					//If there is a full match on the named entity, update the individual tokens
					if(tNER.nEnt.equals(fullNER)){
						for(int l = 0; l <= k; l++){
							Token adder = tFinal.get(input.previousIndex() - k + l);
							tFinal.set(input.previousIndex() - k + l, new Token(adder.tData,tNER.nEnt,adder.eosFlag,adder.puncFlag, l+1, adder.index));
						}										
					}
					fullNER = ""; //Clear string for new evaluation
				}					
			}
		}	
}
	
	public EntityComparison() throws IOException {
		ClassLoader classLoader  = getClass().getClassLoader(); 				//Load class
		File file 				 = new File(classLoader.getResource("resources/NER.txt").getFile()); //Get file resource
		FileReader fr 			 = new FileReader(file);						//Read file contents into string
		BufferedReader br 		 = new BufferedReader(fr); 						//Buffer contents		
		StreamTokenizer tokenize = new StreamTokenizer(br); 					//Tokenize using StreamTokenizer
		String s				 = "";											//Data for full named entity
		int i 					 = 0;											//Counter for full named entity
		Token t					 = new Token("","",0);							//Token for full named entity
		
		tokenize.resetSyntax();
		tokenize.wordChars(0x27, 0x27); // '
		tokenize.wordChars(0x2D, 0x2E); // - .
		tokenize.wordChars(0x30, 0x39); // 0-9
		tokenize.wordChars(0x41, 0x5A); // A-Z
		tokenize.wordChars(0x61, 0x7A); // a-z
		tokenize.whitespaceChars(0x00, 0x20);
		tokenize.eolIsSignificant(true);// Alert line endings
		
		while(tokenize.nextToken() != StreamTokenizer.TT_EOF){

		    if(tokenize.ttype == StreamTokenizer.TT_WORD) {
		        
		    	//Tokenize each named entity
		    	tokens.add(new Token(tokenize.sval,"",0));
		        s += tokenize.sval + " ";
		        i++;
		        
		    } else if(tokenize.ttype == StreamTokenizer.TT_EOL) {
		        
		         //Full named entities are delimited by EOLs,
		         //append full named entity to token		        
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
}

