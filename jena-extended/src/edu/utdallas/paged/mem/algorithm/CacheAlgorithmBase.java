package edu.utdallas.paged.mem.algorithm;

import java.util.Calendar;

/**
 * An abstract class for the cache algorithms. This class only has one abstract method, 
 * compareTo(Object o1), since this method decides the way the algorithm is implemented
 * 
 * @author vaibhav
 * 
 */
public abstract class CacheAlgorithmBase implements Comparable<Object>
{
	/** The last time this node was accessed */
	public Calendar currTime;

	/** The number of connections for this node */
	public int connections = 0;
	
	/** The individual clustering coefficient for this node */
	public double individualCC = 0.0;
	
	/** The transitive clustering coefficient for this node */
	public double transitiveCC = 0.0;
	
	public CacheAlgorithmBase() { }
	
	/** Method that implements the comparable interface
	 *  @param o1 - object against which the current object is compared
	 *  @return an integer based on the algorithm implemented, e.g., for LRU it returns an
	 *          integer based on whether the current node comes before the parameter node
	 */
	public abstract int compareTo(Object o1) ;
	
	/** Method to get the number of current connections for this node 
	 *  @return the number of connections
	 *  */
	public int getConnections()
	{ return connections; }

	/** Method to increment the number of connections of this node */
	public void setConnections()
	{ connections++; }
	
	/** Method to get the last time this node was accessed 
	 *  @return the last time this node was accessed
	 * */
	public Calendar getCurrentTime()
	{ return currTime; }
	
	/** Method that sets the access time of this node to the current time 
	 *  @param currentTime - the current time when this node is accessed
	 * */
	public void setCurrentTime(Calendar currentTime)
	{ currTime = currentTime; }
	
	/** Method to get the individual clustering coefficient for this node */
	public double getIndividualCC()
	{ return individualCC; }

	/** Method to set the individual clustering coefficient for this node */
	public void setIndividualCC(double newCoefficient)
	{ individualCC = newCoefficient; }

	/** Method to get the transitive clustering coefficient for this node */
	public double getTransitiveCC()
	{ return transitiveCC; }

	/** Method to set the individual clustering coefficient for this node */
	public void setTransitiveCC(double newCoefficient)
	{ transitiveCC = newCoefficient; }
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