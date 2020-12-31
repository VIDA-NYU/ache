/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package achecrawler.crawler.crawlercommons.test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class RandomResponseHandler extends AbstractHandler {

    private static final long DEFAULT_DURATION = 1000L;

    private int _length;
    private long _duration;
    private Random _rand;

    /**
     * Create an HTTP response handler that sends random data back at a
     * particular rate.
     * 
     * @param length
     *            - number of bytes to return
     * @param duration
     *            - duration for response, in milliseconds.
     */
    public RandomResponseHandler(int length, long duration) {
        _length = length;
        _duration = duration;
        _rand = new Random();
    }

    /**
     * Send back <length> bytes of random data in one second.
     * 
     * @param length
     *            - number of bytes to return
     */
    public RandomResponseHandler(int length) {
        this(length, DEFAULT_DURATION);
    }

    @Override
    public void handle(String pathInContext, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentLength(_length);
        response.setContentType("text/html");
        response.setStatus(200);

        OutputStream os = response.getOutputStream();
        long startTime = System.currentTimeMillis();
        byte[] bytes = new byte[1];

        try {
            for (long i = 0; i < _length; i++) {
                _rand.nextBytes(bytes);
                os.write(bytes[0]);

                // Given i/_length as % of data written, we know that
                // this * duration is the target elapsed time. Figure out
                // how much to delay to make it so.
                long curTime = System.currentTimeMillis();
                long elapsedTime = curTime - startTime;
                long targetTime = (i * _duration) / _length;
                if (elapsedTime < targetTime) {
                    Thread.sleep(targetTime - elapsedTime);
                }
            }
        } catch (InterruptedException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Response handler interrupted");
        }
    }
}
