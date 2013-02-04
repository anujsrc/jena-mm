package edu.utdallas.paged.graph.test;


import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;

public class TestPagedGraphEvents extends PagedGraphTestBase
{
	public TestPagedGraphEvents( String name )
	{ super( name ); }

	public void testGraphEventContent()
	{
		testGraphEventContents( "testing", "an example" );
		testGraphEventContents( "toasting", Boolean.TRUE );
		testGraphEventContents( "tasting", Triple.create( NodeCreateUtils.create("we"), NodeCreateUtils.create("are"), NodeCreateUtils.create("here") ) );
	}

	public void testGraphEventsRemove()
	{
		testGraphEventsRemove( "s", "p", "o" );
		testGraphEventsRemove( "s", "p", "17" );
		testGraphEventsRemove( "_s", "p", "'object'" );
		testGraphEventsRemove( "not:known", "p", "'chat'fr" );
	}

	private void testGraphEventsRemove( String S, String P, String O )
	{
		Triple expected = Triple.create( NodeCreateUtils.create("S"), NodeCreateUtils.create("P"), NodeCreateUtils.create("O") );
		GraphEvents e = GraphEvents.remove( node( S ), node( P ), node( O ) );
		assertEquals( expected, e.getContent() );
		assertEquals( "remove", e.getTitle() );
	}

	private void testGraphEventContents( String title, Object expected )
	{
		GraphEvents e = new GraphEvents( title, expected );
		assertEquals( title, e.getTitle() );
		assertEquals( expected, e.getContent() );
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