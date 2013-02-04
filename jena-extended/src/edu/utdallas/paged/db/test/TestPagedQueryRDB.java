package edu.utdallas.paged.db.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.query.test.AbstractTestQuery;
import com.hp.hpl.jena.shared.JenaException;

import edu.utdallas.paged.db.PagedGraphRDB;

public class TestPagedQueryRDB extends AbstractTestQuery
{
	public TestPagedQueryRDB( String name )
	{ super( name ); }

	public static TestSuite suite()
	{ return new TestSuite( TestPagedQueryRDB.class ); }     

	private IDBConnection theConnection;
	private int count = 0;

	public void setUp()
	{
		theConnection = TestPagedConnection.makeAndCleanTestConnection();
		super.setUp();
	}

	public void tearDown()
	{
		try { theConnection.close(); }
		catch (Exception e) { throw new JenaException( e ); }
	}

	public Graph getGraph()
	{ 
		return new PagedGraphRDB
		(
				theConnection,
				"testGraph-" + count ++, 
				theConnection.getDefaultModelProperties().getGraph(), 
				GraphRDB.OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS, 
				true
		);
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