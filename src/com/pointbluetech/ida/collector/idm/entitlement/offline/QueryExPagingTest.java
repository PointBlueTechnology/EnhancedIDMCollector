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

import com.pointbluetech.ida.collector.idm.entitlement.Collector;
import com.pointbluetech.ida.collector.idm.entitlement.ResultParser;
import com.pointbluetech.ida.collector.idm.entitlement.ServiceParams;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * Offline regression test for the collector's query-ex chunking. No engine or
 * LDAP connection required: it exercises the XDS the collector emits
 * ({@link Collector#getQuery}) and the response parsing
 * ({@link ResultParser#getQueryToken}).
 *
 * <p>Run as a plain main (mirrors the other jigs in this package). Prints
 * PASS/FAIL per check and exits non-zero on any failure.
 */
public class QueryExPagingTest {

    private static int fails = 0;

    private static void check(String name, boolean cond) {
        System.out.println((cond ? "PASS " : "FAIL ") + name);
        if (!cond) {
            fails++;
        }
    }

    public static void main(String[] args) throws Exception {
        // --- query-ex document generation ---------------------------------
        String first = new String(Collector.getQuery("user", null, true, 500, null));
        check("first page is <query-ex> with max-result-count",
                first.contains("<query-ex ") && first.contains("max-result-count=\"500\""));
        check("first page carries no <query-token>", !first.contains("<query-token>"));
        check("account collection marker set",
                first.contains("ig-account-collection-query=\"true\""));

        String cont = new String(Collector.getQuery("user", null, true, 500, "TKN-9"));
        check("continuation page injects <query-token>",
                cont.contains("<query-token>TKN-9</query-token>"));

        String legacy = new String(Collector.getQuery("group", null, false, 0, null));
        check("page-size 0 emits legacy plain <query>",
                legacy.contains("<query ") && !legacy.contains("query-ex"));

        String custom = "<nds><input><query-ex class-name=\"user\" max-result-count=\"100\">"
                + "<search-class class-name=\"user\"/></query-ex></input></nds>";
        String customCont = new String(Collector.getQuery("user", custom, false, 100, "CT-1"));
        check("custom query continuation splices token before </query-ex>",
                customCont.contains("<query-token>CT-1</query-token></query-ex>"));

        // --- response parsing: token extraction ----------------------------
        ServiceParams sp = serviceParams();

        String paged = "<nds><output>"
                + "<instance class-name=\"user\" src-dn=\"u1\"><association>A1</association></instance>"
                + "<instance class-name=\"user\" src-dn=\"u2\"><association>A2</association></instance>"
                + "<query-token>PAGE-TOKEN-42</query-token></output></nds>";
        ResultParser p1 = new ResultParser();
        JSONArray r1 = p1.parse(paged, sp);
        check("parser returns the page's instances", r1.length() == 2);
        check("parser captures the <query-token>", "PAGE-TOKEN-42".equals(p1.getQueryToken()));

        String complete = "<nds><output>"
                + "<instance class-name=\"user\" src-dn=\"u1\"><association>A1</association></instance>"
                + "</output></nds>";
        ResultParser p2 = new ResultParser();
        p2.parse(complete, sp);
        check("no <query-token> yields null (collection complete)", p2.getQueryToken() == null);

        System.out.println(fails == 0 ? "\nALL PASS" : "\n" + fails + " FAILED");
        if (fails != 0) {
            System.exit(1);
        }
    }

    /** Minimal valid ServiceParams (DaaS service-parms name/value/data-type array). */
    private static ServiceParams serviceParams() throws Exception {
        JSONArray parms = new JSONArray();
        String[][] kv = {
                {"dxml-search-class", "user"},
                {"server", "localhost"},
                {"port", "636"},
                {"entitlement_dn", "cn=ent"},
                {"driver-name", "cn=driver"}
        };
        for (String[] pair : kv) {
            JSONObject pv = new JSONObject();
            pv.put("name", pair[0]);
            pv.put("value", pair[1]);
            pv.put("data-type", "string");
            parms.put(pv);
        }
        JSONObject cfg = new JSONObject();
        cfg.put("service-parms", parms);
        return new ServiceParams(cfg);
    }
}
