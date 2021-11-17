    package com.Mic.microphonebooster;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.filters.LowPassFS;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.writer.WriterProcessor;

public class FragmentChat extends Fragment {
    String ChosenFormat;
    EditText record_text_view;
    TextView extension;
    ImageView record_view;
    SeekBar mic_gain_Seekbar;
    SharedPreferences sharedPrefs;
    private AudioDispatcher dispatcher;
    Thread recordingThread;
    CheckBox noise_remove;
    public boolean getValidSampleRates(int current_rate) {
        int bufferSize = AudioRecord.getMinBufferSize(current_rate, AudioFormat.CHANNEL_CONFIGURATION_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
        return bufferSize > 0;
    }
    public static void triggerRebirth(Context context) {
        Intent intent = new Intent(context, FragmentsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Runtime.getRuntime().exit(0);
    }
    Uri path_save = null;

    public void createFolder() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File fsd = Environment.getExternalStorageDirectory();
            String filePath = fsd.getAbsolutePath() + "/microphone_booster/";
            File dir = new File(filePath);
            if (!dir.isDirectory() || !dir.exists()) {
                if (dir.mkdir())
                    path_save = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/microphone_booster/" );
            }
            else
                path_save = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/microphone_booster/");
        }
        else {
            File file = new File(requireActivity().getExternalFilesDir(null) + "/microphone_booster/");
            if (!file.exists()) {
                if (file.mkdir())
                    path_save = Uri.parse((requireActivity().getExternalFilesDir(null) + "/microphone_booster/"));
            }
            else
                path_save = Uri.parse((requireActivity().getExternalFilesDir(null) + "/microphone_booster/"));
        }
    }

    @Override
    public void onDestroy() {
        if (dispatcher != null && !dispatcher.isStopped()) {
            dispatcher.stop();
            record_view.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_mic_record_foreground));
        }
        super.onDestroy();
    }
    public void record() {
        SharedPreferences.Editor settingsEditor = sharedPrefs.edit();
        String hertz = sharedPrefs.getString("Sample_Rate", "44kHz(CD)");
        int foo = Integer.parseInt(hertz.replaceAll("[^\\d]", ""));
        foo *= 1000;
        int var3 = AudioRecord.getMinBufferSize(foo, 16, 2);
        int buffer_size = var3*2;
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(foo, buffer_size, 0);
        RandomAccessFile outputFile = null;
        try {
            outputFile = new RandomAccessFile(path_save.toString() + record_text_view.getText() + ChosenFormat, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String recordType = sharedPrefs.getString("recordtype", "mono");
        final TarsosDSPAudioFormat outputFormat;
        if (!getValidSampleRates(foo))
        {
            Toast.makeText(getActivity(),"sample rate not supported",Toast.LENGTH_SHORT).show();
            foo = 8000;
            settingsEditor.putString("Sample_Rate","8 kHz(lowest)").apply();
        }
        if(recordType.equals("mono")) {
            outputFormat = new TarsosDSPAudioFormat(foo, 16, 1, true, false);
        }
        else{
            outputFormat = new TarsosDSPAudioFormat(foo, 16, 2, true, false);
        }
        WriterProcessor writer = new WriterProcessor(outputFormat, outputFile);
        GainProcessor gainProcessor = new GainProcessor(3.0);
        if(mic_gain_Seekbar != null) {
             gainProcessor = new GainProcessor(mic_gain_Seekbar.getProgress());
        }
        if(noise_remove.isChecked())
            dispatcher.addAudioProcessor(new LowPassFS(1200,foo));
        dispatcher.addAudioProcessor(gainProcessor);
        dispatcher.addAudioProcessor(writer);
        recordingThread = new Thread(dispatcher, "Audio Dispatcher)");
        recordingThread.start();
    }

    private void requestPermission()
    { 
        requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE},1000);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();
                triggerRebirth(getActivity());
            }
        }
    }
    private boolean CheckPermissionFromDevice()
    {
        int write_external_storage_result = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO);
        int read_external_storage_result = ContextCompat.checkSelfPermission(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED && read_external_storage_result == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }
    boolean record_face = true;
    private SharedPreferences save;
    private RecorderViewModel recorderViewModel;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        noise_remove = view.findViewById(R.id.remove_checkbox);
        mic_gain_Seekbar = view.findViewById(R.id.seekbar_mic_gain);
        record_text_view = view.findViewById(R.id.recordName);
        recorderViewModel = new ViewModelProvider(requireActivity()).get(RecorderViewModel.class);
        save = this.requireActivity().getSharedPreferences("save", MODE_PRIVATE);
        if (save == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("records", 1);
            editor.apply();
        }
        createFolder();
        if(path_save == null)
        {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Failed to create folder")
                    .setMessage("Check if you have storage in your device and accepted the permissions")
                    .setPositiveButton("OK", (dialog, which) -> {
                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        String record_name = "record_" + save.getInt("records", 1) + "";
        AudioManager mAudioManager = (AudioManager) requireActivity().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        extension = view.findViewById(R.id.Record_extension);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ChosenFormat = sharedPrefs.getString("chosenFormat", ".mp3");
        extension.setText(ChosenFormat);
        record_view = view.findViewById(R.id.record_button);
        record_view.setOnClickListener(v -> {
            if (record_face) {
                if(record_text_view.getText().toString().equals(""))
                {
                    Toast.makeText(getActivity(),"please insert a file name",Toast.LENGTH_SHORT)
                            .show();
                }
                else {
                    if (CheckPermissionFromDevice()) {
                        record();
                        record_face = false;
                        record_view.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_mic_stop_foreground));
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setMessage("You must allow all permissions, as they are needed" +
                                        " for the functioning of the app")
                                .setPositiveButton("OK", (dialog, which) -> requestPermission())
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }
            }
            else {
                if (!dispatcher.isStopped())
                    dispatcher.stop();
                record_face = true;
                save.edit().putInt("records", save.getInt("records", 1) + 1).apply();
                record_text_view.setText("record_" + save.getInt("records",1));
                record_view.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_mic_record_foreground));
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
}
