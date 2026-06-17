/*
 * Copyright (C) 2024 Pointblue Technology LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pointbluetech.ida.collector.idm.entitlement.offline;

import com.novell.nds.dirxml.driver.XmlDocument;
import com.pointbluetech.ida.collector.idm.entitlement.ResultParser;
import com.pointbluetech.ida.collector.idm.entitlement.ServiceParams;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ParserTest {
    public static void main(String[] args) throws Exception {
        ParserTest theOne = new ParserTest();
        theOne.run();
    }

    public void run() throws Exception {
        System.out.println("Running ParserTest");
        XmlDocument resultDoc = getXdsDoc("/com/pointbluetech/ida/collector/idm/entitlement/offline/ccResult.xml");
        JSONObject jsonRequest = getJSONDoc("/com/pointbluetech/ida/collector/idm/entitlement/offline/serviceParams.json");
        //TODO: Add jsonRequest parameters
        ResultParser parser = new ResultParser();
        try {
           JSONArray result = parser.parse(resultDoc.getDocumentString(), new ServiceParams(jsonRequest));
            System.out.println(result.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static XmlDocument getXdsDoc(String xdsDocPath){
        InputStream istream = TestJig.class.getResourceAsStream(xdsDocPath);

        String initString = getStringFromInputStream(istream);
        //System.out.println(initString);
        return new XmlDocument(initString);
    }

    public static JSONObject getJSONDoc(String path) throws Exception{
        InputStream istream = TestJig.class.getResourceAsStream(path);

        String initString = getStringFromInputStream(istream);
        //System.out.println(initString);
        return new JSONObject(initString);
    }


    public static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try
        {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

}
