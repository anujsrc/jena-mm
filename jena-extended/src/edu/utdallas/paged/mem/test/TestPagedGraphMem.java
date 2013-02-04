package edu.utdallas.paged.mem.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.utdallas.paged.graph.ExtendedFactory;
import edu.utdallas.paged.mem.graph.impl.PagedGraph;

public class TestPagedGraphMem extends AbstractTestPagedGraphMem
{
	public TestPagedGraphMem(String name)
	{ super(name); }

	public static TestSuite suite()
	{ return new TestSuite( TestPagedGraphMem.class ); }

	public Graph getGraph()
	{ return ExtendedFactory.createPagedGraphMem(ReificationStyle.Minimal); }

	public void testRemoveAllDoesntUseFind()
	{
		Graph g = new GraphMemWithoutFind(ReificationStyle.Minimal);
		graphAdd( g, "x P y; a Q b" );
		g.getBulkUpdateHandler().removeAll();
		assertEquals( 0, g.size() );
	}

	@SuppressWarnings("unchecked")
	public void testSizeAfterRemove() 
	{
		Graph g = getGraph();
		graphAdd( g, "x p y" );
		ExtendedIterator it = g.find( triple( "x ?? ??" ) );
		it.removeNext();
		assertEquals( 0, g.size() );        
	}

	public void testContainsConcreteDoesntUseFind()
	{
		Graph g = new GraphMemWithoutFind(ReificationStyle.Minimal);
		graphAdd( g, "x P y; a Q b" );
		assertTrue( g.contains( triple( "x P y" ) ) );
		assertTrue( g.contains( triple( "a Q b" ) ) );
		assertFalse( g.contains( triple( "a P y" ) ) );
		assertFalse( g.contains( triple( "y R b" ) ) );
	}    

	protected final class GraphMemWithoutFind extends PagedGraph
	{
		public GraphMemWithoutFind(ReificationStyle style) 
		{ super(style);	}

		@SuppressWarnings("unchecked")
		public ExtendedIterator graphBaseFind( TripleMatch t )
		{ throw new JenaException( "find is Not Allowed" ); }
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