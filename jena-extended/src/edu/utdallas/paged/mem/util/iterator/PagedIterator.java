package edu.utdallas.paged.mem.util.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskBase;
import edu.utdallas.paged.sdb.graph.PagedGraphSDB;
import edu.utdallas.paged.sdb.core.sqlnode.PagedSqlSelectBlock;
import edu.utdallas.paged.sdb.graph.PagedGraphQueryHandlerSDB.BindingQueryPlanSDB;

/**
 * The iterator that is used to iterate over memory as well as triples from the lucene indexes
 * @author vaibhav
 */
@SuppressWarnings("unchecked")
public class PagedIterator implements Iterator
{
    /**
     * instance of class that is used to read the triples from file if any exist
     */
    public PagedGraphTripleStoreDiskBase f;
    
    /**
     * the underlying iterator  
     */
 	private Iterator underlying;
 	
 	/**
 	 * Flag to check if a node is in memory or not
 	 */
    private boolean isInMemory = false;
    
    /**
     * flag to check if the current search for nodes is a specific one
     */
    public static boolean isSpecificSearch = false;
    
    /**
     * flag to check if the current search for nodes is for all nodes
     */
    public static boolean isAllNodeSearch = false;
    
    /**
     * flag similar to isInMemory
     * @see edu.utdallas.paged.mem.util.iterator.PagedIterator#isInMemory
     */
    public static boolean foundInMem = false;

    /**
     * instance of sdb query plan
     */
    public BindingQueryPlanSDB qPlan = null;
    
    /**
     * flag to check if we are executing a SDB query
     */
    public boolean isSDB = false;
    
    /** constructor using the base iterator and a lucene searching instance */
	public PagedIterator( Iterator underlying, PagedGraphTripleStoreDiskBase fBase)
    {   
        this.f = fBase;  		
        this.underlying = underlying ;
        if (fBase != null && underlying != null && ( underlying.hasNext() || PagedIterator.foundInMem ) && !PagedIterator.isAllNodeSearch && ( f.tripleMatch.asTriple().getSubject().isConcrete() || f.tripleMatch.asTriple().getPredicate().isConcrete() || f.tripleMatch.asTriple().getObject().isConcrete() ) ) isInMemory = true;
    }

	/** constructor using the base iterator and a sdb query plan */
	public PagedIterator( Iterator underlying, BindingQueryPlanSDB qPlan, boolean isSDB )
    {   
        this.underlying = underlying ;
        this.qPlan = qPlan;
        this.isSDB = isSDB;
    }
	
	/**
	 * Check if the iterator has any more values
	 */
    public boolean hasNext()
    { 
        boolean hasValue = false;
        if (this.underlying != null && this.underlying.hasNext()) { hasValue = true; }
        else
        { 
            try
            {
            	if( !this.underlying.hasNext() ) return false;

            	//check if this iterator is for a sdb query
            	if( this.isSDB )
            	{
            		//increment the offset
            		if (PagedSqlSelectBlock.isStarted) { PagedSqlSelectBlock.start += PagedGraphSDB.length; }
            		else { PagedSqlSelectBlock.start = 0; PagedSqlSelectBlock.isStarted = false; return false; }
            		
            		//execute the query again to get the next part of the result set 
            		this.underlying = qPlan.executeBindings();
            	}
            	else
            	{
            		if(this.f.readLine || this.isInMemory || PagedIterator.isSpecificSearch) return false;
            		this.underlying = f.find();    //try to return an iterator based on the triples read from the lucene index if any
            	}
                if(this.underlying == null) hasValue = false;  //if no triples exist return false
                else
                    if(this.underlying.hasNext()) hasValue = true; //else true
            }
            catch(Exception e){hasValue = false;}   
        }
        return hasValue;
    }

    /**
     * Return the next value in this iterator
     */
    public Object next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException(PagedIterator.class.getName()) ;
        return this.underlying.next() ; 
    }

    /**
     * Call the underlying iterator's remove function
     */
    @Override
    public void remove()
    { this.underlying.remove(); } 
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