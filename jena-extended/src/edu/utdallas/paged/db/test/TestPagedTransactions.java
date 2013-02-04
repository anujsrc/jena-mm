package edu.utdallas.paged.db.test;

import junit.framework.*;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.impl.DriverRDB;
import com.hp.hpl.jena.db.impl.Driver_MySQL;
import com.hp.hpl.jena.db.test.Data;
import com.hp.hpl.jena.rdf.model.*;

import edu.utdallas.paged.db.PagedModelRDB;

public class TestPagedTransactions extends TestCase
{    
	public TestPagedTransactions( String name )
	{ super( name ); }

	public static TestSuite suite()
	{ return new TestSuite( TestPagedTransactions.class ); }   

	PagedModelRDB model = null;    
	Model dbProperties = null;    
	IDBConnection conn = null;
	DriverRDB m_driver = null;

	protected void setUp() throws java.lang.Exception 
	{
		conn = TestPagedConnection.makeAndCleanTestConnection();
		dbProperties = conn.getDatabaseProperties();
		model = PagedModelRDB.createModel(conn); 
		m_driver = new Driver_MySQL();
		m_driver.setConnection(conn);
	}

	protected void tearDown() throws java.lang.Exception 
	{
		model.close();
		model = null;
		conn.cleanDB();
		conn.close();
		conn = null;
	}

	private void addCommit(Statement stmt) 
	{
		model.remove(stmt);
		model.begin();
		model.add(stmt);
		model.commit();
		assertTrue( model.contains(stmt) );
	}

	private void addAbort(Statement stmt) 
	{
		model.remove(stmt);
		model.begin();
		model.add(stmt);
		model.abort();			
		assertTrue(!model.contains(stmt) );
	}

	public void testAddCommitURI() 
	{
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource("test#object");

		addCommit( model.createStatement(s,p,o));    		
	}

	public void testAddAbortURI() 
	{
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource("test#object");

		addAbort( model.createStatement(s,p,o));    		
	}

	public void testAddCommitLiteral() 
	{
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l = model.createLiteral("testLiteral");

		addCommit( model.createStatement(s,p,l));    	
	} 


	public void testAddCommitHugeLiteral() 
	{
		String base = Data.strLong ;
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l = model.createLiteral(buffer.toString());

		addCommit( model.createStatement(s,p,l));    	
	} 

	public void testAddAbortHugeLiteral() 
	{
		String base = Data.strLong ;
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l = model.createLiteral(buffer.toString());

		addAbort( model.createStatement(s,p,l));    	
	} 

	public void testAddCommitDatatype() 
	{
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l = model.createTypedLiteral("stringType");

		addCommit( model.createStatement(s,p,l));    	
	} 

	public void testAddAbortDatatype() 
	{
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l = model.createTypedLiteral("stringType");

		addAbort( model.createStatement(s,p,l));    	
	} 


	public void testAddAbortHugeDatatype() 
	{
		String base = Data.strLong ;
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l2 = model.createTypedLiteral(buffer.toString());

		addAbort( model.createStatement(s,p,l2));    	
	} 

	public void testAddCommitHugeDatatype() 
	{
		String base = Data.strLong ;
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l2 = model.createTypedLiteral(buffer.toString());

		addCommit( model.createStatement(s,p,l2));    	
	} 

	public void testAddCommitBNode() 
	{
		Resource s = model.createResource();
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource();

		addCommit( model.createStatement(s,p,o)); 	
	}

	public void testAddAbortBNode() 
	{
		Resource s = model.createResource();
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource();

		addAbort( model.createStatement(s,p,o)); 	
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