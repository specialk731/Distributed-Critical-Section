package primary;

//import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

class Server extends Thread{
	String myAddress;
	int myPort;
	static private long myClock = 0;
	static ReadWriteLock ClockLock = new ReentrantReadWriteLock(), RepliedLock = new ReentrantReadWriteLock(), terminateLock = new ReentrantReadWriteLock();
	static boolean serverOn = true;
	static boolean[] replied, terminate;
	static int numNodes;
	static ServerSocket serversocket;
	List<ServerThread> threads = new ArrayList<>();
	static boolean termThreads = false;
	
	static Queue<Requests> Defered = new ConcurrentLinkedQueue<>();
	static BlockingQueue<Requests> Q = new PriorityBlockingQueue<>();
	
	Server(String s ,String s2, int numberNodes){
		myAddress = s;
		myPort = Integer.parseInt(s2);
		numNodes = numberNodes;
		replied = new boolean[numNodes-1];
		terminate = new boolean[numNodes-1];
	}
	
	public void run(){
		
		try{
			serversocket = new ServerSocket(myPort);		
			
			for(int i = 0; i < Program.addresses.size(); i++){
				Socket s = serversocket.accept();
				threads.add(new ServerThread(s, this, numNodes, i));
				threads.get(i).start();
				updateTerminate(i,false);
			}
			/*
			while(!TerminateAllTrue()){
				this.wait();
			}
			
			for(int i = 0; i < Program.addresses.size(); i++)
				threads.get(i).terminate = true;
			*/
		} catch(Exception e){
			System.out.println("Error in Server: ");
			e.printStackTrace();
		}
		
		boolean terminating = false;
		
		while(!terminating) {
			
			if(Program.mainDone && RepliedAllTrue()) {
				termThreads = true;
				
				for(int i=0; i<threads.size(); i++) {
					try {
						Program.write(i, new Message(Program.myNode, i, Message.type.Exit, myClock));
						
						threads.get(i).join();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				terminating = true;
			}
		}
		
		
		System.out.println("End of Server");
	}
	
	public void Lamports() throws Exception{
        //On generating a critical section request:
        // Insert the request into the priority queue
        Q.put(new Requests(Program.myNode, getClock()));

        // Broadcast the request to all processes
		for(int i = 0; i < Program.numNodes-1; i++){
			Program.write(i, new Message(Program.myNode, Program.neighborsNode[i], Message.type.Request, getClock()));
			//threads.get(i).write(new Message(Program.myNode, Program.neighborsNode[i], Message.type.Request, getClock()));
			updateReplied(i,false);
		}
		
        // Enter critical section only when:
        //  L1': Pi has received a REPLY message from all processes
        //  (any request received by Pi in the future will have timestamp larger than that of Pi's own request.
        //  L2:  Pi's own request is at the top of its queue
        //  (Pi's request has the smallest timestamp among all requests received by Pi so far.
		while(!RepliedAllTrue() || Q.peek().getNode() != Program.myNode){
			//System.out.println("Waiting for CS... Peeked a " + Q.peek().getNode());
			//synchronized(this){
			//	wait();
			//}
			Thread.sleep(100);
		}
		return;
	}
	
	public synchronized void RicartAndAgrawala() throws Exception{
        //steps:
        //  On generating a critical section request:
        //      broadcast the request to all processes
        for(int i = 0; i < Program.numNodes-1; i++){
            //threads.get(i).write(new Message(Program.myNode, Program.neighborsNode[i], Message.type.Request, getClock()));
            updateReplied(i, false);
        }

        //  Enter critical section only once Pi has received a REPLY from all processes
        while(!RepliedAllTrue()){
            this.wait();
        }
        return;
	}
	
	public void Release() throws Exception{
        //Lamports:
        //  on leaving the critical section:
        //      remove the request from the queue
        Q.take();
        //      Broadcast a release message to all processes
        if(Program.Lamports)
		for(int i = 0; i < Program.numNodes-1; i++)
			Program.write(i, new Message(Program.myNode, Program.neighborsNode[i], Message.type.Release, getClock()));
			//threads.get(i).write(new Message(Program.myNode, Program.neighborsNode[i], Message.type.Release, getClock()));

        //RicartAgrawalas:
        // send all deferred REPLY messages
        //TODO
		
        else{
			Requests R;
			while(Defered.size() > 0){
				R = Defered.poll();
				Program.write(Program.Convert(R.getNode()), new Message(Program.myNode, R.getNode(), Message.type.Release, getClock()));
			}
				
		}
	}
	
	static void updateReplied(int index, boolean value){
		//System.out.println("Changing: " + index + " to: " + value);
		RepliedLock.writeLock().lock();
		//System.out.println("Index: " + index);
		replied[index]=value;
		RepliedLock.writeLock().unlock();
	}
	
	static boolean RepliedAllTrue(){
		boolean tmp = true;
		RepliedLock.readLock().lock();
		for(int i = 0; i < replied.length; i++)
			if(replied[i] == false)
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
		System.out.println("Clock is now: " + myClock);
		ClockLock.writeLock().unlock();
	}
	
	static long getClock(){
		long tmp;
		ClockLock.readLock().lock();
		tmp = myClock;
		ClockLock.readLock().unlock();
		return tmp;
	}
	
	static void updateTerminate(int index, boolean value){
		terminateLock.writeLock().lock();
		terminate[index] = value;
		terminateLock.writeLock().unlock();
	}
	
	static boolean TerminateAllTrue(){
		boolean tmp = true;
		terminateLock.readLock().lock();
		for(int i = 0; i < terminate.length; i++)
			if (terminate[i] == false)
				tmp = false;
		terminateLock.readLock().unlock();
		return tmp;
		
	}

	public void TurnOffServer() throws Exception {
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
