package edu.utdallas.paged.mem.algorithm;

import edu.utdallas.paged.mem.algorithm.CacheAlgorithmBase;
import edu.utdallas.paged.mem.cache.CacheBase;

/**
 * A class that keeps track of the index of a node in the bunch map,
 * the last time this node was accessed and the number of connections for
 * this node. The class also implements the comparable interface to sort
 * nodes based on the timestamp and the number of connections
 * 
 * @author vaibhav
 * 
 */
public class NodeStampLRU extends CacheAlgorithmBase
{
	/**
	 * Method that implements the comparable interface for LRU.
	 * @param o1 - nodestamp object against which the current object is compared
	 * @return an integer that determines if the current node must come before the parameter node
	 */
	public int compareTo(Object o1)
	{
		NodeStampLRU temp = (NodeStampLRU)o1;
		int timeComp = this.currTime.compareTo(temp.currTime);

		//in LRU we need to write nodes to disk that were created the oldest in time
		//but with the fewest connections
		if(timeComp < 0)
			return 0;
		else if(timeComp > 0)
			return 1;
		else
		{
			if( ( this.connections / (CacheBase.size*1.0) ) > ( temp.connections / (CacheBase.size*1.0) ) )
			//if( this.individualCC >= temp.individualCC )
				return 1;
			else
				return 0;
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