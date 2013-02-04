package edu.utdallas.paged.sdb.core.sqlnode;

import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNodeBase1;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTransformCopy;

/**
 * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlTransformCopy
 * @author vaibhav
 */
public class PagedSqlTransformCopy extends SqlTransformCopy
{
    private boolean alwaysCopy = false ;
    
    public PagedSqlTransformCopy() { this(COPY_ONLY_ON_CHANGE) ; }
    public PagedSqlTransformCopy(boolean alwaysDuplicate)   { this.alwaysCopy = alwaysDuplicate ; }

    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlTransformCopy#transform(com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock, SqlNode)
     */
    public SqlNode transform(PagedSqlSelectBlock pagedSqlSelectBlock, SqlNode subNode)
    { return xform(pagedSqlSelectBlock, subNode) ; }

    private SqlNode xform(SqlNodeBase1 sqlNode, SqlNode subNode)
    { 
        if ( ! alwaysCopy && sqlNode.getSubNode() == subNode )
            return sqlNode ;
        return sqlNode.copy(subNode) ;
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