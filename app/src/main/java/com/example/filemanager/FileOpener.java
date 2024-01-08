package com.example.filemanager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.filemanager.tools.FileType;

import java.io.File;

public class FileOpener {
    public static void openFile(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        String fileName = uri.toString().toLowerCase();

        Intent intent = new Intent(Intent.ACTION_VIEW);

        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(FileType.fileExt(fileName));
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent.setDataAndType(uri, "*/*");
            context.startActivity(intent);
        }
    }
}
