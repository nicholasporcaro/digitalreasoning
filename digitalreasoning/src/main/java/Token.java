package main.java;

//Simple object to hold token data

public class Token {
	
	String  tData, nEnt;
	Boolean eosFlag, puncFlag;
	Integer	i, index;

	public Token(String tLoad, Boolean eLoad, Boolean pLoad) {
		
		//Token for input data and flags
		
		this.tData    = tLoad;
		this.eosFlag  = eLoad;
		this.puncFlag = pLoad;			
	}
	
	public Token(String sLoad, String tLoad, Integer iLoad) {
		
		//Token for tokenized named entities, full entity, and number of tokens in full entity
		
		this.tData = sLoad;
		this.nEnt  = tLoad;
		this.i     = iLoad;
		
	}
	
public Token(String tLoad, String nLoad, Boolean eLoad, Boolean pLoad, Integer iLoad, Integer iIndex) {
		
		//Token for input data, flags, and full named entity
		
		this.tData    = tLoad;
		this.nEnt 	  = nLoad;
		this.eosFlag  = eLoad;
		this.puncFlag = pLoad;
		this.i		  = iLoad;
		this.index    = iIndex;
	}
}