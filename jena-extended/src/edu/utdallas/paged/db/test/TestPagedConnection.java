package edu.utdallas.paged.db.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.test.TestConnection;
import com.hp.hpl.jena.shared.JenaException;

import edu.utdallas.paged.db.PagedDBConnection;
import edu.utdallas.paged.db.PagedGraphRDB;
import edu.utdallas.paged.db.RDBConstants;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestPagedConnection extends TestCase
{
	protected static Logger logger = LoggerFactory.getLogger( TestConnection.class );

	String DefModel = PagedGraphRDB.DEFAULT;    

	public TestPagedConnection( String name )
	{ super( name ); }

	public static TestSuite suite()
	{ return new TestSuite( TestPagedConnection.class ); }           

	protected void setUp() throws java.lang.Exception { }

	protected void tearDown() throws java.lang.Exception { }

	private static void loadClass()
	{
		try { Class.forName(RDBConstants.M_DBDRIVER_CLASS); }
		catch (Exception e) { throw new JenaException( e ); }
	}

	public static IDBConnection makeTestConnection() 
	{
		loadClass();
		return new PagedDBConnection( RDBConstants.M_DB_URL, RDBConstants.M_DB_USER, RDBConstants.M_DB_PASSWD, RDBConstants.M_DB );
	}

	public static IDBConnection makeAndCleanTestConnection()
	{
		IDBConnection result = makeTestConnection();
		boolean tryClean = true;
		boolean didClean = false;
		boolean tryUnlock = true;
		String err = null;
		while ( tryClean && !didClean ) {
			try {
				result.cleanDB();
				didClean = true;
			} catch (Exception e) {
				err = err + "\n" + e;
				if ( tryUnlock ) {
					tryUnlock = false;
					if ( result.getDriver().DBisLocked() )
						try {
							result.getDriver().unlockDB();
						} catch ( Exception e1 ) {
							err = err + "\n" + e1;
						}
				} else
					tryClean = false;
			}
		}
		if ( didClean == false )
			throw new JenaException("Failed to clean database.\n" + err);       
		return result;
	}
}
/** Copyright (c) 2008-2010, The University of Texas at Dallas
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the The University of Texas at Dallas nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY The University of Texas at Dallas ''AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL The University of Texas at Dallas BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/