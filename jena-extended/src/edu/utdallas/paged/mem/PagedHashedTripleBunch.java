package edu.utdallas.paged.mem;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.StageElement;
import com.hp.hpl.jena.mem.MatchOrBind;
import com.hp.hpl.jena.mem.TripleBunch;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @see com.hp.hpl.jena.mem.HashedTripleBunch
 * @author vaibhav
 */
public class PagedHashedTripleBunch extends PagedHashCommon implements TripleBunch
{
	/** Constructor **/
	public PagedHashedTripleBunch()
	{ super( nextSize( 0 ) ); }
	
	/** Constructor **/
	@SuppressWarnings("unchecked")
	public PagedHashedTripleBunch( TripleBunch b )
	{
        super( nextSize( (int) (b.size() / loadFactor) ) );
        for (Iterator it = b.iterator(); it.hasNext();) add( (Triple) it.next() );        
        changes = 0;		
	}

	/**
	 * remove all triples from this triple bunch
	 */
	public void removeAll()
	{
		Object [] oldContents = keys;
		final int oldCapacity = capacity;
		for (int i = 0; i < oldCapacity; i += 1)
		{
			Triple t = (Triple)oldContents[i];
			if (t != null) 
			{
				removeFrom(~findPagedSlot( t ));
				changes += 1;
			}
		}
	}

	/**
	 * @see com.hp.hpl.jena.mem.TripleBunch#contains(Triple)
	 */
	public boolean contains( Triple t )
	{ return findPagedSlot( t ) < 0; }    

	
	protected int findSlotBySameValueAs( Triple key )
	{
        int index = initialIndexFor( key ), j = 0;
       	while (true)
        {
       		Object current = keys[index];
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
       		if (key.matches( (Triple)current )) return ~index;
       		if (--index < 0) index += capacity;
        }  
	}

	/**
	 * @see com.hp.hpl.jena.mem.TripleBunch#containsBySameValueAs(Triple)
	 */
	public boolean containsBySameValueAs( Triple t )
	{ return findSlotBySameValueAs( t ) < 0; }

	/**
    	Answer the number of items currently in this TripleBunch. 
    	@see com.hp.hpl.jena.mem.TripleBunch#size()
	 */
	public int size()
	{ return size; }

	/**
    	Answer the current capacity of this PagedHashedTripleBunch; for testing purposes
    	only. [Note that the bunch is resized when it is more than half-occupied.] 
	 */
	public int currentCapacity()
	{ return capacity; }

	/**
	 * @see com.hp.hpl.jena.mem.TripleBunch#add(Triple)
	 */
	public void add( Triple t )
	{
		keys[findPagedSlot( t )] = t;
		changes += 1;
		if (++size > threshold) grow();
	}

	/**
	 * Method that increases the capacity of this triple bunch
	 */
	@SuppressWarnings("unchecked")
	protected void grow()
	{
		Object [] oldContents = keys;
		final int oldCapacity = capacity;
		growCapacityAndThreshold();
		Object [] newKeys = keys = new Triple[capacity];
		for (int i = 0; i < oldCapacity; i += 1)
		{
			Object t = oldContents[i];
			if (t != null) newKeys[findPagedSlot( t )] = t;
		}
	}

	/**
	 * @see com.hp.hpl.jena.mem.TripleBunch#remove(Triple)
	 */
	public void remove( Triple t )
	{
		removeFrom( ~findPagedSlot( t ) );
		changes += 1;
	}

	/**
	 * @see com.hp.hpl.jena.mem.TripleBunch#iterator()
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator iterator()
	{ return iterator( NotifyEmpty.ignore ); }

	/**
	 * @see com.hp.hpl.jena.mem.TripleBunch#iterator(NotifyEmpty)
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator iterator( final NotifyEmpty container )
	{ return keyIterator( container ); }

	/**
	 * @see com.hp.hpl.jena.mem.TripleBunch#app(Domain, StageElement, MatchOrBind)
	 */
	public void app( Domain d, StageElement next, MatchOrBind s )
	{
		int i = capacity, initialChanges = changes;
		while (i > 0)
		{
			if (changes > initialChanges) throw new ConcurrentModificationException();
			Object t = keys[--i];
			if (t != null  && s.matches( (Triple) t )) next.run(d);
		}
	}

	@Override protected Triple[] newKeyArray( int size )
    { return new Triple[size]; }
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