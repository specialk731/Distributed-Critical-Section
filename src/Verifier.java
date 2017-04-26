import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class Verifier {

	static int numOfNodes = -1;
	static int activeFiles = 0;
	static ArrayList<BufferedReader> readers = null;
	static ArrayList<Integer> enterNums = null;
	
	public static void main(String[] args) {
		
		try {
			//Use the config file to determine the # of nodes that are in the system
			File configFile = new File("config.txt");
			BufferedReader confRead = new BufferedReader(new FileReader(configFile));
			String tokenLine = confRead.readLine();
			numOfNodes = Integer.valueOf(tokenLine.substring(0, 1));
			confRead.close();
			
			//build lists and set up activeFiles for termination
			activeFiles = numOfNodes;
			readers = new ArrayList<BufferedReader>();
			enterNums = new ArrayList<Integer>();
			
			//generate all of the output file readers
			for(int i=0; i<numOfNodes; i++) {
				String fileName = "output-" + i + ".txt";
				File tempFile = new File(fileName);
				readers.add(new BufferedReader(new FileReader(tempFile)));
			}
			
			//get the first entry of each file
			for(int i=0; i<readers.size(); i++) {
				String line = readers.get(i).readLine();
				Scanner lineScan = new Scanner(line);
				lineScan.useDelimiter(",");
				String action = lineScan.next();
				
				if(action.compareTo("Enter") != 0) {
					System.out.println("ERROR: Node " + i + " starts with exit");
					lineScan.close();
					cleanUp();
					return;
				}
				
				int clockNum = Integer.valueOf(lineScan.next());
				enterNums.add(clockNum);
				lineScan.close();
			}
			
			//main loop
			do {
				//compare the currently stored enter action values to find the lowest and return
				//	which process it is
				int index = compareEnterClocks();
				
				//check for error in compareEnterClocks()
				//		-1 = invalid node
				//		-2 = 2 nodes enter critical section during the same clock number
				if(index == -1) {
					System.out.println("ERROR: Failed to select next node");
					cleanUp();
					return;
				} else if (index == -2) {
					System.out.println("ERROR: Two nodes enter the critical section at the same time");
					cleanUp();
					return;
				}
				
				// get the next line so we can find the exit clock number
				String line = readers.get(index).readLine();
				Scanner lineScan = new Scanner(line);
				lineScan.useDelimiter(",");
				String action = lineScan.next();
				
				//make sure the next line is actually an exit action
				if(action.compareTo("Exit") != 0) {
					System.out.println("ERROR: Node " + index + ": Enter at " + enterNums.get(index) + " is followed by an enter action");
					lineScan.close();
					cleanUp();
					return;
				}
				
				//store the exit number
				int exitNum = Integer.valueOf(lineScan.next());
				lineScan.close();
				
				//compare this exit to all of the other enters, ensuring that
				//		no 2 processes are in the CS at the same time
				int returnIndex = compareExit(exitNum, index);
				
				//returns the value of the conflicting process if there is one, or -1 if there 
				//		is no conflict
				if(returnIndex != -1) {
					System.out.println("ERROR: Nodes " + index + " and " + returnIndex + " are in the critical section together at clock " + enterNums.get(returnIndex));
					cleanUp();
					return;
				}
				
				//if no error, get the next enter value and replace the current one
				String enterLine = readers.get(index).readLine();
				
				//This checks for the end of file. If it is found, we clean up the file
				if(enterLine != null && !enterLine.isEmpty()) {
					Scanner enterLineScan = new Scanner(enterLine);
					enterLineScan.useDelimiter(",");
					String enterAction = enterLineScan.next();
					
					if(enterAction.compareTo("Enter") != 0) {
						System.out.println("ERROR: Node " + index + ": Exit at " + exitNum + " is followed by an exit action");
						enterLineScan.close();
						cleanUp();
						return;
					}
					
					int clockNum = Integer.valueOf(enterLineScan.next());
					enterNums.set(index, clockNum);
					enterLineScan.close();
				} else {
					//clean up for a finished file
					readers.get(index).close();
					readers.set(index, null);
					enterNums.set(index, Integer.MAX_VALUE);
					activeFiles--;
				}
				
			} while (activeFiles > 1);
			
			System.out.println("Algorithm was successful");
			
			cleanUp();
			
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	private static int compareEnterClocks() {
		int temp = Integer.MAX_VALUE;
		int nextNode = -1;
		
		for(int i=0; i<enterNums.size(); i++) {
			if(enterNums.get(i) < temp) {
				nextNode = i;
				temp = enterNums.get(i);
			} else if (temp != Integer.MAX_VALUE && temp == enterNums.get(i)) {
				return -2;
			}
		}
		
		return nextNode;
	}
	
	private static int compareExit(int exitValue, int index) {
		
		for(int i=0; i<enterNums.size(); i++) {
			if(i != index) {
				if(enterNums.get(i) <= exitValue) {
					return i;
				}
			}
		}
		
		return -1;
	}
	
	private static void cleanUp() {
		for(int i=0; i<readers.size(); i++) {
			if(readers.get(i) != null) {
				try {
					readers.get(i).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
