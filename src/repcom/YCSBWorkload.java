package repcom;

import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.Workload;
import com.yahoo.ycsb.measurements.Measurements;

import java.util.Random;

public class YCSBWorkload extends Workload {

	
	private int txnReadCountX = 0;
	private int txnWriteCountX = 0;
	private int txnReadCountY = 0;
	private int txnWriteCountY = 0;
	private int txnReadCountZ = 0;
	private int txnWriteCountZ = 0;
	
	public YCSBWorkload() {
		
	}
	
	@Override
	public boolean doInsert(DB arg0, Object arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doTransaction(DB arg0, Object arg1) {
		// TODO Auto-generated method stub
		
		String txn = "";
		String ip = "";
		
		Random rand = new Random();
		int randomTxn = rand.nextInt((3 - 1) + 1) + 1;
		int randomIp = rand.nextInt((3 - 1) + 1) + 1;
	    int randomNum1 = rand.nextInt((1000 - 1) + 1) + 1;
	    int randomNum2 = rand.nextInt((1000 - 1) + 1) + 1;
		
	    /*
	     * IP Address randomization
	     */
	    if(randomIp == 1) {
	    	ip = ""; // TODO: fill in
	    }
	    
	    else if(randomIp == 2) {
	    	ip = ""; // TODO: fill in
	    }
	    
	    else if(randomIp == 3) {
	    	ip = ""; // TODO: fill in
	    }
	    
	    /*
	     * TXN generation randomization
	     */
	    if(randomTxn == 1) {
	    	txn = "r,x" + txnReadCountX++
				+ ",w,x" + txnWriteCountX++ + "," + randomNum1
				+ ",w,x" + txnWriteCountX + "," + randomNum2
				+ ",w,z" + txnWriteCountZ + "," + randomNum2;
	    }
	    
	    else if(randomTxn == 2) {
	    	txn = "r,x" + txnReadCountX 
				+ ",w,y" + txnWriteCountY + "," + randomNum1
				+ ",r,y" + txnReadCountY 
				+ ",w,z" + txnWriteCountZ++ + "," + randomNum2;
	    }
	    
	    else if(randomTxn == 3) {
	    	txn = "r,y" + txnReadCountY 
				+ ",r,x" + txnReadCountX++ 
				+ ",r,z" + txnReadCountZ++ 
				+ ",w,y" + txnWriteCountY + "," + randomNum2;
	    }
	    
	    
	    // Send to DB and time
	    long st = System.currentTimeMillis();
	    arg0.update(ip, txn, null);
	    long en = System.currentTimeMillis();
	    Measurements.getMeasurements().measure("READ-MODIFY-WRITE-TXNS", (int)(en-st));
		
		return true;
	}

	@Override
	public void cleanup() {
		
	}
	
}
