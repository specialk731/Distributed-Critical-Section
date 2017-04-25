package primary;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class Program {
	
	static int myNode, numNodes, numRequests;
	static int neighborsNode[];
	static long interRequestDelay, executionTime;
	static String myAddress, myPort;
	static Map<Integer, String> addresses = new HashMap<>();
	static Map<Integer, String> ports = new HashMap<>();
	static Map<Integer, Socket> clients = new HashMap<>();
	static Map<Integer, ObjectOutputStream> oos = new HashMap<>(); //Maybe remove the Synch on write function and make this a ConcurrentHashMap<K,V>???
	static boolean Lamports = true;
	static Server svr;
    private static Random rand;
    static boolean mainDone = false, inCS = false;
	
	public static void main(String[] args) throws Exception {
		
        rand = new Random();
        
        //Uses config.txt to set up variables
		setup(args);
		
		//Outputs to console variables from config.txt
		showInfo();
		
		//Start the Server
		svr = new Server(myAddress, myPort, numNodes);
		svr.start();
		
		try{
			if(myNode == 0)
				Files.deleteIfExists(Paths.get("cs.txt"));
			
			Thread.sleep(5000);
			
			clientSetup(addresses, ports);
						
			System.out.println("Starting CS Requests");
			
			BufferedWriter writer;
			
			//Start of Application and run for numRequests times
			for(int i = 0; i < numRequests; i++){
				System.out.println("Starting Request: " + i);
				//Sleep before entering CS
				Thread.sleep(generateInterRequestDelay());
				//Attempt to enter CS
				csEnter(Lamports);
				inCS = true;
				
				writer = new BufferedWriter(new FileWriter("cs.txt", true));
				
				writer.append("Node " + myNode + " enters.");
				writer.append(System.lineSeparator());
				
				System.out.println("Node: " + myNode + " is in the CS with clock value: " + Server.Q.peek().getClock());
				//Sleep while in the CS
				Thread.sleep(generateExectutionTime());
				
				writer.append("Node " + myNode + " exits.");
				writer.append(System.lineSeparator());
				
				writer.flush();
				writer.close();
				
				//Attempt to exit CS
                inCS = false;
				csExit(Lamports);
				System.out.println("Finished Request: " + i);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		mainDone = true;
		
		for(int i=0; i<neighborsNode.length; i++) {
			write(i, new Message(myNode, neighborsNode[i], Message.type.Termination, Server.getClock()));
		}
		
		System.out.println("Node " + myNode + " has finished");
		
		//svr.done();
	}
	
	private static void showInfo(){
		System.out.println("myNode: " + myNode);
		System.out.println("numNodes: " + numNodes);
		System.out.print("Using Algorithm: ");
		if(Lamports)
			System.out.println("Lamports");
		else
			System.out.println("Ricarts and Agrawalas");
		System.out.println("numRequests: " + numRequests);
		System.out.println("interRequestDelay: " + interRequestDelay);
		System.out.println("executionTime: " + executionTime);
		System.out.println("myAddress: " + myAddress);
		System.out.println("myPort: " + myPort);
		
		for(int i = 0; i < neighborsNode.length; i++)
			System.out.println("Node " + neighborsNode[i] + ": " + addresses.get(i) + ":" + ports.get(i));

	}
	
	private static void setup(String[] arguments){
		try{
			myNode = Integer.parseInt(arguments[0]);
			
			if(arguments.length > 1 && arguments[1].equals("RandA"))
				Lamports = false;
			
			String tmp, tmp2[];
			Scanner in = new Scanner(new FileReader("config.txt"));
			
			do{
				tmp = in.nextLine();			
				
			}while(tmp.startsWith("#") || tmp.trim().length() <= 0);
			
			tmp = tmp.replaceAll("#", " ");
			tmp2 = tmp.split("\\s+");
			
			numNodes = Integer.parseInt(tmp2[0]);
			interRequestDelay = Integer.parseInt(tmp2[1]);
			executionTime = Integer.parseInt(tmp2[2]);
			numRequests = Integer.parseInt(tmp2[3]);
			
			neighborsNode = new int[numNodes-1];
			
			
			for(int i = 0, j = 0; i < numNodes; i++){
				do{
					tmp = in.nextLine();
					
				}while(tmp.startsWith("#") || tmp.trim().length() <= 0);
				
				tmp = tmp.replaceAll("#", " ");
				tmp2 = tmp.split("\\s+");
				
				if(Integer.parseInt(tmp2[0]) == myNode){
					if(tmp2[1].contains(".utdallas.edu") || tmp2[1].equals("127.0.0.1"))
						myAddress = tmp2[1];
					else
						myAddress = tmp2[1] + ".utdallas.edu";
					
					myPort = tmp2[2];
				}else{
					neighborsNode[j] = Integer.parseInt(tmp2[0]);
					
					if(tmp2[1].contains(".utdallas.edu") || tmp2[1].equals("127.0.0.1"))
						addresses.put(j, tmp2[1]);
					else
						addresses.put(j, tmp2[1] + ".utdallas.edu");
					
					ports.put(j, tmp2[2]);
					j++;
				}
				
				
			}
			
			in.close();
			
		}catch(Exception e){
			System.out.println("Got Error in Setup:");
			e.printStackTrace();
		}
	}
	
	private static void clientSetup(Map<Integer, String> a,Map<Integer, String> p) throws Exception{
		for(int i = 0; i < numNodes-1; i++){
			clients.put(i, new Socket(addresses.get(i), Integer.parseInt(ports.get(i))));
			oos.put(i,  new ObjectOutputStream(clients.get(i).getOutputStream()));
			
			oos.get(i).writeInt(myNode);
			oos.get(i).flush();
			Thread.sleep(5000);
		}
	}
	
	private static long generateInterRequestDelay(){
		long tmp;
        //mean value of an exponentially distributed random value is 1/lambda
        // a random exponentially distributed value can be given by log(1-x)/lambda
        // where 0<=x<=1. Thus "log(1-x)*mean" is a suitable function.
		tmp = (long) Math.log(1-rand.nextLong())*interRequestDelay;
        // (no Math.log function returns long type, so I'm casting it.)
		
		return tmp;
	}
	
	private static long generateExectutionTime(){
		long tmp;
        //mean value of an exponentially distributed random value is 1/lambda
        // a random exponentially distributed value can be given by log(1-x)/lambda
        // where 0<=x<=1. Thus "log(1-x)*mean" is a suitable function.
		tmp = (long) Math.log(1-rand.nextLong())*executionTime;
        // (no Math.log function returns long type, so I'm casting it.)
		
		return tmp;
		
	}
	
	private static void csEnter(boolean Lamp) throws Exception{
		if(Lamp)
			svr.Lamports();
		else
			svr.RicartAndAgrawala();
		
		return; //Don't return until you are allowed in the CS
	}
	
	private static void csExit(boolean Lamp) throws Exception{
		if(Lamp)
			svr.Release();
		else;
	}
	
	public static int Convert(int i){
		for(int j = 0; j < neighborsNode.length; j++)
			if(neighborsNode[j] == i)
				return j;
		
		return -1;
	}
	
	synchronized static void write(int node,Message m) throws Exception{
		//System.out.println("Writing to node " + node/* + " at index " + Convert(node)*/);
		oos.get(node).writeObject(m);
		oos.get(node).flush();
	}

}
