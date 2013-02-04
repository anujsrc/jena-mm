package edu.utdallas.paged.graph.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Graph;

import edu.utdallas.paged.graph.ExtendedFactory;

public class TestPagedCapabilities extends PagedGraphTestBase
{
	protected final class AllFalse implements Capabilities
	{
		public boolean sizeAccurate()
		{ return false; }

		public boolean addAllowed()
		{ return false; }

		public boolean addAllowed( boolean everyTriple )
		{ return false; }

		public boolean deleteAllowed()
		{ return false; }

		public boolean deleteAllowed( boolean everyTriple )
		{ return false; }

		public boolean iteratorRemoveAllowed()
		{ return false; }

		public boolean canBeEmpty()
		{ return false; }

		public boolean findContractSafe()
		{ return false; }

		public boolean handlesLiteralTyping()
		{ return false; }
	}

	public TestPagedCapabilities( String name )
	{ super( name ); }

	public static TestSuite suite()
	{ return new TestSuite( TestPagedCapabilities.class ); }   

	/**
      pending on use-cases.
	 */
	public void testTheyreThere()
	{
		Graph g = ExtendedFactory.createPagedGraphMem();
		g.getCapabilities();
	}

	public void testCanConstruct()
	{
		@SuppressWarnings("unused")
		Capabilities c = new AllFalse();
	}

	public void testCanAccess()
	{
		Capabilities c = new AllFalse();
		@SuppressWarnings("unused")
		boolean b = false;
		b = c.addAllowed();
		b = c.addAllowed( true );
		b = c.canBeEmpty();
		b = c.deleteAllowed();
		b = c.deleteAllowed( false );
		b = c.sizeAccurate();
		b = c.iteratorRemoveAllowed();
		b = c.findContractSafe();
		b = c.handlesLiteralTyping();
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