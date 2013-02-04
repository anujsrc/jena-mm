package edu.utdallas.paged.db.test;

import junit.framework.* ;

import com.hp.hpl.jena.db.IDBConnection;

import edu.utdallas.paged.assembler.test.TestPagedRDBAssemblerContents;
import edu.utdallas.paged.db.PagedModelRDB;
import edu.utdallas.paged.db.RDBConstants;
import edu.utdallas.paged.rdf.model.ExtendedModelFactory;

public class TestPagedPackage extends TestSuite
{
	
	/*
	 * this testPackage requires the parameters for a
	 * database connection to be defined. use the "guess" 
     * method in ModelFactoryBase to get the parameters from
	 * a configuration file (see test-db.sh, test.bat,
	 * test.sh). if the guess methods do not work for
	 * you, /contact Chris so we can work out why/ and in
     * the meantime /temporarily/ hack the code using the
     * examples below.
     * 
     * Using the guess* methods allows the same codebase to be
     * tested against different databases without having to
     * fiddle around by hand.
	 */
 
	/*/ oracle settings
	static String M_DB_URL = "jdbc:oracle:oci8:@";
	static String M_DB_USER = "scott";
	static String M_DB_PASSWD = "tiger";
	static String M_DB = "Oracle";
	static String M_DBDRIVER_CLASS = "oracle.jdbc.OracleDriver";
	
	
	/* mysql settings */
	static String M_DB_URL = RDBConstants.M_DB_URL;
	static String M_DB_USER = RDBConstants.M_DB_USER;
	static String M_DB_PASSWD = RDBConstants.M_DB_PASSWD;
	static String M_DB = RDBConstants.M_DB;
	static String M_DBDRIVER_CLASS = RDBConstants.M_DBDRIVER_CLASS;
	// */
		
	/*/ postgresql settings
	static String M_DB_URL = "jdbc:postgresql://localhost/test";
	static String M_DB_USER = "test";
	static String M_DB_PASSWD = "";
	static String M_DB = "PostgreSQL";
	static String M_DBDRIVER_CLASS = "org.postgresql.Driver";
	// */
        
	
    /*
        Command-line controlled settings (using -Dfoo=bar options, see
        ModelFactoryBase.guess* methods). Note that the -D options can
        be set from inside Eclipse, and presumably other IDEs as well.      
    */
	/*/
	public static String M_DB_URL = ModelFactoryBase.guessDBURL();
    public static String M_DB_USER = ModelFactoryBase.guessDBUser();
    public static String M_DB_PASSWD = ModelFactoryBase.guessDBPassword();
    public static String M_DB = ModelFactoryBase.guessDBType();
    public static String M_DBDRIVER_CLASS = ModelFactoryBase.guessDBDriver();
    public static boolean  M_DBCONCURRENT = ModelFactoryBase.guessDBConcurrent();
    // */      

    static public TestSuite suite() {
        return new TestPagedPackage();
    }
    
    /** Creates new TestPagedPackage */
    private TestPagedPackage() 
    {
        super("PagedGraphRDB");
		addTest( "TestPagedConnection", TestPagedConnection.suite() );
        addTest( "TestPagedBasicOperations", TestPagedBasicOperations.suite() );
        addTest( "TestPagedSimpleSelector", TestPagedSimpleSelector.suite() );
		addTest( "TestPagedCompatability", TestPagedCompatability.suite() );
		addTest( "TestCompareToPagedMem", TestCompareToPagedMem.suite() );
		addTest( "TestPagedGraphRDB", TestPagedGraphRDB.suite());
		addTest( "TestPagedModelRDB", TestPagedModelRDB.suite());
		addTest( "TestPagedGraphRDBMaker", TestPagedGraphRDBMaker.suite());
		addTest( "TestPagedMultiModel", TestPagedMultiModel.suite());
		addTest( "TestPagedNsPrefix", TestPagedNsPrefix.suite());
		addTest( "TestPagedPrefixMapping", TestPagedPrefixMapping.suite());
		addTest( "TestPagedTransactions", TestPagedTransactions.suite() );
		addTest( "TestPagedReifier", TestPagedReifier.suite() );
		addTest( "TestReifierCompareToPagedMem", TestReifierCompareToPagedMem.suite());
		addTest( "TestPagedQueryRDB", TestPagedQueryRDB.suite());
		addTest( "TestPagedQuery1", TestPagedQuery1.suite());
        addTest( "TestModelFactory", TestExtendedModelFactory.suite() );
        addTestSuite( TestPagedRDBAssemblerContents.class );
        }

    public static class TestExtendedModelFactory extends TestCase
    {
        public TestExtendedModelFactory( String name ) { super( name ); }
        public static TestSuite suite() { return new TestSuite( TestExtendedModelFactory.class ); }
        
        public void testExtendedModelFactory()
        {
            IDBConnection c = TestPagedConnection.makeAndCleanTestConnection();
            assertTrue( ExtendedModelFactory.createPagedModelRDBMaker( c ).createFreshModel() instanceof PagedModelRDB );
        }
    }
    
    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
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