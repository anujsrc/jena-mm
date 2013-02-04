package edu.utdallas.paged.sparql.engine.iterator;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/**
 * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIter
 * @author vaibhav
 */
public abstract class PagedQueryIter extends PagedQueryIteratorBase
{
    // Volatile just to make it safe to concurrent updates
    // It does not matter too much if it is wrong - it's used as a label.
    volatile static int iteratorCounter = 0 ;
    private int iteratorNumber = (iteratorCounter++) ;
    
    private ExecutionContext tracker ;
    
    public PagedQueryIter(ExecutionContext qCxt)
    { 
        tracker = qCxt ;
        register() ;
    }

    /**
     * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIter#makeTracked(QueryIterator, ExecutionContext)
     */
    public static PagedQueryIter makeTracked(QueryIterator qIter, ExecutionContext qCxt)
    {
        if ( qIter instanceof PagedQueryIter )
            return (PagedQueryIter)qIter ;
        return new PagedQueryIterTracked(qIter, qCxt) ; 
    }
    
    /**
     * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIter#close()
     */
    public final void close()
    {
        super.close() ;
        deregister() ;
    }
    
    /**
     * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIter#getExecContext()
     */
    public ExecutionContext getExecContext() { return tracker ; }

    /**
     * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIter#getIteratorNumber()
     */
    public int getIteratorNumber() { return iteratorNumber ; }
    
    /**
     * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIter#output(IndentedWriter, SerializationContext)
     */
    public void output(IndentedWriter out, SerializationContext sCxt)
    { out.println(getIteratorNumber()+"/"+debug()) ; }
    
    private void register()
    {
        if ( tracker != null )
            tracker.openIterator(this) ;
    }
    
    private void deregister()
    {
        if ( tracker != null )
            tracker.closedIterator(this) ;
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