package com.example.filemanager.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment implements OnFileSelectedListener {
    View view;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<File> fileList = new ArrayList<>();
    private LinearLayout linearImage, linearVideo, linearAudio, linearDocs, linearDownloads, linearAPK;
    String[] items = {"Details", "Rename", "Delete", "Share"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        linearImage = view.findViewById(R.id.linearImage);
        linearVideo = view.findViewById(R.id.linearVideo);
        linearAudio = view.findViewById(R.id.linearAudio);
        linearDocs = view.findViewById(R.id.linearDocs);
        linearDownloads = view.findViewById(R.id.linearDownloads);
        linearAPK = view.findViewById(R.id.linearAPK);

        linearImage.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("fileType", "image");
            CategorizedFragment categorizedFragment = new CategorizedFragment();
            categorizedFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, categorizedFragment).addToBackStack(null).commit();
        });
        linearVideo.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("fileType", "video");
            CategorizedFragment categorizedFragment = new CategorizedFragment();
            categorizedFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, categorizedFragment).addToBackStack(null).commit();
        });
        linearAudio.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("fileType", "audio");
            CategorizedFragment categorizedFragment = new CategorizedFragment();
            categorizedFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, categorizedFragment).addToBackStack(null).commit();
        });
        linearDocs.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("fileType", "doc");
            CategorizedFragment categorizedFragment = new CategorizedFragment();
            categorizedFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, categorizedFragment).addToBackStack(null).commit();
        });
        linearDownloads.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("fileType", "download");
            CategorizedFragment categorizedFragment = new CategorizedFragment();
            categorizedFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, categorizedFragment).addToBackStack(null).commit();
        });
        linearAPK.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("fileType", "apk");
            CategorizedFragment categorizedFragment = new CategorizedFragment();
            categorizedFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, categorizedFragment).addToBackStack(null).commit();
        });

        runtimePermission();

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
            if (singleFile.isDirectory()) arrayList.addAll(findFiles(singleFile));
            else arrayList.add(singleFile);
        }
        arrayList.sort(Comparator.comparing(File::lastModified).reversed());
        return arrayList.stream().limit(10).collect(Collectors.toCollection(ArrayList::new));
    }


    private void displayFiles() {
        recyclerView = view.findViewById(R.id.recycler_recents);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        fileList.addAll(findFiles(Environment.getExternalStorageDirectory()));
        fileAdapter = new FileAdapter(getContext(), fileList, this);
        recyclerView.setAdapter(fileAdapter);
    }

    @Override
    public void onFileClicked(File file) {
        if (file.isDirectory()) {
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getAbsolutePath());
            InternalFragment internalFragment = new InternalFragment();
            internalFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, internalFragment).addToBackStack(null).commit();
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

        options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
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
                                fileList.set(position, destination);
                                fileAdapter.notifyItemChanged(position);
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
                        // TODO: RENAME DOESN'T WORK
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
                        // TODO: DELETE DOESN'T WORK
                        break;
                    case "Share":
                        Intent share = new Intent();
                        share.setAction(Intent.ACTION_SEND);
                        share.setType("image/jpeg");
                        share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file));
                        startActivity(Intent.createChooser(share, "Share " + file.getName()));
                        break;

                }
            }
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
