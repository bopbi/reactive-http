/*
 *
 *  * Copyright (C) 2012 Lyft, Inc.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.lyft.reactivehttp;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import org.junit.Test;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;

/**
 * @author Alexey Zakharov
 */
public class HttpRequestBuilderTest {

    private Gson gson = new Gson();

    ReactiveHttpClient client = new ReactiveHttpClient(
            new OkHttpTransport(new OkHttpClient()),
            gson,
            Schedulers.io(),
            new ConsoleLog(),
            true);

    @Test
    public void createGet() {
        HttpRequestBuilder request = new HttpRequestBuilder(client, gson).get("http://google.com/%s/", "request");
        assertEquals("http://google.com/request/", request.getUrlWithQueryString());
    }

    @Test
    public void createQueryString() {

        HttpRequestBuilder request = new HttpRequestBuilder(client, gson).get("http://google.com").query("q1", "abc").query("q2", "cba");
        assertEquals("http://google.com?q1=abc&q2=cba", request.getUrlWithQueryString());
    }
}
