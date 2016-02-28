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
	private String clientIp; //the client holding the lock

	public Lock() {
		lockStatus = 0;
		clientIp = "";
	}

	public void setLockStatus(int i) {
		if(i >= 0 && i <= 2)
			lockStatus = i;
	}

	public int getLockStatus() { return lockStatus; }

	public void setClientIp(String c) { clientIp = c; }

	public String getClientIp() { return clientIp; }
}