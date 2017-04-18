package primary;

import java.io.*;

class Message implements Serializable{
	private static final long serialVersionUID = -7192440727255331648L;
	private String message;
	private int from = -1, to = -1;
	long clock = -1;
	boolean isActive = false;
	public enum type {Termination, Request, Reply, Release}; 
	type Type;
	
	//Message
	Message(int f, int t, type ty, long c){
		from = f;
		to = t;
		Type = ty;
		clock = c;
	}
	
	public String GetMessage(){
		return message;
	}
	
	public int GetTo(){
		return to;
	}
	
	public int GetFrom(){
		return from;
	}
	
	public long GetClock(){
		return clock;
	}
	
	public type GetType(){
		return Type;
	}
	
	public boolean SetMessage(String s){
		message = s;
		return true;
	}
	
	public boolean SetTo(int s){
		to = s;
		return true;
	}
	
	public boolean SetFrom(int s){
		from = s;
		return true;
	}
	
	public boolean SetClock(long c){
		clock = c;
		return true;
	}
	
	public void Display() {

	}
	
}