package com.example.classlink;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    public static byte[] getFileDataFromUri(Context context, Uri uri) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ContentResolver resolver = context.getContentResolver();
            InputStream inputStream = resolver.openInputStream(uri);
            if (inputStream == null) return new byte[0];
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }
}
