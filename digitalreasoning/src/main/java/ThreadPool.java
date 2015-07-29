package main.java;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.concurrent.*;

public class ThreadPool {
	
	//List of MainClasses for thread pooling
	public static List<Tokenizer>   lm  = new ArrayList<Tokenizer>();

	public ThreadPool() {
		
		try {			
			//Load ZIP file
			ClassLoader classLoader  = getClass().getClassLoader(); //Load class
			URL zipURL = classLoader.getResource("resources/nlp_data.zip");
			File zipFile = new File(zipURL.toURI());
			ZipFile zf = new ZipFile(zipFile);
			
			//Iterate through file and create threads with readers 
			for (Enumeration<? extends ZipEntry> e = zf.entries(); e.hasMoreElements();) {
				ZipEntry ze = e.nextElement();
			    String name = ze.getName();		    
			    
			    //Exclude the __MAC_OSX directory
			    if(name.endsWith("t") && name.startsWith("n")){
			    	InputStream in = zf.getInputStream(ze);	
			    	lm.add(new Tokenizer(in)); //Add InputStream to MainClass argument for thread pool
			    }		    
			} zf.close(); //Close ZIP file
			} catch(IOException i){				
			} catch(URISyntaxException use){}		
	}

	public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException, ExecutionException {
		
		ThreadPool                   t = new ThreadPool();				//Load ZIP file and queue MainClass thread calls		
		List<List<Token>>   	   llt = new ArrayList<List<Token>>();	//List of lists from parallel threads
		List<Token>          		lt = new ArrayList<Token>();		//Complete aggregated list of tokens
		List<Token>          	  nelt = new ArrayList<Token>();		//Token list of named entity data
		SortedMap<Integer,String> smis = new TreeMap<Integer,String>(); //Sorted map of token indexes of named entities
		SortedMap<String,String>   sms = new TreeMap<String,String>(); 	//Sorted map of named entities with token indexes as value
		
		//Begin parallel execution
		ExecutorService   ex  = Executors.newFixedThreadPool(lm.size());
		List<Future<List<Token>>> lflt = ex.invokeAll(lm);
		ex.shutdown();
		
		//Read results into list of lists
		for(Future<List<Token>> flt: lflt){
			llt.add(flt.get());			
		}
		
		//Flatten down to one list of tokens
		for(int i = 0; i < llt.size(); i++){
			lt.addAll(llt.get(i));
		}
		
		//Reset index for flattened list
		for(int i = 0; i < lt.size(); i++){
			lt.set(i,new Token(lt.get(i).tData,lt.get(i).nEnt,lt.get(i).eosFlag,lt.get(i).puncFlag,lt.get(i).i,i));
		}
					
		//Load named entities into sorted map by token index
		for(Token b: lt){
			if(b.i > 0){smis.put(b.index, b.nEnt);}	
		}
		
		//Load named entity values as index in sorted map, and append token indexes to value
		for(Map.Entry<Integer, String> entry : smis.entrySet()){			
			if(sms.containsKey(entry.getValue())){
				sms.replace(entry.getValue(), sms.get(entry.getValue()) + " | " + entry.getKey().toString());
			}else {
				sms.put(entry.getValue(), entry.getKey().toString());
			}			
		}
		
		/* 
		 * 
		 * Print named entities, token indexes, count the amount
		 * of times a token matches a named entity, and load
		 * the results into a list to be added in the XML file 
		 * 
		 */
		
		for(Map.Entry<String, String> entry : sms.entrySet()){
			System.out.println("Named Entity  : " + entry.getKey());
			System.out.println("Token Indexes : " + entry.getValue());
			System.out.println("# of Tokens   : " + (entry.getValue().length() - entry.getValue().replace("|", "").length() + 1));
			System.out.println("------------------------");
			
			nelt.add(new Token(entry.getKey(), entry.getValue(), entry.getValue().length() - entry.getValue().replace("|", "").length() + 1));			
		}
						
		CreateXML xml = new CreateXML(lt, nelt); //Create XML file from list of tokens and named entities
		
		}		
	}

