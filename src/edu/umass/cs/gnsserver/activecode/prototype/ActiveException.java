package edu.umass.cs.gnsserver.activecode.prototype;

/**
 * @author gaozy
 *
 */
public class ActiveException extends Exception{
	
	public ActiveException(String msg){
		super(msg);
	}
	
	public ActiveException(){
		this("");
	}
}
