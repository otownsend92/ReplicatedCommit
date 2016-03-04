package repcom; /**
 * Created by jhughes on 2/27/16.
 */
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import java.io.FileNotFoundException;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Scanner;
import org.json.simple.JSONObject;
import java.util.Timer;

import java.io.*;
import java.util.TimerTask;

public class Main{

    private static String configFile = "";
    private static String clientServer = "";
    public static ArrayList<String> serverHosts = new ArrayList<>();
    public static ArrayList<DataCenter> dataCenters= new ArrayList<>();
    private static Integer serverID = -1;
    
    // List of DC IPs
    public static ArrayList<String> serverHosts = new ArrayList<>();

    public static void main(String[] args){
        configFile = args[0];
        readConfig();
        initialize();
    }

    private static void readConfig(){

        try {
            //this reads the whole file as a single string
            String content = new Scanner(new File(configFile)).useDelimiter("\\Z").next();
            parseConfig(content);

        } catch(FileNotFoundException e) {
            System.out.println("Can't find config file: " + configFile);
        }

    }

    private static void parseConfig(String content){
        try {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(content);
            clientServer = (String) obj.get("client_server");
            serverID =  Integer.parseInt((String) obj.get("server_id"));
            JSONArray servs = (JSONArray) obj.get("servers");
            for (Object o : servs){
                JSONObject currServ  = (JSONObject) o;
                serverHosts.add((String)currServ.get("ip"));
            }

        }catch(ParseException e){
            System.out.println("Error parsing JSON:" + e);
        }
    }

    private static void initialize(){
        //spawn a new client or server instance
        if(clientServer.equals("client")){
            final Client c = new Client();
            c.initConnections();

            //Basic test message
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    c.sendMessage(serverHosts.get(0), "Sending a test message...");
                }
            }, 2000);


        }
        else if(clientServer.equals("server")){
            //Spawn a new datacenter, etc
            DataCenter d = new DataCenter(500); // TODO: numShardData is hardcoded for now
            dataCenters.add(d);
            d.start();
        }
    }
}
