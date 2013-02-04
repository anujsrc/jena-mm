package edu.utdallas.paged.mem;

import java.util.ConcurrentModificationException;
import java.util.List;

import com.hp.hpl.jena.mem.HashCommon;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.util.iterator.NiceIterator;

import edu.utdallas.paged.shared.impl.ExtendedJenaParameters;

/**
 * An abstract class that extends Jena's HashCommon class
 * @author vaibhav
 */
@SuppressWarnings("unchecked")
public abstract class PagedHashCommon extends HashCommon
{
	/**
	 * Check these many previous buckets to make sure that a node written to disk does
	 * not have a new incarnation in memory at a different bucket
	 */
	public int searchConstant = 30;

	/**
	 * Constructor
	 * @see com.hp.hpl.jena.mem.HashCommon
	 */
	protected PagedHashCommon( int initialCapacity )
	{ super( initialCapacity ); if( ExtendedJenaParameters.searchConstant > 0 ) this.searchConstant = ExtendedJenaParameters.searchConstant; }

	/**
    	Search for the slot in which <code>key</code> is found. If it is absent,
    	return the index of the free slot in which it could be placed. If it is present,
    	return the bitwise complement of the index of the slot it appears in. Hence
    	negative values imply present, positive absent, and there's no confusion
    	around 0.
	 */
	public final int findPagedSlot( Object key )
	{
		int index = initialIndexFor( key ), j = 0;
		while (true)
		{
			Object current = keys[index];

			//if we don't find the index make sure it is not in the previous
			//searchConstant number of buckets
			if (current == null)
			{
				int addIndex = index, pos = index;
				while(j++<searchConstant && capacity > searchConstant)
				{
					if (--pos < 0) pos += capacity;
					Object addCurrent = keys[pos];
					if (key.equals( addCurrent )) return ~pos;
				}
				return addIndex;
			}
			if (key.equals( current )) return ~index;
			if (--index < 0) index += capacity;
		}  
	}   

	/**
	 * @see com.hp.hpl.jena.mem.HashCommon#remove(Object)
	 */
	public void remove( Object key )
	{
		int slot = findPagedSlot( key );
		if (slot < 0) removeFrom( ~slot );
	}

	/**
	 	Copied from com.hp.hpl.jena.mem.HashCommon
	 <p>	
    	Remove the triple at element <code>i</code> of <code>contents</code>.
    	This is an implementation of Knuth's Algorithm R from tAoCP vol3, p 527,
    	with exchanging of the roles of i and j so that they can be usefully renamed
    	to <i>here</i> and <i>scan</i>.
	<p>
    	It relies on linear probing but doesn't require a distinguished REMOVED
    	value. Since we resize the table when it gets fullish, we don't worry [much]
    	about the overhead of the linear probing.
	<p>	
    	Iterators running over the keys may miss elements that are moved from the
    	top of the table to the bottom because of Iterator::remove. removeFrom
    	returns such a moved key as its result, and null otherwise.
	*/
	protected Object removeFrom( int here )
	{
		final int original = here;
		Object wrappedAround = null;
		size -= 1;
		while (true)
		{
			keys[here] = null;
			removeAssociatedValues( here );
			int scan = here;
			while (true)
			{
				if (--scan < 0) scan += capacity;
				Object key = keys[scan];
				if (key == null) return wrappedAround;
				int r = initialIndexFor( key );
				if (scan <= r && r < here || r < here && here < scan || here < scan && scan <= r)
				{ /* Nothing. We'd have preferred an `unless` statement. */}
				else
				{
					// System.err.println( ">> move from " + scan + " to " + here + " [original = " + original + ", r = " + r + "]" );
					if (here <= original && scan > original) 
					{
						// System.err.println( "]] recording wrapped " );
						wrappedAround = keys[scan];
					}
					keys[here] = keys[scan];
					moveAssociatedValues( here, scan );
					here = scan;
					break;
				}
			}
		}
	}    

	/**
	 * 	Copied from com.hp.hpl.jena.mem.HashCommon
	 */
	void showkey()
	{
		if (false)
		{
			System.err.print( ">> KEYS:" );
			for (int i = 0; i < capacity; i += 1)
				if (keys[i] != null) System.err.print( " " + initialIndexFor( keys[i] ) + "@" + i + "::" + keys[i] );
			System.err.println();
		}
	}

	/**
	  	Copied from com.hp.hpl.jena.mem.HashCommon
	 <p>	
    	The MovedKeysIterator iterates over the elements of the <code>keys</code>
    	list. It's not sufficient to just use List::iterator, because the .remove
    	method must remove elements from the hash table itself.
	 <p>
    	Note that the list supplied on construction will be empty: it is filled before
    	the first call to <code>hasNext()</code>.
	*/
	protected final class MovedKeysIterator extends NiceIterator
	{
		private final List keys;

		protected int index = 0;
		final int initialChanges;
		final NotifyEmpty container;

		protected MovedKeysIterator( int initialChanges, NotifyEmpty container, List keys )
		{ 
			this.keys = keys; 
			this.initialChanges = initialChanges; 
			this.container = container;
		}

		public boolean hasNext()
		{ 
			if (changes > initialChanges) throw new ConcurrentModificationException();
			return index < keys.size(); 
		}

		public Object next()
		{
			if (changes > initialChanges) throw new ConcurrentModificationException();
			if (hasNext() == false) noElements( "" );
			return keys.get( index++ );
		}

		public void remove()
		{ 
			if (changes > initialChanges) throw new ConcurrentModificationException();
			PagedHashCommon.this.remove( keys.get( index - 1 ) ); 
			if (size == 0) container.emptied();
		}
	}

	/**
	 	Copied from com.hp.hpl.jena.mem.HashCommon
	 <p>	
    	The BasicKeyIterator iterates over the <code>keys</code> array.
    	If a .remove call moves an unprocessed key underneath the iterator's
    	index, that key value is added to the <code>movedKeys</code>
    	list supplied to the constructor.
	*/
	protected final class BasicKeyIterator extends NiceIterator
	{
		protected final List movedKeys;

		int index = 0;
		final int initialChanges;
		final NotifyEmpty container;

		protected BasicKeyIterator( int initialChanges, NotifyEmpty container, List movedKeys )
		{ 
			this.movedKeys = movedKeys; 
			this.initialChanges = initialChanges;  
			this.container = container;
		}

		public boolean hasNext()
		{
			if (changes > initialChanges) throw new ConcurrentModificationException();
			while (index < capacity && keys[index] == null) index += 1;
			return index < capacity;
		}

		public Object next()
		{
			if (changes > initialChanges) throw new ConcurrentModificationException();
			if (hasNext() == false) noElements( "HashCommon keys" );
			return keys[index++];
		}

		public void remove()
		{
			if (changes > initialChanges) throw new ConcurrentModificationException();
			// System.err.println( ">> keyIterator::remove, size := " + size +
			// ", removing " + keys[index + 1] );
			Object moved = removeFrom( index - 1 );
			if (moved != null) movedKeys.add( moved );
			if (size == 0) container.emptied();
			if (size < 0) throw new BrokenException( "BROKEN" );
			showkey();
		}
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