package primary;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

class Server extends Thread{
	String myAddress;
	int myPort;
	static private long myClock = 0;
	static ReadWriteLock ClockLock = new ReentrantReadWriteLock(), RepliedLock = new ReentrantReadWriteLock();
	static boolean serverOn = true;
	static boolean[] replied;
	static ServerSocket serversocket;
	List<ServerThread> threads = new ArrayList<>();

	static BlockingQueue<Requests> Q = new PriorityBlockingQueue<>();
	
	Server(String s ,String s2, int numNodes){
		myAddress = s;
		myPort = Integer.parseInt(s2);
		replied = new boolean[numNodes-1];
	}
	
	public void run(){
		
		try{
			serversocket = new ServerSocket(myPort);		
			
			for(int i = 0; i < Program.addresses.size()-1; i++){
				Socket s = serversocket.accept();
				threads.add(new ServerThread(s, this));
				threads.get(i).start();
			}
		} catch(Exception e){
			System.out.println("Error in Server: " + e);
		}
		
		for(int i=0; i<threads.size(); i++) {
			try {
				threads.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("End of Server");
	}
	
	public void Lamports() throws Exception{
		for(int i = 0; i < Program.numNodes-1; i++){
			threads.get(i).write(new Message(Program.myNode, Program.neighborsNode[i], Message.type.Request, getClock()));
			updateReplied(i,false);
		}
		
		while(!RepliedAllTrue() && Q.peek().getNode() != Program.myNode)
			this.wait();
		
		return;
	}
	
	public void RicartAndAgrawala(){
		
	}
	
	public void Release() throws Exception{
		for(int i = 0; i < Program.numNodes-1; i++)
			threads.get(i).write(new Message(Program.myNode, Program.neighborsNode[i], Message.type.Release, getClock()));
	}
	
	static void updateReplied(int index, boolean value){
		RepliedLock.writeLock().lock();
		replied[index]=value;
		RepliedLock.writeLock().unlock();
	}
	
	static boolean RepliedAllTrue(){
		boolean tmp = true;
		RepliedLock.readLock().lock();
		for(int i = 0; i < replied.length; i++)
			if(!replied[i])
				tmp = false;
		RepliedLock.readLock().unlock();
		return tmp;
	}
	
	static void updateClock(long newClock){
		ClockLock.writeLock().lock();
		if(newClock > myClock)
			myClock = ++newClock;
		else
			myClock++;
		ClockLock.writeLock().unlock();
	}
	
	static long getClock(){
		long tmp;
		ClockLock.readLock().lock();
		tmp = myClock;
		ClockLock.readLock().unlock();
		return tmp;
	}

	public void TurnOffServer() throws IOException {
		serversocket.close();
	}
}

class Requests implements Comparable<Requests>{
	int node;
	long clock;
	
	Requests(int i, long l){
		node = i;
		clock = l;
	}

	@Override
	public int compareTo(Requests arg0) {
		if(this.clock < arg0.clock)
			return -1;
		else if(this.clock > arg0.clock)
			return 1;
		else if(this.node < arg0.node)
			return -1;
		else
			return 0;
	}
	
	public int getNode(){
		return node;
	}
	
	public long getClock(){
		return clock;
	}
	
}