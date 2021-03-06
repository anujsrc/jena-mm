package edu.utdallas.paged.mem.test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.TripleBunch;

import edu.utdallas.paged.mem.PagedHashedTripleBunch;

public class TestPagedHashedTripleBunch extends TestPagedTripleBunch
{

	public TestPagedHashedTripleBunch( String name )
	{ super( name ); }

	protected static class HTB extends PagedHashedTripleBunch
	{
		public HTB( TripleBunch b )
		{ super( b ); }

		protected int improveHashCode( int hashCode )
		{ return hashCode; }    
	}

	public TripleBunch getBunch()
	{ return new PagedHashedTripleBunch( emptyBunch ); }

	PagedHashedTripleBunch htb = new HTB( emptyBunch ); 

	static class TripleWithHash extends Triple
	{
		final int hash;

		TripleWithHash( int hash, Node s, Node p, Node o )
		{
			super( s, p, o );
			this.hash = hash;
		}

		public static TripleWithHash create( int n, String s )
		{
			Triple t = triple( s );
			return new TripleWithHash( n, t.getSubject(), t.getPredicate(), t.getObject() );            
		}

		public int hashCode()
		{ return hash; }
	}

	public void testHashcodeUsedAsIndex()
	{
		PagedHashedTripleBunch htb = new HTB( emptyBunch );
		int limit = htb.currentCapacity();
		for (int i = 0; i < limit; i += 1)
		{
			TripleWithHash t = TripleWithHash.create( i, "s p o"+i );
			htb.add( t );
			assertSame( t, htb.getItemForTestingAt( i ) );
		}
	}

	public void testRemovePerformsShiftFromTop()
	{
		int capacity = htb.currentCapacity();
		testRemovePerformsShift( capacity - 1, capacity );
	}

	public void testRemovePerformsShiftFromMiddle()
	{
		int capacity = htb.currentCapacity();
		testRemovePerformsShift( capacity - 3, capacity );
	}

	public void testRemovePerformsShiftWrappingLowestTwo()
	{
		int capacity = htb.currentCapacity();
		testRemovePerformsShift( 0, capacity );
	}
	public void testRemovePerformsShiftWrappingLowest()
	{
		int capacity = htb.currentCapacity();
		testRemovePerformsShift( 1, capacity );
	}

	private void testRemovePerformsShift( int most, int capacity )
	{
		int next = most - 1; if (next < 0) next += capacity;
		int least = most - 2; if (least < 0) least += capacity;
		TripleWithHash t1 = TripleWithHash.create( most, "s p o" );
		TripleWithHash t2 = TripleWithHash.create( next, "a b c" );
		TripleWithHash t3 = TripleWithHash.create( most, "x y z" );
		htb.add( t1 );
		htb.add( t2 );
		htb.add( t3 );
		assertSame( t1, htb.getItemForTestingAt( most ) );
		assertSame( t2, htb.getItemForTestingAt( next ) );
		assertSame( t3, htb.getItemForTestingAt( least ) );
		//
		htb.remove( t1 );
		assertSame( t3, htb.getItemForTestingAt( most ) );
		assertSame( t2, htb.getItemForTestingAt( next ) );
		assertSame( null, htb.getItemForTestingAt( least ) );
	}

	public void testIteratorRemovePerformsShiftAndDeliversElementFromTop()
	{
		int capacity = htb.currentCapacity();
		testIteratorRemovePerformsShiftAndDeliversElement( capacity - 1, capacity );
	}

	public void testIteratorRemovePerformsShiftAndDeliversElementFromMiddle()
	{
		int capacity = htb.currentCapacity();
		testIteratorRemovePerformsShiftAndDeliversElement( capacity - 3, capacity );
	}

	//public void testIteratorRemovePerformsShiftAndDeliversElementWrappingLowest()
	//    {
	//    int capacity = htb.currentCapacity();
	//    testIteratorRemovePerformsShiftAndDeliversElement( 1, capacity );
	//    }
	//
	//public void testIteratorRemovePerformsShiftAndDeliversElementWrappingLowestTwo()
	//    {
	//    int capacity = htb.currentCapacity();
	//    testIteratorRemovePerformsShiftAndDeliversElement( 0, capacity );
	//    }

	private void testIteratorRemovePerformsShiftAndDeliversElement( int most, int capacity )
	{
		//    int next = most - 1; if (next < 0) next += capacity;
		//    int least = most - 2; if (least < 0) least += capacity;
		//    TripleWithHash t1 = TripleWithHash.create( most, "s p o" );
		//    TripleWithHash t2 = TripleWithHash.create( next, "a b c" );
		//    TripleWithHash t3 = TripleWithHash.create( most, "x y z" );
		//    htb.add( t1 );
		//    htb.add( t2 );
		//    htb.add( t3 );
		//    ExtendedIterator it = htb.iterator();
		//    assertSame( t1, it.next() );
		//    it.remove();
		//    assertSame( t3, it.next() );
		//    assertSame( t2, it.next() );
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