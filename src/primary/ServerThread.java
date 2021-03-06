package primary;

import java.io.*;
import java.net.*;

class ServerThread extends Thread{
	Socket socket = null;
	int node = -1, index;
	Message m = new Message(Program.myNode, 0, Message.type.Request, -1);
	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	boolean terminate = false;
	boolean terminateall[];
	Server svr;
	
	public ServerThread(Socket s, Server SVR, int numNodes, int ind){
		socket = s;
		svr = SVR;
		terminateall = new boolean[numNodes-1];
		index = ind;
	}
	
	public void run(){
		
		try{
			ois = new ObjectInputStream(socket.getInputStream());
			oos = new ObjectOutputStream(socket.getOutputStream());
			
			node = ois.readInt();
					
			if(node >= 0) {
				do {
					//System.out.println("Thread: " + index + " Waiting for Message...");
					m = (Message)ois.readObject();
					
					//Lamports:
					// on receiving a critical section request from another process:
					//  insert the request into the priority queue (done)
					//  send a REPLY message to the requesting process
					//
					// On receiving a release message from another process:
					//  remove the request of that process from the queue (done)


					//RicartAgrawalas:
					//TODO
					// On receiving a critical section request from another process:
					// if (Pi has no unfulfilled request of its own OR
					//	 Pi's unfulfilled request has a larger timestamp than that of the received request):
					// then send a REPLY to requesting process
					// else: defer sending REPLY message

					switch(m.GetType()){
					case Request:
						if(Program.Lamports){
							Server.Q.put(new Requests(m.GetFrom(), m.GetClock()));
							Program.write(Program.Convert(m.GetFrom()),new Message(Program.myNode, m.GetFrom(), Message.type.Reply, Server.getClock())); //Plus 1???
						}
						else { //R&A

							synchronized (Server.Q) {
								if (Server.Q.size() == 0 || Server.Q.peek().getClock() > m.GetClock() || (Server.Q.peek().getClock() == m.GetClock() && Server.Q.peek().getNode() > m.GetFrom()))
									//Send Reply
									Program.write(Program.Convert(m.GetFrom()), new Message(Program.myNode, m.GetFrom(), Message.type.Reply, Server.getClock())); //Plus 1???
								else
									//Otherwise Defer
									Server.Defered.add(new Requests(m.GetFrom(), m.GetClock()));
							}
						}
						break;
					case Reply:
						Server.updateReplied(index, true);
						break;
					case Release:
						Server.Q.take();
						break;
					case Termination:
						Server.updateTerminate(index, true);
						//terminate = true;
						break;
					case Exit:
						break;
					default:
						System.out.println("Message Type ERROR...");
					}
					
					Server.updateClock(m.GetClock());
					synchronized(svr){
					svr.notify();
					}

					
				}
				while(!Server.termThreads);
			} else {
				System.out.println("Got a bad Node");
			}
			
			System.out.println("Thread " + index + " Terminating");
			
			ois.close();
			oos.close();
			socket.close();
			
		} catch (SocketException e) {
			
		} catch (Exception e) {
			System.out.println("Error in ServerThread: ");
			e.printStackTrace();
		}
	}
}
