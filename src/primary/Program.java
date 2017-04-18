package primary;

import java.io.*;
import java.net.*;
import java.util.*;

public class Program {
	
	static int myNode, numNodes, numRequests;
	static int neighborsNode[];
	static long interRequestDelay, executionTime;
	static String myAddress, myPort;
	static Map<Integer, String> addresses = new HashMap<>();
	static Map<Integer, String> ports = new HashMap<>();
	static Map<Integer, Socket> clients = new HashMap<>();
	static Map<Integer, ObjectOutputStream> oos = new HashMap<>();
	static boolean Lamports = true;
	static Server svr;
	
	public static void main(String[] args) {
		
		setup(args);
		
		showInfo();
		
		svr = new Server(myAddress, myPort, numNodes);
		svr.start();
						
		try{
			Thread.sleep(5000);
			
			clientSetup(addresses, ports);
			
			System.out.println("Starting CS Requests");

			for(int i = 0; i < numRequests; i++){
				Thread.sleep(generateInterRequestDelay());
				csEnter(Lamports);
				Thread.sleep(generateExectutionTime());
				csExit(Lamports);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//showInfo();
		
		System.out.println("Node " + myNode + " has finished");
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
			System.out.println("Node " + neighborsNode[i] + ": " + addresses.get(neighborsNode[i]) + ":" + ports.get(neighborsNode[i]));

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
					if(tmp2[1].contains(".utdallas.edu"))
						myAddress = tmp2[1];
					else
						myAddress = tmp2[1]/* + ".utdallas.edu"*/;
					
					myPort = tmp2[2];
				}else{
					neighborsNode[j] = Integer.parseInt(tmp2[0]);
					
					if(tmp2[1].contains(".utdallas.edu"))
						addresses.put(neighborsNode[j], tmp2[1]);
					else
						addresses.put(neighborsNode[j], tmp2[1]/* + ".utdallas.edu"*/);
					
					ports.put(neighborsNode[j], tmp2[2]);
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
			clients.put(neighborsNode[i], new Socket(addresses.get(neighborsNode[i]), Integer.parseInt(ports.get(neighborsNode[i]))));
			oos.put(neighborsNode[i],  new ObjectOutputStream(clients.get(neighborsNode[i]).getOutputStream()));
			
			oos.get(neighborsNode[i]).writeInt(myNode);
			oos.get(neighborsNode[i]).flush();
			Thread.sleep(5000);
		}
	}
	
	private static long generateInterRequestDelay(){
		long tmp;
		
		tmp = interRequestDelay;
		
		return tmp;
	}
	
	private static long generateExectutionTime(){
		long tmp;
		
		tmp = executionTime;
		
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

}
