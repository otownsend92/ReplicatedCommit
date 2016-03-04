package repcom;

import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;
import java.util.*;



/**
 * Created by jhughes on 2/27/16.
 */
public class Client extends com.yahoo.ycsb.DB{

    private ArrayList<String> hosts;
    private int portNumber = 3000;
    private HashMap<String, ClientConnection> serverConnections;

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

    @Override
    public Status update(String s, String s1, HashMap<String, ByteIterator> hashMap) {
        this.sendMessage(s, s1);
        return null;
    }

    @Override
    public void setProperties(Properties p) {
        super.setProperties(p);
    }

    @Override
    public Status scan(String s, String s1, int i, Set<String> set, Vector<HashMap<String, ByteIterator>> vector) {

        return null;
    }

    @Override
    public Status read(String s, String s1, Set<String> set, HashMap<String, ByteIterator> hashMap) {
        this.sendMessage(s, s1);
        return null;
    }

    @Override
    public Status insert(String s, String s1, HashMap<String, ByteIterator> hashMap) {
        System.out.println("Insert transaction...");
        //this.sendMessage(s, s1);
        return Status.OK;
    }

    @Override
    public void init() throws DBException {
        super.init();
    }

    @Override
    public Properties getProperties() {
        return super.getProperties();
    }

    @Override
    public Status delete(String s, String s1) {
        this.sendMessage(s, s1);
        return null;
    }

    public Client() {
        super();
        serverConnections = new HashMap<>();
        hosts = Main.serverHosts;
    }

    @Override
    public void cleanup() throws DBException {
        super.cleanup();
    }
}
