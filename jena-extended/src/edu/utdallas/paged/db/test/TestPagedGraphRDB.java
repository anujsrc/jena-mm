package edu.utdallas.paged.db.test;

import java.sql.SQLException;
import junit.framework.TestSuite;
import com.hp.hpl.jena.db.IDBConnection;
import edu.utdallas.paged.db.test.TestPagedConnection;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.test.MetaTestGraph;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.utdallas.paged.db.PagedGraphRDB;

public class TestPagedGraphRDB extends MetaTestGraph
{
	public TestPagedGraphRDB( String name )
	{ super( name ); }

	@SuppressWarnings("unchecked")
	public TestPagedGraphRDB( Class graphClass, String name, ReificationStyle style ) 
	{ super( graphClass, name, style ); }

	public static TestSuite suite()
	{ return MetaTestGraph.suite( TestPagedGraphRDB.class, LocalGraphRDB.class ); }

	private IDBConnection con;
	private int count = 0;
	private Graph properties;

	public void setUp()
	{ con = TestPagedConnection.makeAndCleanTestConnection();
	properties = con.getDefaultModelProperties().getGraph(); }

	public void tearDown() throws SQLException
	{ con.close(); }

	public class LocalGraphRDB extends PagedGraphRDB
	{
		public LocalGraphRDB( ReificationStyle style )
		{ super( con, "testGraph-" + count ++, properties, styleRDB( style ), true ); }   
	} 

	protected final class GraphRDBWithoutFind extends PagedGraphRDB
	{
		public GraphRDBWithoutFind()
		{
			super( con, "testGraph-" + count ++, properties, styleRDB( ReificationStyle.Minimal ), true );
		}

		@SuppressWarnings("unchecked")
		public ExtendedIterator graphBaseFind( TripleMatch t )
		{ throw new JenaException( "find is Not Allowed" ); }

		public void performDelete( Triple t )
		{ throw new JenaException( "delete is Not Allowed" ); }
	}

	public void testRemoveAllUsesClearNotDelete()
	{
		Graph g = new GraphRDBWithoutFind();
		graphAdd( g, "a P b; c Q d" );
		g.getBulkUpdateHandler().removeAll();
		assertEquals( 0, g.size() );
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