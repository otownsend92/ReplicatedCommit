import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jhughes on 2/27/16.
 */
public class Client {

    private ArrayList<String> hosts;
    private int portNumber = 3000;
    private HashMap<String, ClientConnection> serverConnections;


    public  Client(ArrayList<String> hostList){
        hosts = hostList;
        serverConnections = new HashMap<>();
    }

    public void initConnections(){
        for (String h: hosts){
            ClientConnection connect = new ClientConnection(h, portNumber, this);
            serverConnections.put(h, connect);
            new Thread(connect).start();
        }
    }

    public void receivedMessage(String host, String msg){
        System.out.println("Received message from: " + host + " msg: " + msg);
    }

    public void sendMessage(String host, String msg){
        serverConnections.get(host).sendMessage(msg);
    }
}
