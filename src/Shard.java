/**
 * Shard class represents a data shard in a data center. Holds lock tables and a log	
*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


public class Shard {
    private final int UNLOCKED = 0;
    private final int READ = 1;
    private final int WRITE = 2;

	ArrayList<String> transactionLog;

	//these two need to be the same length
    //indexed by variable name (a,b,c...)
	Map<String, Lock> lockTable; 
	Map<String, Integer> data;
    String shardIp;

	//TODO: change this based on config file
	public Shard() { 
		lockTable = new HashMap<String, Lock>();
		data = new HashMap<String, Integer>();
        shardIp = "123.123.123.123";
	}

    public Shard(String ip) {
		lockTable = new HashMap<String, Lock>();
		data = new HashMap<String, Integer>();
        shardIp = ip;
    }

    /**
     * Phase 1 of two phase commit
     * @return: true if it can gather all locks.
     */
    public boolean processTransaction(String rawTransaction) {
        List<Transaction> trans = tokenizeTransaction(rawTransaction); 
        for(Transaction tran:trans) {
            System.out.println(tran.getType() + ", " + tran.getVariable() + ", " + tran.getWriteValue());
        }

        return gatherLocks(trans);
    }

    /*
     * Given a string of input, returns a list of transactions
     */
    private List<Transaction> tokenizeTransaction(String rawTransaction) {
        List<Transaction> trans = new ArrayList<Transaction>();
        StringTokenizer st = new StringTokenizer(rawTransaction, ",");

        while(st.hasMoreElements()) {
            String type = (String)st.nextElement();
            if(type.equals("r")) {
                String variable = (String)st.nextElement();
                if(variable == null) {
                    System.out.println("read is wrong");
                    return null;
                }
                Transaction tran = new Transaction(type, variable, 0);
                trans.add(tran);

            } else if(type.equals("w")) {
                String variable = (String)st.nextElement();
                if(variable == null) {
                    System.out.println("write is wrong");
                    return null;
                }
                String write = (String)st.nextElement();
                if(write == null) {
                    System.out.println("write is wrong");
                    return null;
                }
                int writeValue = Integer.parseInt(write);
                
                Transaction tran = new Transaction(type, variable, writeValue);
                trans.add(tran);
            } else {
                System.out.println("bro wtf");
                return null;
            }
        }

        return trans;
    }

    private boolean gatherLocks(List<Transaction> trans) {
        for(Transaction tran:trans) {
            if(!lockTable.containsKey(tran.getVariable())) //don't look in lockTable for variables we don't store
                continue;

            Lock lock = lockTable.get(tran.getVariable());
            int lockStatus = lock.getLockStatus();
            List<String> lockIp = lock.getClientIp();

            if(tran.isRead()) { //processing read transaction
                if(lockStatus == WRITE) { //write lock has been acquired (doesn't matter by who),  we can't get our read lock
                   return false; 
                }

                //acquire read lock
                lock.addClientIp(shardIp); 
                lock.setLockStatus(READ);
            } else { //processing write transaction
                if(lockStatus == WRITE && !lockIp.contains(shardIp)) { //write lock has been acquired by someone else,  we can't get our read lock
                   return false; 
                }
                lock.removeAllClients(); //remove all clients that have had a read lock

                //acquire write lock
                lock.addClientIp(shardIp); 
                lock.setLockStatus(WRITE);
            }
        }

        return true;
    }

	
}