import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

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
            serverSocket = new Socket(host, port);
            out = new PrintWriter(serverSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            return true;
        }catch(IOException e){
            System.out.println("Error creating socket:" + e);
            return false;
        }

    }

    public void run(){
        if(initConnection()) {
            listen();
        }
    }

    public void listen(){
        System.out.println("Client listening for server messages...");
        String fromServer = "";
        try {
            while ((fromServer = in.readLine()) != null) {
                client.receivedMessage(host, fromServer);
            }
        }catch(IOException e){
            System.out.println("Error listening:" + e);
        }
    }

    public void sendMessage(String msg){
        System.out.println("Client sent:" + msg);
        out.println(msg);
    }

    public PrintWriter getOut(){
        return out;
    }

    public BufferedReader getIn(){
        return in;
    }


}
