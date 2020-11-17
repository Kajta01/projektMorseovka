package com.example.buzzer01;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static double SOUND_LIMIT = 0.01;

    boolean run = false;

    private int frequency = 10000;
    private AudioRecord audioRecord;
    private RecordAudioTask recordTask;
    private float[] buffer;
    private int sampleCount = 128*8; //512
    private final int REQUEST_PERMISSION = 200;

    StringBuilder morseValue = new StringBuilder();
    StringBuilder AllRereceiveData = new StringBuilder();

    private Button startStopButton;
    private Button clearButton;
    private ImageView graphView;
    private TextView morseView;
    private TextView morseValueView;
    private TextView morseSymbol;

    private Paint paintSnd;
    private Canvas canvasSnd;
    private int countToRefresh=1000;
    private int imageViewHeight=250;
    private int counterToRefresh=0;
    private double micMax=0;

    private int seekBarValue = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buffer = new float[sampleCount];

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        countToRefresh = metrics.widthPixels;

        graphView = this.findViewById(R.id.graphView);
        morseSymbol = this.findViewById(R.id.morseSymbol);
        morseView = this.findViewById(R.id.morseView);
        morseValueView = this.findViewById(R.id.morseValueView);
        startStopButton = this.findViewById(R.id.StartButton);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (run) {
                    cancelRecording();
                } else {
                    if(getPermission()) {
                        startRecording(); } } }});
        clearButton = this.findViewById(R.id.ClearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { ClearData();}});

        this.setUpSndInTime();

        if(this.getPermission()) this.setUpMicrophone();
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
        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

        audioRecord = new AudioRecord( MediaRecorder.AudioSource.MIC, frequency,
                channelConfiguration, audioEncoding, bufferSize);
    }
    private void startRecording(){
        this.setUpMicrophone();
        run = true;
        startStopButton.setText("Stop");
       startStopButton.setBackgroundColor(getColor(R.color.darkGreen));
        recordTask = new RecordAudioTask();
        recordTask.execute();

    }
    private void cancelRecording(){
        run = false;
        startStopButton.setText("Start");
        startStopButton.setBackgroundColor(getColor(R.color.darkRed));
        if(audioRecord != null) {
            audioRecord.stop();
            recordTask.cancel(true);
        }
    }
    private void ClearData(){
        morseValue.delete(0,morseValue.length());
        AllRereceiveData.delete(0,  AllRereceiveData.length());
        morseValueView.setText("");
        morseView.setText("");

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
            }
            else return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\n !!!!!!!\n"+e.toString());
        }
        return false;
    }
    // Případ, kdy jsou permission zamítnuty
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Nelze použít mikrofon dokud aplikace nezíská oprávnění", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setUpSndInTime() {
        // Nastavení vykreslování audia v závislosti na čase (imageView)
        Bitmap bitmapSnd = Bitmap.createBitmap(countToRefresh, imageViewHeight, Bitmap.Config.ARGB_8888);
        canvasSnd = new Canvas(bitmapSnd);
        paintSnd = new Paint();
        paintSnd.setColor(Color.RED);
        graphView.setImageBitmap(bitmapSnd);
    }
    private class RecordAudioTask extends AsyncTask<Void, Void, Void> {
        private int maxIndex;
        private int onPushCounter;
        @Override
        protected Void doInBackground(Void... voids) {
            try{
            audioRecord.startRecording();
            onPushCounter = 5 + seekBarValue;
            while (run) {
                // Získání vzorků
                int bufferReadResult = audioRecord.read(buffer, 0, sampleCount, AudioRecord.READ_BLOCKING);

                //System.out.println("\n result:"+ bufferReadResult + "\n");
                // Reset hodnot
                 micMax = 0;


                // Úprava vstupním hodnot na rozsah -1 až 1
                double value = 0;
                int valueMorse = 0;
                for (int i = 0; i < sampleCount && i < bufferReadResult; i++) {

                    value = buffer[i];

                    if (micMax < value) {
                        micMax = value;
                    }

                }
                morseView.setText(String.valueOf(micMax));

                if(micMax > SOUND_LIMIT)  {valueMorse = 1;}
                morseValue.append(valueMorse);
                AllRereceiveData.append(valueMorse);

                if((morseValue.length() >  1) && valueMorse == 0)
                {
                    Morse.SolveSymbol(morseValue.toString(),morseValue.length() );

                    morseValue.delete(0,morseValue.length());
                    AllRereceiveData.append(",");
                }



                onProgressUpdate();

            }
            // END OF WHILE
            if(audioRecord != null) audioRecord.stop();
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("AudioRecord", "Recording Failed");
        }
            return null;
        }

        protected void onProgressUpdate() {
            try {
                int value = maxIndex;

                // Zavolání funkcí
                this.drawSndInTime();


                // Tisk textu
                morseValueView.setText(AllRereceiveData.toString());
                morseSymbol.setText(Morse.getMorseChar());


                if(onPushCounter == 0)
                    startStopButton.setBackgroundColor(getColor(R.color.darkRed));
            } catch (Throwable e) {
                e.printStackTrace();
                Log.e("Graph", "Selhalo vykresleni");
            }
        }
        private void drawSndInTime() {
            // Vykreslení maximální hodnoty z audia vzorku do imageView -> pod grafy
            if(counterToRefresh >= countToRefresh)
            {
                System.out.println("\n refresh!\n");
                counterToRefresh = 0;
                 canvasSnd.drawColor(getColor(R.color.darkRed));
            }

            float Maximum = (float)((micMax)*imageViewHeight)+1;
            canvasSnd.drawLine(counterToRefresh, imageViewHeight/2f+Maximum, counterToRefresh, imageViewHeight/2f-Maximum, paintSnd);
            graphView.invalidate();
            counterToRefresh++;
        }

    }
}