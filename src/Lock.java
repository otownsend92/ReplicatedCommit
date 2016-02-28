/*
	Simple java object that represents a lock
	Lock status:
		0 - unlocked
		1 - read lock (shared)
		2 - write lock (exclusive)
*/
import java.util.*;

public class Lock {
	private int lockStatus;	
	private List<String> clientIp; //the client holding the lock

	public Lock() {
		lockStatus = 0;
        clientIp = new ArrayList<String>();
	}

	public void setLockStatus(int i) {
		if(i >= 0 && i <= 2)
			lockStatus = i;
	}

	public int getLockStatus() { return lockStatus; }

	public void addClientIp(String c) { clientIp.add(c); }

    public boolean removeClientIp(String c) {
        return clientIp.remove(c);
    }

    public void removeAllClients() {
        clientIp = new ArrayList<String>();
    }

	public List<String> getClientIp() { return clientIp; }
}