package edu.utdallas.paged.graph.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.util.iterator.*;

import junit.framework.*;

public class TestPagedGraphUtils extends PagedGraphTestBase
{
	public TestPagedGraphUtils(String name)
	{ super(name); }

	public static TestSuite suite()
	{ return new TestSuite( TestPagedGraphUtils.class ); }

	private static class Bool 
	{
		boolean value;
		Bool( boolean value ) { this.value = value; }
	}

	public void testFindAll()
	{
		final Bool foundAll = new Bool( false );
		Graph mock = new GraphBase() 
		{
			@SuppressWarnings("unchecked")
			public ExtendedIterator graphBaseFind( TripleMatch m )
			{ 
				Triple t = m.asTriple();
				assertEquals( Node.ANY, t.getSubject() ); 
				assertEquals( Node.ANY, t.getPredicate() );
				assertEquals( Node.ANY, t.getObject() );
				foundAll.value = true;
				return null;
			}
		};
		GraphUtil.findAll( mock );
		assertTrue( "find(ANY, ANY, ANY) called", foundAll.value );
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