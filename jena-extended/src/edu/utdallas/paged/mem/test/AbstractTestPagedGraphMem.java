package edu.utdallas.paged.mem.test;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.SimpleReifier;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.utdallas.paged.graph.ExtendedFactory;

public class AbstractTestPagedGraphMem extends GraphTestBase
{
	public AbstractTestPagedGraphMem(String name)
	{ super( name ); }

	public Graph getGraph() { return ExtendedFactory.createPagedGraphMem(ReificationStyle.Minimal); }

	public void testClosesReifier()
	{
		Graph g = getGraph();
		SimpleReifier r = (SimpleReifier) g.getReifier();
		g.close();
		assertTrue( r.isClosed() );
	}

	@SuppressWarnings("unchecked")
	public void testBrokenIndexes()
	{
		Graph g = getGraph();
		graphAdd( g, "x R y; x S z" );
		ExtendedIterator it = g.find( Node.ANY, Node.ANY, Node.ANY );
		it.removeNext(); it.removeNext();
		assertFalse( g.find( node( "x" ), Node.ANY, Node.ANY ).hasNext() );
		assertFalse( g.find( Node.ANY, node( "R" ), Node.ANY ).hasNext() );
		assertFalse( g.find( Node.ANY, Node.ANY, node( "y" ) ).hasNext() );
	}   

	@SuppressWarnings("unchecked")
	public void testBrokenSubject()
	{
		Graph g = getGraph();
		graphAdd( g, "x brokenSubject y" );
		ExtendedIterator it = g.find( node( "x" ), Node.ANY, Node.ANY );
		it.removeNext();
		assertFalse( g.find( Node.ANY, Node.ANY, Node.ANY ).hasNext() );
	}

	@SuppressWarnings("unchecked")
	public void testBrokenPredicate()
	{
		Graph g = getGraph();
		graphAdd( g, "x brokenPredicate y" );
		ExtendedIterator it = g.find( Node.ANY, node( "brokenPredicate"), Node.ANY );
		it.removeNext();
		assertFalse( g.find( Node.ANY, Node.ANY, Node.ANY ).hasNext() );
	}

	@SuppressWarnings("unchecked")
	public void testBrokenObject()
	{
		Graph g = getGraph();
		graphAdd( g, "x brokenObject y" );
		ExtendedIterator it = g.find( Node.ANY, Node.ANY, node( "y" ) );
		it.removeNext();
		assertFalse( g.find( Node.ANY, Node.ANY, Node.ANY ).hasNext() );
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

	public void testUnnecessaryMatches() 
	{
		Node special = new Node_URI( "eg:foo" ) 
		{
			public boolean matches( Node s ) 
			{
				fail( "Matched called superfluously." );
				return true;
			}
		};
		Graph g = getGraph();
		graphAdd( g, "x p y" );
		g.add( new Triple( special, special, special ) );
		exhaust( g.find( special, Node.ANY, Node.ANY ) );
		exhaust( g.find( Node.ANY, special, Node.ANY ) );
		exhaust( g.find( Node.ANY, Node.ANY, special ) );
	}

	@SuppressWarnings("unchecked")
	protected void exhaust( Iterator it )
	{ while (it.hasNext()) it.next(); }
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