package com.example.filemanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.tools.FileType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {
    private Context context;
    private List<File> files;
    private OnFileSelectedListener listener;

    public FileAdapter(Context context, List<File> files, OnFileSelectedListener listener) {
        this.context = context;
        this.files = files;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(context).inflate(R.layout.file_container, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File selectedFile = files.get(position);
        String selectedFileName = selectedFile.getName();
        holder.tvName.setText(selectedFileName);
        holder.tvName.setSelected(true);

        int items = 0;
        if (selectedFile.isDirectory()) {
            File[] dir_files = selectedFile.listFiles();
            if (dir_files == null) {
                return;
            }
            for (File file : dir_files) {
                if (!file.isHidden()) {
                    items += 1;
                }
            }
            holder.tvSize.setText(items + " files");
            holder.imgFile.setImageResource(R.drawable.ic_icon_dir);
        } else {
            holder.tvSize.setText(Formatter.formatShortFileSize(context, selectedFile.length()));
        }


        if (FileType.isFileIMG(selectedFileName)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            InputStream imageStream = null;
            Bitmap bitmap = null;
            try {
                imageStream = context.getContentResolver().openInputStream(Uri.fromFile(files.get(position)));
                bitmap = BitmapFactory.decodeStream(imageStream, null, options);
                imageStream.close();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
            if (bitmap == null) {
                holder.imgFile.setImageResource(R.drawable.ic_icon_img);
            } else {
                holder.imgFile.setImageBitmap(bitmap);
            }

        } else if (FileType.isFileDOC(selectedFileName)) {
            holder.imgFile.setImageResource(R.drawable.ic_icon_doc);
        } else if (FileType.isFileAUDIO(selectedFileName)) {
            holder.imgFile.setImageResource(R.drawable.ic_icon_audio);
        } else if (FileType.isFileVIDEO(selectedFileName)) {
            holder.imgFile.setImageResource(R.drawable.ic_icon_video);
        } else if (FileType.isFileEXEC(selectedFileName)) {
            holder.imgFile.setImageResource(R.drawable.ic_icon_exec);
        }

        holder.container.setOnClickListener(v -> listener.onFileClicked(files.get(position)));
        holder.container.setOnLongClickListener(v -> {
            listener.onFileLongClicked(files.get(position), position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }
}
