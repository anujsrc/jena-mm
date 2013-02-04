package edu.utdallas.paged.db.test;

import com.hp.hpl.jena.db.test.AbstractTestQuery1;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.shared.*;

import edu.utdallas.paged.db.PagedGraphRDB;

import java.util.*;

import junit.framework.*;

public class TestPagedQuery1 extends AbstractTestQuery1
{
	public TestPagedQuery1( String name )
	{ super( name ); }

	public static TestSuite suite()
	{ return new TestSuite( TestPagedQuery1.class ); }     

	private IDBConnection theConnection;
	private int count = 0;

	private List<PagedGraphRDB> graphs;

	public void setUp() throws Exception
	{
		theConnection = TestPagedConnection.makeTestConnection();
		graphs = new ArrayList<PagedGraphRDB>();
		super.setUp();
	}

	public void tearDown() throws Exception
	{
		removeGraphs();
		theConnection.close(); 
		super.tearDown(); 
	}

	private void removeGraphs()
	{ for (int i = 0; i < graphs.size(); i += 1) ((PagedGraphRDB) graphs.get(i)).remove(); }

	public Graph getGraph ( ) {
		return getGraph( ReificationStyle.Minimal );
	}

	public Graph getGraph ( ReificationStyle style )
	{ 
		String name = "jena-test-rdb-TestQuery1-" + count ++;
		if (theConnection.containsModel( name )) makeGraph( name, false, style ).remove();
		PagedGraphRDB result = makeGraph( name, true, style );
		graphs.add( result );    
		return result;
	}

	protected PagedGraphRDB makeGraph( String name, boolean fresh, ReificationStyle style )
	{ return new PagedGraphRDB
		(
				theConnection,
				name, 
				theConnection.getDefaultModelProperties().getGraph(),
				PagedGraphRDB.styleRDB( style ), 
				fresh
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