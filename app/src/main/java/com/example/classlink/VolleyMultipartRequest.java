package com.example.classlink;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Multipart request for uploading files (image/pdf) with Volley.
 */
public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private Map<String, String> mHeaders;
    private Map<String, String> params;
    private Map<String, DataPart> byteData;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setByteData(Map<String, DataPart> byteData) {
        this.byteData = byteData;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            // Text params
            if (params != null && params.size() > 0) {
                buildTextPart(dos, params);
            }

            // File data
            if (byteData != null && byteData.size() > 0) {
                for (Map.Entry<String, DataPart> entry : byteData.entrySet()) {
                    buildDataPart(dos, entry.getValue(), entry.getKey());
                }
            }

            // End boundary
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(com.android.volley.VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    private void buildTextPart(DataOutputStream dos, Map<String, String> params) throws IOException {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(entry.getValue() + lineEnd);
        }
    }

    private void buildDataPart(DataOutputStream dos, DataPart dataFile, String inputName) throws IOException {
        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"" +
                inputName + "\"; filename=\"" + dataFile.getFileName() + "\"" + lineEnd);
        if (dataFile.getType() != null && !dataFile.getType().isEmpty()) {
            dos.writeBytes("Content-Type: " + dataFile.getType() + lineEnd);
        }
        dos.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(dataFile.getContent());
        int bytesAvailable = fileInputStream.available();

        int bufferSize = Math.min(bytesAvailable, 1024 * 1024);
        byte[] buffer = new byte[bufferSize];

        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dos.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, 1024 * 1024);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dos.writeBytes(lineEnd);
    }

    // Data holder for file
    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}
