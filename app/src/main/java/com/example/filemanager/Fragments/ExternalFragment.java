package com.example.filemanager.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.FileAdapter;
import com.example.filemanager.FileOpener;
import com.example.filemanager.OnFileSelectedListener;
import com.example.filemanager.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ExternalFragment extends Fragment implements OnFileSelectedListener {
    View view;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<File> fileList = new ArrayList<>();
    private ImageView img_back;
    private TextView tv_pathHolder;
    File storage;
    String data;
    String[] items = {"Details", "Rename", "Delete", "Share"};
    String secStorage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_external, container, false);
        tv_pathHolder = view.findViewById(R.id.tv_pathHolder);
        img_back = view.findViewById(R.id.img_back);
        recyclerView = view.findViewById(R.id.recycler_internal);

        File[] externalCacheDirs = requireContext().getExternalCacheDirs();
        for (File file : externalCacheDirs) {
            if (Environment.isExternalStorageRemovable(file)) {
                secStorage = file.getPath().split("/Android")[0];
                break;
            }
        }
        if (secStorage == null) {
            Toast.makeText(getContext(), "Memory card not found.", Toast.LENGTH_LONG).show();
            tv_pathHolder.setText("Memory card not found");
            return view;
        }
        storage = new File(secStorage);

        Bundle bundle = getArguments();
        if (bundle != null) {
            data = getArguments().getString("path");
            File file = null;
            if (data != null) {
                file = new File(data);
            }
            storage = file;
        }

        tv_pathHolder.setText(storage.getAbsolutePath());
        runtimePermission();

        img_back.setOnClickListener(v -> {
            if (Objects.equals(secStorage, tv_pathHolder.getText().toString())) {
                return;
            }
            List<String> test1 = new ArrayList<>(Arrays.asList(tv_pathHolder.getText().toString().split("/")));
            test1.remove(test1.size()-1);
            Bundle newBundle = new Bundle();
            newBundle.putString("path", String.join("/", test1));
            ExternalFragment externalFragment = new ExternalFragment();
            externalFragment.setArguments(newBundle);
            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, externalFragment)
                    .addToBackStack(null).commit();

            getParentFragmentManager()
                    .beginTransaction()
                    .detach(ExternalFragment.this)
                    .attach(ExternalFragment.this)
                    .commit();
        });

        return view;
    }

    private void runtimePermission() {
        Dexter.withContext(getContext()).withPermissions(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                displayFiles();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }
        }).check();

    }
    public ArrayList<File> findFiles(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }

        for (File singleFile : files) {
            if (singleFile.isHidden()) continue;
            arrayList.add(singleFile);
        }
        return arrayList;
    }

    private void displayFiles() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        fileList.addAll(findFiles(storage));
        fileAdapter = new FileAdapter(getContext(), fileList, this);
        recyclerView.setAdapter(fileAdapter);
    }

    @Override
    public void onFileClicked(File file) {
        if (file.isDirectory()) {
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getAbsolutePath());
            ExternalFragment externalFragment = new ExternalFragment();
            externalFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, externalFragment).addToBackStack(null).commit();
        } else {
            FileOpener.openFile(getContext(), file);
        }
    }

    @Override
    public void onFileLongClicked(File file, int position) {
        final Dialog optionDialog = new Dialog(getContext());
        optionDialog.setContentView(R.layout.option_dialog);
        optionDialog.setTitle("Select option");
        ListView options = (ListView) optionDialog.findViewById(R.id.List);
        CustomAdapter customAdapter = new CustomAdapter();
        options.setAdapter(customAdapter);
        optionDialog.show();

        options.setOnItemClickListener((parent, view, position1, id) -> {
            String selectedItem = parent.getItemAtPosition(position1).toString();
            switch (selectedItem) {
                case "Details":
                    AlertDialog.Builder detailDialog = new AlertDialog.Builder(getContext());
                    detailDialog.setTitle("Details");
                    final TextView details = new TextView(getContext());
                    detailDialog.setView(details);
                    Date lastModified = new Date(file.lastModified());
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String formattedDate = formatter.format(lastModified);

                    details.setText(String.format("File Name: %s\nSize: %s\nPath: %s\nLast modified: %s",
                            file.getName(), Formatter.formatShortFileSize(getContext(), file.length()),
                            file.getAbsoluteFile(), formattedDate));

                    detailDialog.setPositiveButton("OK", (dialog, which) -> optionDialog.cancel());

                    AlertDialog alertDialog_details = detailDialog.create();
                    alertDialog_details.show();
                    break;
                case "Rename":
                    AlertDialog.Builder renameDialog = new AlertDialog.Builder(getContext());
                    renameDialog.setTitle("Rename file");
                    final EditText name = new EditText(getContext());
                    name.setPadding(50,30,0,0);
                    renameDialog.setView(name);
                    renameDialog.setPositiveButton("OK", (dialog, which) -> {
                        String new_name = name.getEditableText().toString();
                        String ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                        File current = new File(file.getAbsolutePath());
                        File destination = new File(file.getAbsolutePath().replace(file.getName(), new_name) + ext);
                        if (current.renameTo(destination)) {
                            fileList.set(fileList.indexOf(file), destination);
                            fileAdapter.notifyItemChanged(fileList.indexOf(file));
                            Toast.makeText(getContext(), "File renamed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "File cannot be renamed", Toast.LENGTH_SHORT).show();
                        }
                    });
                    renameDialog.setNegativeButton("Cancel", ((dialog, which) -> {
                        optionDialog.cancel();
                    }));
                    AlertDialog alertDialog_rename = renameDialog.create();
                    alertDialog_rename.show();
                    break;
                case "Delete":
                    AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                    deleteDialog.setTitle("Delete " + file.getName() + "?");
                    deleteDialog.setPositiveButton("Yes", (dialog, which) -> {
                        if (file.delete()) {
                            fileList.remove(file);
                            fileAdapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "File deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "File cannot be deleted", Toast.LENGTH_SHORT).show();
                        }
                    });
                    deleteDialog.setNegativeButton("No", ((dialog, which) -> {
                        optionDialog.cancel();
                    }));
                    AlertDialog alertDialog_delete = deleteDialog.create();
                    alertDialog_delete.show();
                    break;
                case "Share":
                    Intent share = new Intent();
                    share.setAction(Intent.ACTION_SEND);
                    share.setType("image/jpeg");
                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    startActivity(Intent.createChooser(share, "Share " + file.getName()));
                    break;
            }
            optionDialog.cancel();
        });

    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.option_layout, null);
            TextView txtOptions = view.findViewById(R.id.txtOption);
            ImageView imgOptions = view.findViewById(R.id.imgOption);
            txtOptions.setText(items[position]);
            switch (items[position]) {
                case "Details":
                    imgOptions.setImageResource(R.drawable.ic_details);
                    break;
                case "Rename":
                    imgOptions.setImageResource(R.drawable.ic_rename);
                    break;
                case "Delete":
                    imgOptions.setImageResource(R.drawable.ic_delete);
                    break;
                case "Share":
                    imgOptions.setImageResource(R.drawable.ic_share);
                    break;
            }
            return view;
        }
    }
}
