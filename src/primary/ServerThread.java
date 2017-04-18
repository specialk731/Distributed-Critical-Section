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
					
					switch(m.GetType()){
					case Request:
						Server.Q.put(new Requests(m.GetFrom(), m.GetClock()));
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