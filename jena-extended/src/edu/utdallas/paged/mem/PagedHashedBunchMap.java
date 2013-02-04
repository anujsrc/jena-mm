package edu.utdallas.paged.mem;

import com.hp.hpl.jena.mem.BunchMap;
import com.hp.hpl.jena.mem.TripleBunch;
import com.hp.hpl.jena.shared.BrokenException;

/**
 * An implementation of BunchMap that does open-addressed hashing.
 * @author vaibhav
 */
@SuppressWarnings("unchecked")
public class PagedHashedBunchMap extends PagedHashCommon implements BunchMap
{
	/** the triple bunch that holds the triples **/
	protected TripleBunch [] values;

	/** Constructor	**/
	public PagedHashedBunchMap()
	{
		super( 10 );
		values = new TripleBunch[capacity];
	}

	/**
	 * @see com.hp.hpl.jena.mem.BunchMap#clear()
	 */
	public void clear()
	{
		size = 0;
		for (int i = 0; i < capacity; i += 1) keys[i] = values[i] = null; 
	}  

	/**
	 * @see com.hp.hpl.jena.mem.BunchMap#size()
	 */
	public long size()
	{ return size; }

	/**
	 * @see com.hp.hpl.jena.mem.BunchMap#get(Object)
	 */
	public TripleBunch get( Object key )
	{
		int slot = findPagedSlot( key );
		return slot < 0 ? values[~slot] : null;
	}

	/**
	 * @see com.hp.hpl.jena.mem.BunchMap#put(Object, TripleBunch)
	 */
	public void put( Object key, TripleBunch value )
	{
		int slot = findPagedSlot( key );
		if (slot < 0)
			values[~slot] = value;
		else
		{
			keys[slot] = key;
			values[slot] = value; 
			size += 1;
			if (size == threshold) grow();
		}
	}

	/**
	 * Method to increase the capacity of the bunch map
	 */
	protected void grow()
	{
		Object [] oldContents = keys;
		TripleBunch [] oldValues = values;
		final int oldCapacity = capacity;
		growCapacityAndThreshold();
		keys = new Object[capacity];
		values = new TripleBunch[capacity];
		for (int i = 0; i < oldCapacity; i += 1)
		{
			Object key = oldContents[i];
			if (key != null) 
			{
				int j = findPagedSlot( key );
				if (j < 0) 
				{
					throw new BrokenException( "oh dear, already have a slot for " + key  + ", viz " + ~j );
				}
				keys[j] = key;
				values[j] = oldValues[i];
			}
		}
	}

	/**
	 * Method that removes the triples for a given position in the map
	 */
	 protected void removeAssociatedValues( int here )
	 { values[here] = null; }

	 /**
        Called by HashCommon when a key is moved: move the
        associated element of the <code>values</code> array.
	  */
	 protected void moveAssociatedValues( int here, int scan )
	 { values[here] = values[scan]; }
	 
	 /** Set the triplebunch at the specified location with the given value */
	 public void setTripleBunch( int index, TripleBunch value ) { values[index] = value; }

	 /** Set the key at the specified index with the value given */
	 public void setKey( int index, Object value ) { keys[index] = value; }

	 /** Set the size of the bunchmap */
	 public void setSize() { size = size - 1; }

	 @Override protected Object[] newKeyArray( int size )
     { return new Object[size]; }
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