package edu.utdallas.paged.db.test;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.shared.*;

import edu.utdallas.paged.db.PagedGraphRDB;

import junit.framework.*;

public class TestPagedReifier extends AbstractTestReifier  
{
	private int count;
	private Graph properties;
	private IDBConnection con;

	public TestPagedReifier( String name ) 
	{ super(name); }

	@SuppressWarnings("unchecked")
	public TestPagedReifier( Class graphClass, String name, ReificationStyle style ) 
	{ super( name ); }

	public static TestSuite suite() { return MetaTestGraph.suite( TestPagedReifier.class, LocalGraphRDB.class ); }

	public class LocalGraphRDB extends PagedGraphRDB
	{
		public LocalGraphRDB( ReificationStyle style )
		{ super( con, "testGraph-" + count ++, properties, styleRDB( style ), true ); }   
	} 

	public void setUp() 
	{ con = TestPagedConnection.makeAndCleanTestConnection(); 
	properties = con.getDefaultModelProperties().getGraph(); }

	public void tearDown() throws Exception 
	{ con.close(); }

	public Graph getGraph( ReificationStyle style )
	{ return new LocalGraphRDB( style ); }

	public Graph getGraph()
	{ return getGraph( ReificationStyle.Minimal ); }
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