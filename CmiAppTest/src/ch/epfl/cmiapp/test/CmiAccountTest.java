package ch.epfl.cmiapp.test;

import ch.epfl.cmiapp.util.CmiAccount;
import android.test.AndroidTestCase;

public class CmiAccountTest extends AndroidTestCase
{
	CmiAccount account;
	
	@Override
	protected void setUp() throws Exception
	{
		account = new CmiAccount();
		
		account.setGasparId("cnyffele");
		account.setGasparPassword("2branchGraphene");
		account.setUserid("user1262");
		account.setUsername("cnyffeler");
		account.setPassword("clemens");
		
		
		super.setUp();
	}
}
