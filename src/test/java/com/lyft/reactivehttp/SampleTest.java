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

import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * @author Alexey Zakharov
 */
public class SampleTest {

    public static final String IMGUR_CLIENT_ID = "146bbf89f891032";

    public static class Contributor {
        String login;
        int contributions;
    }

    static class Contributors extends ArrayList<Contributor> {
    }

//    interface GitHub {
//        @GET("/repos/{owner}/{repo}/contributors")
//        List<Contributor> contributors(
//                @Path("owner") String owner,
//                @Path("repo") String repo
//        );
//    }

    private Gson gson = new Gson();

    protected ReactiveHttpClient createClient() {
        return  new ReactiveHttpClient(
                new OkHttpTransport(new OkHttpClient()),
                gson,
                Schedulers.io(),
                new ConsoleLog(),
                false);
    }

    @Test
    public void getRepoContributors() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        createClient().create()
                .get("https://api.github.com/repos/%s/%s/contributors", "lyft", "reactive-http")
                .observe(Contributors.class)
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        latch.countDown();
                    }
                })
                .subscribe(new Action1<Contributors>() {
                    @Override
                    public void call(Contributors contributors) {
                        System.out.println("Contributors count" + contributors.size());
                    }
                });

        latch.await();
    }

    @Test
    public void getRepoContributorsAsString() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        createClient().create()
                .get("https://api.github.com/repos/%s/%s/contributors", "lyft", "reactive-http")
                .observeAsString()
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        latch.countDown();
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String result) {
                        System.out.println("Contributors:" + result);
                    }
                });

        latch.await();
    }

    @Test
    public void postRepoContributors() {
        Contributor data = new Contributor();
        data.login = "sdfsdaf";
        int contributions = 232;

        createClient().create()
                .post("https://api.github.com/repos/%s/%s/contributors", "lyft", "reactive-http")
                .data(data)
                .observe(Contributors.class)
                .subscribe(new Action1<Contributors>() {
                               @Override
                               public void call(Contributors contributors) {
                                   for (Contributor contributor : contributors) {

                                   }
                               }
                           }, new Action1<Throwable>() {
                               @Override
                               public void call(Throwable throwable) {

                               }
                           }
                );
    }

    @Test
    public void getNotExistingRepoContributors() {
        createClient().create()
                .get("https://api.github.com/repos/%s/%s/contributors", "lyft", "asdfdsaf")
                .observe(Contributors.class)
                .subscribe(new Action1<Contributors>() {
                               @Override
                               public void call(Contributors contributors) {

                               }
                           }, new Action1<Throwable>() {
                               @Override
                               public void call(Throwable throwable) {

                               }
                           }
                );
    }


    public static class GithubApiError {
        String message;
    }

    public static class GithubException extends IOException {

        private int status;
        private GithubApiError error;

        public GithubException(int status, GithubApiError error) {

            this.status = status;
            this.error = error;
        }

        public int getStatus() {
            return status;
        }

        public GithubApiError getError() {
            return error;
        }
    }

    public static class GithubErrorHandler implements ErrorHandler {

        @Override
        public Throwable handleError(HttpResponseException cause) {
            GithubException ge = new GithubException(cause.getStatus(), cause.getBodyAs(GithubApiError.class));

            return ge;
        }
    }


    @Test
    public void failAuthroizeOnGithub() {
        ReactiveHttpClient client = createClient();

        client.setErrorHandler(new GithubErrorHandler());
        client.create()
                .get("https://api.github.com")
                .set("Authorization", "foo")
                .observe(Void.class)
                .subscribe(new Action1<Void>() {
                               @Override
                               public void call(Void v) {

                               }
                           }, new Action1<Throwable>() {
                               @Override
                               public void call(Throwable e) {
                                    if (e instanceof GithubException) {

                                        GithubException ge = (GithubException) e;
                                    }
                               }
                           }
                );
    }

    static class ImgurResponse {
        Object data;
        Boolean success;
        Integer status;
    }

    @Test
    public void uploadImageToImgur() throws InterruptedException {
        File file = new File("src/test/resources/image_sample.jpg");

        final CountDownLatch latch = new CountDownLatch(1);

        createClient().create()
                .post("https://api.imgur.com/3/image")
                .file("image/jpeg", file)
                .set("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                .observe(ImgurResponse.class)
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        latch.countDown();
                    }
                })
                .subscribe(new Action1<ImgurResponse>() {
                               @Override
                               public void call(ImgurResponse response) {

                               }
                           }, new Action1<Throwable>() {
                               @Override
                               public void call(Throwable throwable) {

                               }
                           }
                );

        latch.await();
    }

}
