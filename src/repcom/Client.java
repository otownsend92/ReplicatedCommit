package repcom;

import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


/**
 * Created by jhughes on 2/27/16.
 * Test updte
 */
public class Client extends com.yahoo.ycsb.DB implements Runnable{

    private ArrayList<String> hosts;
    private int portNumber = 3000;
    private HashMap<String, ClientConnection> serverConnections;
    private LinkedList<String> operationQueue;
    private final Object lock = new Object();
    private ServerSocket serverSocket;

    public void initConnections(){
        for (String h: hosts){
            ClientConnection connect = new ClientConnection(h, portNumber, this);
            serverConnections.put(h, connect);
            new Thread(connect).start();
        }

        try {
            serverSocket = new ServerSocket(portNumber);
        }catch(IOException e){
            System.out.println(e.toString());
        }
        new Thread(this).start();
    }

    public void run() {
        System.out.println("Client listening on port " + portNumber + "...");

        while(true) {
            // Accept incoming client connections
            Socket incomingSocket = null;
            try {
                incomingSocket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(incomingSocket != null) {
                new Thread(new ClientHandlerThread(this, incomingSocket)).start();
            }
            else {
                System.out.println("Client failed to connect to server in run().");
            }
        }
    }

    public class ClientHandlerThread implements Runnable{

        private Socket socket;
        private Client c;

        public ClientHandlerThread(Client c, Socket s) {
            this.c = c;
            this.socket = s;
        }

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
                receivedMessage(input);
                socketIn.close();
            }
            catch(IOException e){
                System.out.println(e.toString());
            }
        }

        public void receivedMessage(String msg){
            System.out.println("Received message from: " +msg);
            synchronized(c.lock) {
                c.lock.notify();
            }
        }
    }


    public void sendMessage(String host, String msg){
        try {
            System.out.println("Sending message to: " + host + " msg: " + msg);
            serverConnections.get(host).sendMessage(msg);
        }catch(NullPointerException e){
            System.out.println("Could not get host: " + host);
            e.printStackTrace();
        }
    }

    @Override
    public Status update(String s, String s1, HashMap<String, ByteIterator> hashMap) {

        synchronized(lock) {
            try {
                this.sendMessage(s, s1);
                lock.wait();
            } catch (InterruptedException e) {
                System.out.println("Update thread interrupted: " + e);
            }
        }
        return Status.OK;
    }

    @Override
    public void setProperties(Properties p) {
        super.setProperties(p);
    }

    @Override
    public Status scan(String s, String s1, int i, Set<String> set, Vector<HashMap<String, ByteIterator>> vector) {
        return Status.BAD_REQUEST;
    }

    @Override
    public Status read(String s, String s1, Set<String> set, HashMap<String, ByteIterator> hashMap) {
        synchronized(lock) {
            try {
                this.sendMessage(s, s1);
                lock.wait();
            } catch (InterruptedException e) {
                System.out.println("Read thread interrupted: " + e);
            }
        }
        return Status.OK;
    }

    @Override
    public Status insert(String s, String s1, HashMap<String, ByteIterator> hashMap) {
        return Status.BAD_REQUEST;
    }

    @Override
    public void init() throws DBException {
        super.init();
        //This method is run for YCSB, override and initialize connections since main doesn't run
        hosts.clear();
        Properties prop = getProperties();
        Integer numServ = Integer.parseInt(prop.getProperty("NumServ"));
        for (int i=1; i<= numServ; i++){
            String ip = prop.getProperty("Server" + Integer.toString(numServ));
            System.out.println("Server " + Integer.toString(numServ) +  ": " + ip );
            hosts.add(ip);
        }
        initConnections();
    }

    @Override
    public Properties getProperties() {
        return super.getProperties();
    }

    @Override
    public Status delete(String s, String s1) {
        return Status.BAD_REQUEST;
    }

    public Client() {
        super();
        serverConnections = new HashMap<String, ClientConnection>();
        hosts = Main.serverHosts;
        operationQueue = new LinkedList<String>();
    }

    @Override
    public void cleanup() throws DBException {
        super.cleanup();
    }
}
