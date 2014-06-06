package com.lyft.reactivehttp;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class OkHttpTransport implements HttpTransport {
    private OkHttpClient okHttpClient;
    private OkUrlFactory okUrlFactory;

    public OkHttpTransport(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        this.okUrlFactory = new OkUrlFactory(this.okHttpClient);
    }

    @Override
    public HttpResponse execute(HttpRequest request) throws IOException {
        OutputStream out = null;
        InputStream in = null;

        HttpURLConnection connection = null;
        HttpResponse response = null;

        try {
            connection = okUrlFactory.open(new URL(request.getUrl()));

            connection.setRequestMethod(request.getMethod());

            for (NameValuePair header : request.getHeaders()) {
                connection.addRequestProperty(header.getName(), header.getValue());
            }

            TypedOutput requestBody = request.getBody();

            if (requestBody != null) {

                connection.addRequestProperty("Content-type", requestBody.mimeType());

                long contentLength = requestBody.length();
                connection.setFixedLengthStreamingMode((int) contentLength);

                connection.addRequestProperty("Content-Length", String.valueOf(contentLength));
                out = connection.getOutputStream();

                requestBody.writeTo(out);
                out.close();
            }


            int statusCode = connection.getResponseCode();

            if (statusCode >= 200 && statusCode < 300) {
                in = connection.getInputStream();
            } else {
                in = connection.getErrorStream();
            }

            String mimeType = connection.getContentType();
            long length = connection.getContentLength();

            TypedInputStream typedInputStream = new TypedInputStream(mimeType, length, in);

            response = new HttpResponse(statusCode, typedInputStream);

            for (Map.Entry<String, List<String>> field : connection.getHeaderFields().entrySet()) {
                String name = field.getKey();
                for (String value : field.getValue()) {
                    response.addHeader(name, value);
                }
            }

        } catch (IOException e) {

            try {
                if (in != null) {
                    in.close();
                }
            } catch (Throwable e2) {
                // suppress stream close errors
            }

            throw e;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Throwable e) {
                // suppress stream close errors
            }
        }

        return response;
    }
}
