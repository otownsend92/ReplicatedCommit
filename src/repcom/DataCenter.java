package repcom;
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
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class DataCenter extends Thread {

	private String myIp;
    
	private ServerSocket serverSocket;
	private Map<String, Integer> pendingTxns = 
			Collections.synchronizedMap(new HashMap<String, Integer>());
	
	Shard shardX; 
	Shard shardY;
	Shard shardZ;
	
	// Possibly remove later 
	int port = 3000;
	
	
	// DataCenter constructor
	public DataCenter(int numShardData, String ip) {
		try{
			serverSocket = new ServerSocket(port);
			
			myIp = ip;
			
			shardX = new Shard("x", numShardData);
			shardY = new Shard("y", numShardData);
			shardZ = new Shard("z", numShardData);
		}
		catch (IOException e){
			System.out.println(e.toString());
		}
	}
	

	/**
	 * Listener thread 
	 */
	public void run() {
		while(true) {
			
			// Accept incoming client connections
			Socket clientSocket = null;
			try {
				System.out.println("Data center listening on port " + port + "...");
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
			System.out.println("Received input: " + input);
			String[] recvMsg = input.split(" ");
			String ipAddr = recvMsg[1];
			String txn = recvMsg[2];
			
			if (recvMsg[0].equals("accept")) {
				// New accept request from client
				// Begin 2 Phase Commit
				addPendingTxn(txn);
				
				// Start 2PC
				boolean xGood = shardX.processTransaction(ipAddr, txn);
				boolean yGood = shardY.processTransaction(ipAddr, txn);
				boolean zGood = shardZ.processTransaction(ipAddr, txn);
				
				if (xGood && yGood && zGood) {
					// All shards agreed and there are no conflicting locks
					// Move forward with transaction and inform other DCs 
					// that you accept the Paxos request
					notifyDCsAndClient(true, txn);
				}
				
				else {
					// One of the shards found a lock conflict and rejected the request
					notifyDCsAndClient(false, txn);
				}
				
				
			}
			
			else if (recvMsg[0].equals("yes")) {
				// The DC that sent this message is accepting 
				// the attached transaction. Check pendingTxns
				
				synchronized(pendingTxns) {
					if(!pendingTxns.containsKey(txn)) 
						addPendingTxn(txn);
					
					incrementTxnQuorum(txn);
					if(checkQuorum(txn, ipAddr))
						removePendingTxn(txn);
				}
			}
			
			else if(recvMsg[0].equals("no")) {
				// Another DC has told us it doesn't accept this txn
				// Decrement quorum and check
				synchronized(pendingTxns) {
					if(!pendingTxns.containsKey(txn)) 
						addPendingTxn(txn); 
					
					decrementTxnQuorum(txn);
					if(checkQuorum(txn, ipAddr))
						removePendingTxn(txn);
				}
			}
		}
		
		
		/*
		 * Add this new incoming txn to pendingTxns
		 */
		private synchronized void addPendingTxn(String txn) {
			pendingTxns.put(txn, 0);
		}
		
		/*
		 * This txn is finished. Remove it from pendingTxns
		 */
		private synchronized void removePendingTxn(String txn) {
			pendingTxns.remove(txn);
		}
		
		/*
		 * Increment txn quorum counter
		 */
		private synchronized void incrementTxnQuorum(String txn) {
			pendingTxns.put(txn, pendingTxns.get(txn)+3);
		}
		
		/*
		 * Decrement txn quorum counter
		 */
		private synchronized void decrementTxnQuorum(String txn) {
			pendingTxns.put(txn, pendingTxns.get(txn)-1);
		}
		
		/*
		 * Check quorum for this txn. If = 3, commit txn
		 * 
		 * Return TRUE if txn was successfully committed or aborted (remove txn from pendingTxns)
		 * Return FALSE if txn failed to commit or abort
		 */
		private synchronized boolean checkQuorum(String txn, String ip) {
			
			int quorumVal = -9;
			
			synchronized(pendingTxns) {
				quorumVal = pendingTxns.get(txn);
			}
			
			
			if (quorumVal == -9) {
				// Wasn't able to access quorumVal
				return false;
			}
			
			else if(quorumVal == -1) { 
				// One has said "no"  ...wait for others
				return false;
			}
			
			else if(quorumVal == -2) { 
				// 2 have said "no" ...abort
				performTxn(false, txn, ip);
				
				// Return false as in, keep txn in pendingTxns
				// in case 3rd DC comes around and responds eventually
				// we won't re-add it to pendingTxns
				return false; 
			}
			
			else if(quorumVal == -3) { 
				// All 3 have said "no", but we have already aborted above
				// when (quroumVal == -2) for efficiency's sake
				
				// Txn is already aborted, but need to remove
				// from pendingTxns
				return true;
			}
			
			else if(quorumVal == 1) { 
				// 2 said "no" and 1 said "yes"  ...abort
				
				performTxn(false, txn, ip);
				return true;
			}
			
			else if(quorumVal == 2) { 
				// 1 said "no" and 1 said "yes"  ...keep waiting
				return false;
			}
			
			else if(quorumVal == 3) { 
				// 1 said "yes"  ...keep waiting
				return false;
			}
			
			else if(quorumVal >= 5) { 
				// Either 2 have said "yes" and 1 has said "no" (quorumVal == 5)
				// OR ... all have said "yes" (quorumVal == 9)
				
				// Tell shards to acceot
				performTxn(true, txn, ip);
				return true;
			}
			
			
			return false;
		}
		
		private void performTxn(boolean commit, String txn, String ip) {
			shardX.performTransaction(ip, commit, txn);
			shardY.performTransaction(ip, commit, txn);
			shardZ.performTransaction(ip, commit, txn);
		}
		
		/*
		 * Send a broadcast message to all DCs letting
		 * them know you accept this transaction
		 */
		private void notifyDCsAndClient(boolean accepted, String txn) {
			String msg = "";
			if(accepted) {
				msg = "yes " + myIp + " " + txn;
			}
			else {
				msg = "no " + myIp + " " + txn;
			}
			
			for(int i = 0; i < Main.serverHosts.size(); i++){
				try{
					Socket s = new Socket(Main.serverHosts.get(i), 3000);
					PrintWriter socketOut = new PrintWriter(s.getOutputStream(), true);
	
					socketOut.println(msg);
					
					socketOut.close();
					s.close();
				}
				catch (IOException e){
					// ...
				}
			} 
		}
	}
	
}