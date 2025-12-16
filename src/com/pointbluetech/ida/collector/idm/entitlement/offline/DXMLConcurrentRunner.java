/*
 * Copyright (C) 2025 Pointblue Technology LLC.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Wrapper that runs three instances of DXMLTest concurrently in separate threads within the same JVM.
 *
 * Usage mirrors DXMLTest:
 * - No args: each thread will read configuration from dxmlConfig.properties in the working directory
 * - One arg: treated as the properties file path for each thread
 * - >= 6 args: positional args forwarded to DXMLTest
 */
public class DXMLConcurrentRunner {

    public static void main(String[] args) {
        int instances = 3; // fixed per requirement
        ExecutorService pool = Executors.newFixedThreadPool(instances);
        List<Future<Integer>> futures = new ArrayList<>(instances);

        for (int i = 1; i <= instances; i++) {
            final int idx = i;
            Callable<Integer> task = () -> {
                System.out.println("[T" + idx + "] starting");
                int code = DXMLTest.run(args);
                System.out.println("[T" + idx + "] finished with code " + code);
                return code;
            };
            futures.add(pool.submit(task));
        }

        int exit = 0;
        try {
            for (int i = 0; i < futures.size(); i++) {
                int code = futures.get(i).get();
                if (code != 0) exit = code;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for threads to finish.");
            exit = 3;
        } catch (ExecutionException e) {
            System.err.println("Thread execution failed: " + e.getCause());
            e.getCause().printStackTrace(System.err);
            exit = 2;
        } finally {
            pool.shutdownNow();
        }

        if (exit != 0) {
            System.exit(exit);
        }
    }
}
