package com.example.record;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;


public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick{

    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView audioList;
    private File[] allFiles;

    private AudioListAdapter audioListAdapter;

    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;

    private File fileToPlay = null;

    //UI Elements

    private ImageButton playBtn;
    private ImageButton forwardButton;
    private ImageButton rewindButton;
    private TextView playerHeader;
    private TextView playerFileName;



    private SeekBar playerSeekBar;
    private Handler seekBarHandler;
    private Runnable updateSeekBar;


    public AudioListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        // Gets player and bottom sheet behavior
        playerSheet = view.findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        audioList = view.findViewById(R.id.audio_list_view);

        playBtn = view.findViewById(R.id.playButton);
        forwardButton = view.findViewById(R.id.forwardButton);
        rewindButton = view.findViewById(R.id.rewindButton);

        playerHeader = view.findViewById(R.id.player_header_title);
        playerFileName = view.findViewById(R.id.player_file_name);

        playerSeekBar = view.findViewById(R.id.player_seekbar);

        // Get path to file
        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();

        // Get directory
        File directory = new File(path);

        //Get audio files
        allFiles = directory.listFiles();

        Arrays.sort(allFiles);
        audioListAdapter = new AudioListAdapter(allFiles, this);

        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(audioListAdapter);



        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Checks if media player is hidden
                if (newState == BottomSheetBehavior.STATE_HIDDEN){
                    //Returns it to minimised
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = playerSeekBar.getProgress();
                int skipTo = progress+2000;
                if (mediaPlayer.getDuration() <= progress) {

                } else {
                    Log.i("SKIP TO: ", skipTo+"");
                    mediaPlayer.seekTo(skipTo);
                }
            }
        });

        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = playerSeekBar.getProgress();
                int skipTo = progress-2000;
                if (mediaPlayer.getDuration() <= progress) {

                } else {
                    Log.i("SKIP TO: ", skipTo+"");
                    mediaPlayer.seekTo(skipTo);
                }
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (isPlaying){
                    pauseAudio();
                } else {
                    if (fileToPlay != null) {
                        resumeAudio();
                    }
                }
            }
        });

        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseAudio();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mediaPlayer.seekTo(progress);

                resumeAudio();
            }
        });
    }

    @Override
    public void onClickListener(File file, int position) {
        fileToPlay = file;
        if (isPlaying){
            //If it's already playing
            stopAudio();
        } else {
            //If its not playing
            fileToPlay = file;
        }
        playAudio(fileToPlay);

    }

    private void pauseAudio(){
        mediaPlayer.pause();
        isPlaying = false;
        playBtn.setImageDrawable(getResources().getDrawable(R.drawable.play));
        if (seekBarHandler != null) {
            seekBarHandler.removeCallbacks(updateSeekBar);
        }
    }

    private void resumeAudio(){
        mediaPlayer.start();
        isPlaying = true;
        playBtn.setImageDrawable(getResources().getDrawable(R.drawable.pause));
    }

    //Play audio
    private void playAudio(File fileToPlay){
        isPlaying = true;
        mediaPlayer = new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e){
            e.printStackTrace();
        }

        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.pause));
        playerFileName.setText(fileToPlay.getName());
        playerHeader.setText("Playing");

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
                playerHeader.setText("Finished");
            }
        });

        playerSeekBar.setMax(mediaPlayer.getDuration());
        seekBarHandler = new Handler();
        updateSeekBar = new Runnable(){
            @Override
            public void run(){
                playerSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                seekBarHandler.postDelayed(this, 500);

            }
        };
        seekBarHandler.postDelayed(updateSeekBar,0);

    }

    //Stop audio
    private void stopAudio(){
        isPlaying = false;
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.play));
        playerHeader.setText("Stopped");
        mediaPlayer.stop();
        if (seekBarHandler != null) {
            seekBarHandler.removeCallbacks(updateSeekBar);
        }
    }
}