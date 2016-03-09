
package repcom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by jhughes on 2/28/16.
 * Convenience class for creating a new server connection
 */
public class ClientConnection implements Runnable{
    private Socket serverSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String host;
    private int port;
    private Client client;

    public ClientConnection(String h, int p, Client c){
        host = h;
        port = p;
        client = c;
    }


    public Boolean initConnection(){
        try{
            serverSocket = new Socket();
            serverSocket.connect(new InetSocketAddress(host, port), 5*1000); //try connecting to the socket for x milliseconds, then timeout

            out = new PrintWriter(serverSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            return true;
        }catch(IOException e){
        	System.out.println("Error connecting to host: " + host);
            System.out.println("Error creating socket:" + e);
            e.printStackTrace();
            return false;
        }

    }

    public void run(){
       initConnection();
        System.out.println("Successfully connected to host: " + host);
    }

    public void sendMessage(String msg){
        initConnection();
        System.out.println("Client sent:" + msg);
        out.println(msg); //TODO: when dataCenter is disconnected, this returns a nullpointer
    }

    public PrintWriter getOut(){
        return out;
    }

    public BufferedReader getIn(){
        return in;
    }


}
