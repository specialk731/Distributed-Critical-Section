====================
COMPILE
====================
To compile the program, first ensure that all included files are placed according to the included
make file.
Then, use the make file to compile all of the java into class files.

====================
RUN
====================
To run the program use "java Program <nodeID> <Optional R&A argument>"
<nodeID> should be the ID of the node for the specific instance of Program
The optional argument will be "RandA" and is included to run Ricard and Agrawala's.
If the optional argument is omited, the program will use Lamports.

Ideally, the included script will be used to run the nodes on seperate systems.
Without the scripts, each node must be started manually on the correct system as 
stated in config.txt
Change the variables at the top of the script to match your current system configuration. 

After all nodes have output that they are finished, use "java Verifier" to verify the output.
This program will inform the user of any errors by analyzing log files.
