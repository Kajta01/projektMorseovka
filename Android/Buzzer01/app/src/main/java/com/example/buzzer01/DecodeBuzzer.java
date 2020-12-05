package com.example.buzzer01;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.Math;

import com.google.android.material.chip.Chip;

public class DecodeBuzzer extends AppCompatActivity {
    static float SOUND_LIMIT = 0.02f;
    static int BOOST = 10;
    static int FREQUENCY = 12000;

    boolean run = false;


    private AudioRecord audioRecord;
    private DecodeBuzzer.RecordAudioTask recordTask;
    private float[] buffer;
    private int sampleCount = 128 * 8; //512
    private final int REQUEST_PERMISSION = 200;

    StringBuilder morseValue = new StringBuilder();
    StringBuilder AllRereceiveData = new StringBuilder();

    private Button startStopButton;
    private Button clearButton;
    private ImageView graphView;
    private TextView measureValue;
    private TextView morseValueView;
    private TextView morseSymbol;
    private TextView morseText;

    private TextView textViewFreqValue;
    private TextView textViewTreshValue;

    private SeekBar seekBarTresh;
    private SeekBar seekBarFreq;
    private CheckBox checkMore;
    private LinearLayout moreLayout;

    private Paint paintSnd;
    private Canvas canvasSnd;
    private int countToRefresh = 1000;
    private int imageViewHeight = 200;
    private int counterToRefresh = 0;
    private double micMax = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        buffer = new float[sampleCount];

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        countToRefresh = metrics.widthPixels;

        graphView = this.findViewById(R.id.graphView);
        morseSymbol = this.findViewById(R.id.morseSymbol);
        morseText = this.findViewById(R.id.morseText);
        measureValue = this.findViewById(R.id.measureValue);
        morseValueView = this.findViewById(R.id.morseValueView);
        morseValueView.setMovementMethod(new ScrollingMovementMethod());

