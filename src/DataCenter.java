/*
 * DataCenter.java (network)
 * 	Each Datacenter:
 * 		has its own IP address
 * 		has 3 shards (just simple local objects, NOT over IP)
 * 		will communicate with other datacenters (Paxos coordinators) (these are the “cross-datacenter one-way trips” talked about in the paper)
 * 		will know client IP 
 * 		has a listener thread that spawns new thread per incoming request so listener can go back to listening
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class DataCenter extends Thread {

	private ServerSocket serverSocket;
	
	// Class items that will come in later with other commits
//	Shard x = new Shard();
//	Shard y = new Shard();
//	Shard x = new Shard();
	
	// Possibly remove later 
	int port = 3000;
	
	
	// DataCenter constructor
	public DataCenter() {
		try{
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(2000);
		}
		catch (IOException e){
			System.out.println(e.toString());
		}
	}
	

	public void run() {
		while(true) {
			
			// Accept incoming client connections
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(clientSocket != null) 
				new Thread(new DCHandlerThread(this, clientSocket)).start();
				
			else {
				System.out.println("DC failed to connect to client.");
			}
		}
	}

	
	// HandlerThread class to handle new client connection requests
	public class DCHandlerThread extends Thread {
		
		private Socket socket;
		private DataCenter parentThread;
		
		public DCHandlerThread(DataCenter t, Socket s){
			socket = s;
			parentThread = t;
		}
		
		// Open up socket that was passed in from DataCenter
		// and read contents and parse
		public void run(){
			try{
				String input = null;
				Scanner socketIn = new Scanner(socket.getInputStream());
				if (socketIn.hasNext()){
					input = socketIn.nextLine();
				}
				if (input == null){
					socketIn.close();
					socket.close();
					return;
				}
				processInput(input);
				socketIn.close();
				socket.close();
			}
			catch(IOException e){
				System.out.println(e.toString());
			}
		}
		
		/*
		 * Parse incoming string from client socket
		 */
		private void processInput(String input) {
			String[] recvMsg = input.split(" ");
			
			if (recvMsg[0].equals("accept")) {
				// New accept request from client
				// Begin 2 Phase Commit
				String ipAddr = recvMsg[1];
				String txn = recvMsg[2];
				
				// Start 2PC
				/*
				boolean xGood = shardX.processTransaction(txn);
				boolean YGood = shardY.processTransaction(txn);
				boolean ZGood = shardZ.processTransaction(txn);
				
				if (xGood && yGood && zGood) {
					// All shards agreed and there are no conflicting locks
					// Move forward with transaction and inform other DCs 
					// that you accept the Paxos request
					notifyDCs(txn);
				}
				
				else {
					// One of the shards found a lock conflict and rejected the request
					// TODO: Respond to client? 
				}
				*/
				
			}
			
			else if (recvMsg[0].equals("yes")) {
				// The DC that sent this message is accepting 
				// the attached transaction. Check 
			}
		}
		
		/*
		 * Send a broadcast message to all DCs letting
		 * them know you accept this transaction
		 */
		private void notifyDCs(boolean accepted, String txn) {
			String msg;
			//String myIp = Globals.myIP;
			if(accepted) {
				//msg = "yes " + myIp + " " + txn;
			}
			else {
				//msg = "no " + myIp + " " + txn;
			}
			/*
			for(int i = 0; i < 5; i++){
				try{
				Socket s = new Socket(Main.siteIpAddresses.get(i), Globals.sitePorts.get(i));
				PrintWriter socketOut = new PrintWriter(s.getOutputStream(), true);

				socketOut.println(msg);
				
				socketOut.close();
				s.close();
				}
				catch (IOException e){
					// Try the next one.
				}
			} */
		}
		
	}
	
}
