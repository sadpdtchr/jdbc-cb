/*
 * //  Copyright (c) 2015 Couchbase, Inc.
 * //  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * //  except in compliance with the License. You may obtain a copy of the License at
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //  Unless required by applicable law or agreed to in writing, software distributed under the
 * //  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * //  either express or implied. See the License for the specific language governing permissions
 * //  and limitations under the License.
 */

package com.couchbase;

import com.couchbase.jdbc.TestUtil;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by davec on 2015-02-26.
 */
@RunWith(JUnit4.class)
public class StatementTest extends TestCase
{
    Connection con;

    @Before
    public void openConnection() throws Exception
    {
        con = DriverManager.getConnection(TestUtil.getURL(), TestUtil.getUser(), TestUtil.getPassword());
        assertNotNull(con);
    }
    @After
    public void closeConnection() throws Exception
    {
        assertNotNull(con);
        con.createStatement().executeUpdate("delete from test1");
        con.close();
    }
    @Test
    public void createStatement() throws Exception
    {
        Statement statement = con.createStatement();
        assertNotNull(statement);

    }
    @Test
    public void emptyResult() throws Exception
    {
        Statement statement = con.createStatement();
        assertNotNull(statement);

        ResultSet rs = statement.executeQuery("select * from test1");
        assertFalse(rs.next());

    }
    @Test
    public void simpleSelect() throws Exception
    {
        Statement statement = con.createStatement();
        assertNotNull(statement);

        ResultSet rs = statement.executeQuery("select 1");

        assertTrue(rs.next());
        assertEquals(1,rs.getInt(1));

    }

    @Test
    public void simpleInsert() throws Exception
    {
        assertNotNull(con);
        Statement statement = con.createStatement();
        assertNotNull(statement);

        for (int i = 0; i++< 100;)
        {

            int inserted = statement.executeUpdate("INSERT INTO test1  (KEY, VALUE) VALUES ( 'K" + i +"'," + i +")");
            assertEquals(1, inserted);
        }

        ResultSet resultSet = statement.executeQuery("select count(1) as test_count from test1");
        assertTrue(resultSet.next());
        assertEquals(100,resultSet.getInt("test_count"));

        resultSet = statement.executeQuery("select * from test1 order by test1");
        for (int i=0; resultSet.next(); i++)
        {
            assertEquals(i+1, resultSet.getInt(1));
        }

        resultSet = statement.executeQuery("select raw test1 from test1 order by test1");
        for (int i=0; resultSet.next(); i++)
        {
            assertTrue(resultSet.getInt(1)>0);
        }

        boolean hasResultSet = statement.execute("update test1 set test1=0 returning test1");
        if ( hasResultSet )
        {
            resultSet = statement.getResultSet();
            for (int i=0; resultSet.next(); i++)
            {
                assertEquals(0, resultSet.getInt(1));
            }

        }

        statement.executeUpdate("delete from test1");

        resultSet = statement.executeQuery("select count(1) as count from test1");
        assertTrue(resultSet.next());
        assertEquals(0, resultSet.getInt(1));

    }
    @Test
    public void getAllTypes() throws Exception
    {
        JsonArray array = Json.createArrayBuilder().add(1).add(2).add(3).add(5).add(8).build();

        JsonObject jsonObject = Json.createObjectBuilder().add("a1","Object").build();

        Statement statement = con.createStatement();
        assertNotNull(statement);

        ResultSet resultSet = statement.executeQuery("SELECT true as c1, 1 as c2, 3.14 as c3,  'Hello World!' as c4, [1,2,3,5,8] as c5, { 'a1': 'Object' } as c6");

        assertTrue(resultSet.next());

        assertTrue(resultSet.getBoolean(1));
        assertTrue(resultSet.getBoolean("c1"));

        assertEquals(1,resultSet.getInt(2));
        assertEquals(1,resultSet.getInt("c2"));


        assertEquals(3.14F, resultSet.getFloat(3), 0.0f);
        assertEquals(3.14F, resultSet.getFloat("c3"), 0.0f);

        assertEquals("Hello World!",resultSet.getString(4));
        assertEquals("Hello World!",resultSet.getString("c4"));

        assertEquals(array,resultSet.getArray(5).getArray());
        assertEquals(array,resultSet.getArray("c5").getArray());

        assertEquals(jsonObject, resultSet.getObject(6));
        assertEquals(jsonObject, resultSet.getObject("c6"));


    }
    /*
    @Test
    public void batchInsert() throws Exception
    {
        assertNotNull(con);
        Statement statement = con.createStatement();
        assertNotNull(statement);

        for (int i = 0; i++ < 2; )
        {

            statement.addBatch("INSERT INTO test1  (KEY, VALUE) VALUES ( 'K" + i + "'," + i + ")");
        }
        statement.executeBatch();
    }
    */
}