        startStopButton = this.findViewById(R.id.StartButton);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (run) {
                    cancelRecording();
                } else {
                    if (getPermission()) {
                        startRecording();
                    }
                }
            }
        });

        clearButton = this.findViewById(R.id.ClearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearData();
            }
        });

        seekBarTresh = this.findViewById(R.id.seekBarTresh);
        seekBarTresh.setOnSeekBarChangeListener(new SeekTreshListener());

        seekBarFreq = this.findViewById(R.id.seekBarFreq);
        seekBarFreq.setOnSeekBarChangeListener(new SeekFreqListener());

        checkMore = findViewById(R.id.checkMore);
        checkMore.setOnCheckedChangeListener(new moreCheckedListener());
        moreLayout = this.findViewById(R.id.moreLayout);

        textViewFreqValue = findViewById(R.id.textViewFreqValue);
        textViewTreshValue = findViewById(R.id.textViewTreshValue);

        Morse.initMorse();
        ClearData();

        this.setUpSndInTime();

        this.setSeekTresh();
        this.setSeekFreq();

        if (this.getPermission()) this.setUpMicrophone();
    }

    // nastavení Seek bar
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setSeekTresh() {
        seekBarTresh.setMax(10);
        seekBarTresh.setMin(0);
        seekBarTresh.incrementProgressBy(1);
        seekBarTresh.setProgress((int) (SOUND_LIMIT * 100));
        textViewTreshValue.setText(String.valueOf(SOUND_LIMIT));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setSeekFreq() {
        seekBarFreq.setMax(FREQUENCY + 8000);
        seekBarFreq.setMin(FREQUENCY - 8000);
        seekBarFreq.incrementProgressBy(100);
        seekBarFreq.setProgress(FREQUENCY);
        textViewFreqValue.setText(String.valueOf(FREQUENCY));
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelRecording();
    }

    private void setUpMicrophone() {
        // Nastavení mikrofonu
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_FLOAT;
        int bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, channelConfiguration, audioEncoding);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY,
                channelConfiguration, audioEncoding, bufferSize);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startRecording() {
        this.setUpMicrophone();
        run = true;
        startStopButton.setText("Stop");

        startStopButton.getBackground().setColorFilter(getColor(R.color.darkGreen), PorterDuff.Mode.MULTIPLY);
        recordTask = new DecodeBuzzer.RecordAudioTask();
        recordTask.execute();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void cancelRecording() {
        run = false;
        startStopButton.setText("Start");
        startStopButton.getBackground().clearColorFilter();

        if (audioRecord != null) {
            audioRecord.stop();
        }
    }

    private void ClearData() {
        morseValue.delete(0, morseValue.length());
        AllRereceiveData.delete(0, AllRereceiveData.length());
        morseValueView.setText("");
        measureValue.setText("");

        Morse.clearMorseChar();
        morseSymbol.setText("");

        Morse.clearSolving();
        morseText.setText("");

        setUpSndInTime();
        counterToRefresh = 0;
    }

    private boolean getPermission() {
        try {
            // Permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_PERMISSION);
                return false;
            } else return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\n !!!!!!!\n" + e.toString());
        }
        return false;
    }

    // Případ, kdy jsou permission zamítnuty
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Nelze použít mikrofon dokud aplikace nezíská oprávnění", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setUpSndInTime() {
        // Nastavení vykreslování audia v závislosti na čase (imageView) + vykreslení dodnotící urovně
        Bitmap bitmapSnd = Bitmap.createBitmap(countToRefresh, imageViewHeight + 1, Bitmap.Config.ARGB_8888);
        canvasSnd = new Canvas(bitmapSnd);
        paintSnd = new Paint();
        paintSnd.setColor(Color.BLACK);
        graphView.setImageBitmap(bitmapSnd);

        canvasSnd.drawLine(0, imageViewHeight - (imageViewHeight * SOUND_LIMIT * BOOST), countToRefresh, imageViewHeight - (imageViewHeight * SOUND_LIMIT * BOOST), paintSnd);

        paintSnd.setColor((R.color.colorPrimary));
    }

    /******************************************************************/
    private class RecordAudioTask extends AsyncTask<Void, Void, Void> {
        private int maxIndex;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                audioRecord.startRecording();

                while (run) {
                    // Získání vzorků
                    int bufferReadResult = audioRecord.read(buffer, 0, sampleCount, AudioRecord.READ_BLOCKING);

                    micMax = 0;

                    double value = 0;
                    int valueMorse = 0;
                    for (int i = 0; i < sampleCount && i < bufferReadResult; i++) {

                        value = buffer[i];

                        if (micMax < value) {
                            micMax = value;
                        }
                    }

                    if (micMax > 0.5) micMax = 0.5;
                    if (micMax < 0.001) micMax = 0;
                    measureValue.setText(String.valueOf(micMax));

                    if (micMax > SOUND_LIMIT) {
                        valueMorse = 1;
                    }
                    morseValue.append(valueMorse);
                    AllRereceiveData.append(valueMorse);

                    if ((morseValue.length() > 1) && valueMorse == 0) {
                        Morse.SolveSymbol(morseValue.toString(), morseValue.length());

                        morseValue.delete(0, morseValue.length());
                        AllRereceiveData.append(",");
                    }

                    onProgressUpdate();
                }
                // END OF WHILE

                if (audioRecord != null) audioRecord.stop();
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        protected void onProgressUpdate() {
            try {
                int value = maxIndex;

                this.drawSndInTime();

                // Tisk textu
                morseValueView.setText(AllRereceiveData.toString());
                morseSymbol.setText(Morse.getMorseChar());
                morseText.setText(Morse.getSolving());

            } catch (Throwable e) {
                e.printStackTrace();
                Log.e("Graph", "Selhalo vykresleni");
            }
        }

        private void drawSndInTime() {
            // Vykreslení maximální hodnoty z audia vzorku
            if (counterToRefresh >= countToRefresh) {
                System.out.println("\n refresh!\n");
                counterToRefresh = 0;
                canvasSnd.drawColor(getColor(R.color.colorPrimaryI));
            }

            float Maximum = (float) ((((micMax) * imageViewHeight)) * BOOST) + 1;

            canvasSnd.drawLine(counterToRefresh, imageViewHeight - 1, counterToRefresh, imageViewHeight - Maximum, paintSnd);
            canvasSnd.drawLine(counterToRefresh+1, imageViewHeight - 1, counterToRefresh+1, imageViewHeight - Maximum, paintSnd);

            graphView.invalidate();
            counterToRefresh=counterToRefresh+2;
        }
    }


    /********************Skrytí/zobrazení dalších možností **********************************************/

    private class moreCheckedListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                moreLayout.setVisibility(View.VISIBLE);
                // LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) moreLayout.getLayoutParams();
                // params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                // moreLayout.setLayoutParams(params);


            } else {
                moreLayout.setVisibility(View.INVISIBLE);

               // LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) moreLayout.getLayoutParams();
                // params.height = 0;
               // moreLayout.setLayoutParams(params);

            }
        }
    }

    /******************** Změna frekvence **********************************************/
    private class SeekFreqListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (progress > 1000 && fromUser) {
                FREQUENCY = progress;
                textViewFreqValue.setText(String.valueOf(FREQUENCY));

                //obnovení
                cancelRecording();
                startRecording();
                ClearData();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    /******************** Zmena threshold**********************************************/
    private class SeekTreshListener implements SeekBar.OnSeekBarChangeListener {


        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            SOUND_LIMIT = (float) (progress / 100.0);
            textViewTreshValue.setText(String.valueOf(SOUND_LIMIT));
            ClearData();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

}