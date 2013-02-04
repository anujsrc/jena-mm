package edu.utdallas.paged.mem.test;

import java.util.*;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.*;

public class TestMixedPagedGraphMem extends TestPagedGraphMem
{
	public TestMixedPagedGraphMem( String name ) 
	{ super( name );}

	public static TestSuite suite()
	{ return new TestSuite( TestMixedPagedGraphMem.class ); }

	public Graph getGraph()
	{ return new MixedGraphMem(); }

	@SuppressWarnings("unchecked")
	public void testRepeatedAddSuppressesPredicateAndObject()
	{
		final List history = new ArrayList();
		MixedGraphMemStore t = new MixedGraphMemStore( getGraph() )
		{
			protected boolean add( Node key, Triple t )
			{
				history.add( key );
				return super.add( key, t );
			}
		};
		t.add( triple( "s P o" ) );
		assertEquals( nodeList( "s P o" ), history );
		t.add( triple( "s P o" ) );
		assertEquals( nodeList( "s P o s" ), history );
	}

	public void testUnnecessaryMatches() { 
		/* test not appropriate for subclass */ 
	}
	@SuppressWarnings("unchecked")
	public void testRemoveAbsentSuppressesPredicateAndObject()
	{
		final List history = new ArrayList();
		MixedGraphMemStore t = new MixedGraphMemStore( getGraph() )
		{
			protected boolean remove( Node key, Triple t )
			{
				history.add( key );
				return super.remove( key, t );
			}
		};
		t.remove( triple( "s P o" ) );
		assertEquals( nodeList( "s" ), history );
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