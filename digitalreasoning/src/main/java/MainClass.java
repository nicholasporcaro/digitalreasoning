package main.java;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class MainClass {
	
	public static List<Token> tokens = new ArrayList<Token>(); //List of tokens
	
	
	public static void parseWord(String t){			
		
		/*
		 * 
		 * The first part of this method tokenizes multi-period words by
    	 * checking if the word contains at least two repeating periods.
    	 * 
    	 * The next part checks for a period as the last character of
    	 * the string passed to it. If that is the case, it strips the 
    	 * period, issues the proper commits for alphas and punctuation,
    	 * and sets the "End of Sentence" flag. This logic is also
    	 * applied to question marks and exclamation points 
    	 * 
    	 */		
		
		if (t.contains("..")){
			tokens.add(new Token(t,false,false));
				
		} else if((t.substring(t.length()-1)).equals(".")){	
			tokens.add(new Token(t.substring(0, t.length()-1),false,false));
			tokens.add(new Token(".",true,true));
				
		} else if((t.substring(t.length()-1)).equals("!")){   			
    		tokens.add(new Token(t.substring(0, t.length()-1),false,false));
			tokens.add(new Token("!",true,true));
			
    	} else if((t.substring(t.length()-1)).equals("?")){		    				
    		tokens.add(new Token(t.substring(0, t.length()-1),false,false));
			tokens.add(new Token("?",true,true));			
	    }			
		else{
			tokens.add(new Token(t,false,false));				
		}			
	}
	
	public static void sQuoteCheck(String a, String p, Boolean b){
		
		/*
		 * 
		 * This method strips single quotes from the
		 * beginning and end of the alpha strings that
		 * are passed to it. It then issues commits to
		 * the token in the proper order to maintain
		 * sentence structure. It does not remove any
		 * single quotes that are between alphas to 
		 * preserve contractions. If b is set, the
		 * punctuation is to be committed first.
		 * 
		 */
		
		if ((a.indexOf("'") == 0) && (a.lastIndexOf("'") == (a.length()-1))){
			a = a.substring(1);
			a = a.substring(0, a.length()-1);
			tokens.add(new Token("'",false,true));
			parseWord(a);
			tokens.add(new Token("'",false,true));
			
		} else if (a.indexOf("'") == 0){
			a = a.substring(1);
			tokens.add(new Token("'",false,true));
			parseWord(a);
			
		} else if (a.lastIndexOf("'") == (a.length()-1)) {
			a = a.substring(0, a.length()-1);
			parseWord(a);
			tokens.add(new Token("'",false,true));
			
		} else {
			if(b){
				tokens.add(new Token(p,false,true));
				parseWord(a);
				
			} else{
				parseWord(a);
				tokens.add(new Token(p,false,true));
			}			
		}		
	}	

	public MainClass() throws IOException{
		
		ClassLoader classLoader  = getClass().getClassLoader(); 				//Load class
		File file 				 = new File(classLoader.getResource("resources/nlp_data.txt").getFile()); //Get file resource
		FileReader fr 			 = new FileReader(file);						//Read file contents into string
		BufferedReader br 		 = new BufferedReader(fr); 						//Buffer contents		
		StreamTokenizer tokenize = new StreamTokenizer(br); 					//Tokenize using StreamTokenizer
		Pattern allPunc 	     = Pattern.compile("(?!\\.)(?!\\-)\\p{Punct}");	//Regex expresstion for all punctuation except . and - as one character
		Pattern allAlnum 		 = Pattern.compile("[\\p{Alnum}'.-]{1,}");		//Regex expresstion for all alphanumerics, \. \- and \' as one range
		Matcher ap 				 = allPunc.matcher("");							//Run regex on punctuation
		Matcher aa 				 = allAlnum.matcher("");						//Run regex on alphanumerics
		Boolean noPunc 			 = true;										//Matcher is not initialized when instantiated. Errors must be caught to use properly
		Boolean pFirst			 = false;										//If the punctuation in a stream comes first, set this flag
		
		/*
		 * 
		 * StreamTokenizer has some behavior that needs modification, namely that it outputs
		 * all numbers as doubles, and " characters as comment starts. I reset the syntax to read
		 * every character as a string except for whitespace. The standard ACSII set of whitespace 
		 * characters is what determines word separation by default, so I just went with that.
		 * However, further analysis needed to occur on each token considering that the default
		 * values were changed. 
		 * 
		 */		
				
		tokenize.resetSyntax();
		tokenize.wordChars(0x21, 0x7E);
		tokenize.whitespaceChars(0x00, 0x20);

		//Loop through entire file and search for tokens
		while(tokenize.nextToken() != StreamTokenizer.TT_EOF){

			//Only take action on words
		    if(tokenize.ttype == StreamTokenizer.TT_WORD) {
			
				//Apply regex to token
				ap.reset(tokenize.sval);
				aa.reset(tokenize.sval);

				//Loop through word
				while(aa.find()){
					ap.find();
					
					/* 
					 * 
					 * Set a flag if there is no punctuation in the stream
					 * so that the code will continue to run. Since StreamTokenizer
					 * will only pass valid streams to this section,we do not need 
					 * to perform this check for alphas.
					 * 
					 * In the case that there are single quotes in the stream, 
					 * sQuoteCheck is called to strip them out of the word if
					 * they are not contractions. Otherwise, a straight call to
					 * parseWord() is issued.
					 * 
					 */
					
					try {
						ap.start();
						noPunc = false;
					}
					
					catch (IllegalStateException e){
						noPunc = true;
					}
					
					if(noPunc){
						parseWord(aa.group());
												
					} else{
						//Check if the first character is punctuation
						if(aa.start() > ap.start())	{pFirst = true;	} 
						else 						{pFirst = false;}
						sQuoteCheck(aa.group(), ap.group(), pFirst);						
					}
				}
		    }		    
		}		
		fr.close();
	}
	
	public static void main(String[] args) throws IOException{
		
		MainClass main = new MainClass();
		EntityComparison e = new EntityComparison();
		e.CompareEntity(tokens);		
		for(Token b: e.tFinal){
			if(b.i > 0){
				System.out.println("Named Entity  : " + b.nEnt);
				System.out.println("Token Value   : " + b.tData);			
				System.out.println("Token Index   : " + b.index);
				System.out.println("Token Position: " + b.i);
				System.out.println("------------------------");
			}
		}
		System.out.println();
		CreateXML xml = new CreateXML(e.tFinal);
	}
}