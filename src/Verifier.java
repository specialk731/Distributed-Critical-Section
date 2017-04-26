import java.io.*;
import java.util.*;

public class Verifier {

	public static void main(String[] args) {
		
		String fileName = "cs.txt";
		
		File csFile = new File(fileName);
		
		try {
			Scanner csScan = new Scanner(csFile);
			
			String line = csScan.nextLine();
			Scanner lineScan = new Scanner(line);
			
			int node1;
			int node2 = lineScan.nextInt();
			
			String action1;
			String action2 = lineScan.next();
			
			lineScan.close();
			
			do{
				node1 = node2;
				action1 = action2;
				
				line = csScan.nextLine();
				lineScan = new Scanner(line);
				
				node2 = lineScan.nextInt();
				action2 = lineScan.next();
				
				lineScan.close();
				
				if(action1.compareTo("enter") == 0) {
					if(action2.compareTo("exit") != 0) {
						System.out.println("Two enter actions occur consecutively");
						csScan.close();
						return;
					}
					
					if(node1 != node2) {
						System.out.println("An enter is followed by an exit of another process");
						csScan.close();
						return;
					}
				} else if(action1.compareTo("exit") == 0) {
					if(action2.compareTo("enter") != 0) {
						System.out.println("Two exit actions occur consecutively");
					}
				} else {
					System.out.println("Invalid action found");
				}
			} while (csScan.hasNextLine());
			
			System.out.println("System executed correctly");
			
			csScan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
}
