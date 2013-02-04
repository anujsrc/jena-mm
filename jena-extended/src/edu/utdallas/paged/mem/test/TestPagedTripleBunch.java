package edu.utdallas.paged.mem.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.TripleBunch;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.utdallas.paged.graph.test.PagedGraphTestBase;
import edu.utdallas.paged.mem.PagedHashedTripleBunch;

public abstract class TestPagedTripleBunch extends PagedGraphTestBase
{
	protected static final Triple tripleSPO = triple( "s P o" );
	protected static final Triple tripleXQY = triple( "x Q y" );

	public TestPagedTripleBunch(String name)
	{ super( name ); }

	protected static final TripleBunch emptyBunch = new PagedHashedTripleBunch();

	protected abstract TripleBunch getBunch();

	public void testEmptyBunch()
	{
		TripleBunch b = getBunch();
		assertEquals( 0, b.size() );
		assertFalse( b.contains( tripleSPO ) );
		assertFalse( b.contains( tripleXQY ) );
		assertFalse( b.iterator().hasNext() );
	}

	public void testAddElement()
	{
		TripleBunch b = getBunch();
		b.add( tripleSPO );
		assertEquals( 1, b.size() );
		assertTrue( b.contains( tripleSPO ) );
		assertEquals( listOf( tripleSPO ), iteratorToList( b.iterator() ) );
	}

	public void testAddElements()
	{
		TripleBunch b = getBunch();
		b.add( tripleSPO );
		b.add( tripleXQY );
		assertEquals( 2, b.size() );
		assertTrue( b.contains( tripleSPO ) );
		assertTrue( b.contains( tripleXQY ) );
		assertEquals( setOf( tripleSPO, tripleXQY ), iteratorToSet( b.iterator() ) );
	}

	public void testRemoveOnlyElement()
	{
		TripleBunch b = getBunch();
		b.add( tripleSPO );
		b.remove( tripleSPO );
		assertEquals( 0, b.size() );
		assertFalse( b.contains( tripleSPO ) );
		assertFalse( b.iterator().hasNext() );
	}

	public void testRemoveFirstOfTwo()
	{
		TripleBunch b = getBunch();
		b.add( tripleSPO );
		b.add( tripleXQY );
		b.remove( tripleSPO );
		assertEquals( 1, b.size() );
		assertFalse( b.contains( tripleSPO ) );
		assertTrue( b.contains( tripleXQY ) );
		assertEquals( listOf( tripleXQY ), iteratorToList( b.iterator() ) );
	}

	public void testTableGrows()
	{
		TripleBunch b = getBunch();
		b.add( tripleSPO );
		b.add( tripleXQY );
		b.add( triple( "a I b" ) );
		b.add( triple( "c J d" ) );
	}

	public void testIterator()
	{
		TripleBunch b = getBunch();
		b.add( triple( "a P b" ) );
		b.add( triple( "c Q d" ) );
		b.add( triple( "e R f" ) );
		assertEquals( tripleSet( "a P b; c Q d; e R f" ), b.iterator().toSet() );
	}

	@SuppressWarnings("unchecked")
	public void testIteratorRemoveOneItem()
	{
		TripleBunch b = getBunch();
		b.add( triple( "a P b" ) );
		b.add( triple( "c Q d" ) );
		b.add( triple( "e R f" ) );
		ExtendedIterator it = b.iterator();
		while (it.hasNext()) if (it.next().equals( triple( "c Q d") )) it.remove();
		assertEquals( tripleSet( "a P b; e R f" ), b.iterator().toSet() );
	}

	@SuppressWarnings("unchecked")
	public void testIteratorRemoveAlltems()
	{
		TripleBunch b = getBunch();
		b.add( triple( "a P b" ) );
		b.add( triple( "c Q d" ) );
		b.add( triple( "e R f" ) );
		ExtendedIterator it = b.iterator();
		while (it.hasNext()) it.removeNext();
		assertEquals( tripleSet( "" ), b.iterator().toSet() );
	}

	@SuppressWarnings("unchecked")
	protected List listOf( Triple x )
	{
		List result = new ArrayList();
		result.add( x );
		return result;
	}

	@SuppressWarnings("unchecked")
	protected Set setOf( Triple x, Triple y )
	{
		Set result = setOf( x );
		result.add( y );
		return result;
	}

	@SuppressWarnings("unchecked")
	protected Set setOf( Triple x )
	{
		Set result = new HashSet();
		result.add( x );
		return result;
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