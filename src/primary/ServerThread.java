package primary;

import java.io.*;
import java.net.*;

class ServerThread extends Thread{
	Socket socket = null;
	int node = -1;
	Message m = new Message(Program.myNode, 0, Message.type.Request, -1);
	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	boolean terminate = false;
	Server svr;
	
	public ServerThread(Socket s, Server SVR){
		socket = s;
		svr = SVR;
	}
	
	public void run(){
		
		try{
			ois = new ObjectInputStream(socket.getInputStream());
			oos = new ObjectOutputStream(socket.getOutputStream());
			
			node = ois.readInt();
					
			if(node >= 0) {
				do {
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
                    //     Pi's unfulfilled request has a larger timestamp than that of the received request):
                    // then send a REPLY to requesting process
                    // else: defer sending REPLY message

					switch(m.GetType()){
					case Request:
						Server.Q.put(new Requests(m.GetFrom(), m.GetClock()));
                        //need to add a reply to requesting process here for Lamports?
						break;
					case Reply:
						Server.updateReplied(m.GetFrom(), true);
						break;
					case Release:
						Server.Q.take();
						break;
					case Termination:
						terminate = true;
						break;
					default:
						System.out.println("Message Type ERROR...");
					}
					
					Server.updateClock(m.GetClock());
					notifyAll();

					
				}
				while(!terminate);
			} else {
				System.out.println("Got a bad Node");
			}
			
			ois.close();
			oos.close();
			socket.close();
			
		} catch (SocketException e) {
			
		} catch (Exception e) {
			System.out.println("Error in ServerThread: " + e);
		}
	}	
	
	synchronized void write(Message m) throws Exception{
		oos.writeObject(m);
		oos.flush();
	}
	
}
