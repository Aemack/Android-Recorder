package com.example.record;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordFragment extends Fragment implements View.OnClickListener {


    private String recordFile;
    private NavController navController;
    private TextView recordFilename;
    private ImageButton listBtn;
    private ImageButton recordBtn;
    private boolean isRecording = false;
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;
    private MediaRecorder mediaRecorder;
    public RecordFragment() {
        // Required empty public constructor
    }

    private Chronometer timer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved){

        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        listBtn = view.findViewById(R.id.record_list);
        recordBtn = view.findViewById(R.id.record_button);

        timer = view.findViewById(R.id.record_timer);
        listBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        //Switch for which button is clicked
        switch (v.getId()){
            // Menu button
            case R.id.record_list:
                navController.navigate((R.id.action_recordFragment_to_audioListFragment));
                break;
            // Record Button
            case R.id.record_button:
                if (isRecording){
                    //Stop Recording
                    stopRecording();
                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.microphone));
                    isRecording = false;
                } else {
                    //Start Recording
                    if(checkPermissions()){
                        startRecording();
                        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.rec));
                        isRecording = true;
                    }
                }
                break;

        }
    }

    private void startRecording() {
        // Sets timer to 0
        timer.setBase(SystemClock.elapsedRealtime());

        // Starts timer
        timer.start();

        //Sets path to save file
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        // Sets format for date and creates new Date
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd__hh_mm__ss", Locale.ENGLISH);
        Date now = new Date();

        // Names recording file with date
        recordFile = "Recording_"+formatter.format(now)+".3gp";

        recordFilename = getView().findViewById(R.id.record_filename);

        recordFilename.setText("Recording! File Name: \n"+recordFile);
        //Instantiate mediaRecorder and set up recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath+"/"+recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    private void stopRecording() {
        //Stop timer
        timer.stop();

        //Stops recording
        mediaRecorder.stop();

        //Releases the file
        mediaRecorder.release();

        // Empties mediaRecorder
        mediaRecorder = null;

        recordFilename = getView().findViewById(R.id.record_filename);

        recordFilename.setText("Recording Saved as:\n "+recordFile);


    }


    private boolean checkPermissions() {
        if(ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED){
            //Have Permission
            return true;
        } else {
            //No Permission
            //Request Permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }
}