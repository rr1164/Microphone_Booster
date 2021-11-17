package com.Mic.microphonebooster;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
public class StatusFragment extends Fragment {
    ListView allMusicList;
    ArrayAdapter<String> musicArrayAdapter;
    List<String> main_list = new ArrayList<>();
    protected View mView;
    @Override
    public void onStart() {
        super.onStart();
    }
    private boolean checkpermission()
    {
        return ContextCompat.checkSelfPermission(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        this.mView = view;
        allMusicList = mView.findViewById(R.id.list_view);
        if(checkpermission()) {
            String path;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                path =requireActivity().getExternalFilesDir(null) + "/microphone_booster/";
            } else {
                path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/microphone_booster/";
            }
            File dir = new File(path);
            Log.v("red",dir.toString());
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                public void run() {
                    findMusicFiles(dir);
                    musicArrayAdapter.notifyDataSetChanged();
                    handler.postDelayed(this, 2000);
                }
            }, 3000);
            musicArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, main_list);
            allMusicList.setAdapter(musicArrayAdapter);
            allMusicList.setOnItemClickListener(
                    (parent, view1, position, id) -> {
                        TextView ff = view1.findViewById(android.R.id.text1);
                        Uri uri = Uri.parse(path + ff.getText().toString());
                        File check = new File(uri.getPath());
                        if (!check.exists()) {
                            Toast.makeText(getContext(), "file doesn't exist", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), FragmentsActivity.class);
                            startActivity(intent);
                        } else {
                            startActivity(new Intent(getContext(), PlayerActivity.class)
                                    .putStringArrayListExtra("songs", (ArrayList<String>) main_list)
                                    .putExtra("position", position));
                        }
                    });
        }
        return view;
    }

    @Override
    public void onDetach() {
        mView = null;
        super.onDetach();
    }
    private void findMusicFiles (File file) {
        File [] files = file.listFiles();
        main_list.clear();
        if(files!= null) {
            for (File currentFiles : files) {
                if (currentFiles.getName().endsWith(".mp3") || currentFiles.getName().endsWith(".m4a") || currentFiles.getName().endsWith(".wav") || currentFiles.getName().endsWith(".mp4")) {
                    main_list.add(0,currentFiles.getName());
                }
            }
        }
    }
}
