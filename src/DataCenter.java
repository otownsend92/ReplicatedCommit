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
		
		// Parse incoming string from client socket
		public void processInput(String input) {
			
		}
		
	}
	
}
