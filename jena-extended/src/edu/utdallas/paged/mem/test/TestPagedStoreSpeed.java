package edu.utdallas.paged.mem.test;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.utdallas.paged.graph.ExtendedFactory;

public class TestPagedStoreSpeed extends GraphTestBase
{
	private long began;

	public TestPagedStoreSpeed( String name )
	{
		super( name );
	}

	public static void main( String [] args )
	{
		new TestPagedStoreSpeed( "vladimir taltos" ) .gonzales( "subject PagedStoreMem", ExtendedFactory.createPagedGraphMem() );
		new TestPagedStoreSpeed( "vladimir taltos" ) .gonzales( "normal PagedStoreMem", ExtendedFactory.createPagedGraphMem() );
		new TestPagedStoreSpeed( "vladimir taltos" ) .gonzales( "PagedGraphMem", ExtendedFactory.createPagedGraphMem() );
		new TestPagedStoreSpeed( "vladimir taltos" ) .gonzales( "subject PagedStoreMem", ExtendedFactory.createPagedGraphMem() );
		new TestPagedStoreSpeed( "vladimir taltos" ) .gonzales( "normal PagedStoreMem", ExtendedFactory.createPagedGraphMem() );
		new TestPagedStoreSpeed( "vladimir taltos" ) .gonzales( "PagedGraphMem", ExtendedFactory.createPagedGraphMem() );
		new TestPagedStoreSpeed( "vladimir taltos" ) .gonzales( "subject PagedStoreMem", ExtendedFactory.createPagedGraphMem() );
		new TestPagedStoreSpeed( "vladimir taltos" ) .gonzales( "normal PagedStoreMem", ExtendedFactory.createPagedGraphMem() );
		new TestPagedStoreSpeed( "vladimir taltos" ) .gonzales( "PagedGraphMem", ExtendedFactory.createPagedGraphMem() );
	}

	private void mark()
	{
		began = System.currentTimeMillis();
	}

	static final int COUNT = 100000;

	private Triple newt( int i )
	{ return new Triple( node("s" + (i % 1000)), node("p" + ((i + 11) % 20)), node("s" + ((i + 131) % 1001) ) ); }

	private void makeTriples()
	{ for (int i = 0; i < COUNT; i += 1) newt( i ); }

	private void fillGraph( Graph g )
	{
		for (int i = 0; i < COUNT; i += 1) g.add( newt( i ) );
	}

	private long ticktock( String title )
	{
		long ticks = System.currentTimeMillis() - began;
		System.err.println( "+ " + title + " took: " + ticks + "ms." );
		mark();
		return ticks;
	}

	@SuppressWarnings("unchecked")
	private void consumeAll( Graph g )
	{
		int count = 0;
		ClosableIterator it = g.find( node("s500"), null, null );
		while (it.hasNext()) { it.next(); count += 1; /* if (count %1000 == 0) System.err.print( (count / 1000) %10 ); */}
		// System.err.println( "| we have " + count + " triples." );
		// assertEquals( g.size(), count );
	}

	private void gonzales( String title, Graph g )
	{
		System.err.println( "" );
		System.err.println( "| gonzales: " + title );
		mark(); 
		makeTriples(); ticktock( "generating triples" );
		makeTriples(); ticktock( "generating triples again" );
		makeTriples(); long gen = ticktock( "generating triples yet again" );
		fillGraph( g ); long fill = ticktock( "filling graph" );
		System.err.println( "> insertion time: " + (fill - gen) );
		fillGraph( g ); ticktock( "adding the same triples again" );
		consumeAll( g ); ticktock( "counting s500 triples" );
		consumeAll( g ); ticktock( "counting s500 triples again" );
		consumeAll( g ); ticktock( "counting s500 triples yet again" );
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