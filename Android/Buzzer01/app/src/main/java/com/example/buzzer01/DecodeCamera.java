package com.example.buzzer01;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;

public class DecodeCamera extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    public int shinning = 0;
    private int run = 0;

    // User interface
    private TextureView textureView;
    private ImageView imageViewGray;
    private TextView textViewMeasureValue;
    private TextView editTextCountText;
    private TextView editTextBrightText;
    private EditText editTextBright;
    private EditText editTextCountPX;

    private Button StartButton;
    private Button ClearButton;

    private TextView morseValueView;
    private TextView morseSymbol;
    private TextView morseText;
    private CheckBox checkMore;

    private LinearLayout moreLayout;


    //Camera
    private CameraDevice cameraDevice;
    private Size cameraDimension;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private Bitmap pictureBitmap;
    private Bitmap pictureBitmapGray;

    private int setBrightVal = 0;
    private int setCountPXVal = 0;
    private int maxIndex;

    StringBuilder morseValue = new StringBuilder();
    StringBuilder AllRereceiveData = new StringBuilder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        textureView = (TextureView) findViewById(R.id.textureViewImage);
        textureView.setSurfaceTextureListener(textureListener); //textureListener se vytváří mimo funkci onCreate

        imageViewGray = (ImageView) findViewById(R.id.imageViewGray);

        editTextBrightText = (TextView) findViewById(R.id.editTextBrightText);
        editTextBrightText.setText("bright level:");

        editTextCountText = (TextView) findViewById(R.id.editTextCountText);
        editTextCountText.setText("pixels:");

        editTextBright = (EditText) findViewById(R.id.editTextBright);
        editTextBright.setText("250");
        setBrightVal = 250;
        editTextBright.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here

                String no=editTextBright.getText().toString();         //this will get a string
                if(no.length() != 0) {
                    setBrightVal = Integer.parseInt(no);                   //this will get a no from the string
                    // yourEditText...
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        editTextCountPX = (EditText) findViewById(R.id.editTextCountPX);
        editTextCountPX.setText("2500");
        setCountPXVal = 2500;
        editTextCountPX.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here
                String no = editTextCountPX.getText().toString();         //this will get a string
                if(no.length() != 0) {                                     //check for empty input
                    setCountPXVal = Integer.parseInt(no);                   //this will get a no from the string
                    // yourEditText...
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        ClearButton = findViewById(R.id.ClearButton);
        ClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearData();

            }
        });

        StartButton = this.findViewById(R.id.StartButton);
        StartButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (run == 1) {
                    run = 0;
                    StartButton.setText("Start");
                    StartButton.getBackground().clearColorFilter();

                } else {
                    run = 1;
                    StartButton.setText("Stop");
                    StartButton.getBackground().setColorFilter(getColor(R.color.darkGreen), PorterDuff.Mode.MULTIPLY);
                }
            }
        });


        morseSymbol = this.findViewById(R.id.morseSymbol);
        morseText = this.findViewById(R.id.morseText);
        morseValueView = this.findViewById(R.id.morseValueView);
        morseValueView.setMovementMethod(new ScrollingMovementMethod());

        textViewMeasureValue = this.findViewById(R.id.textViewMeasureValue);

        checkMore = findViewById(R.id.checkMore);
        checkMore.setOnCheckedChangeListener(new DecodeCamera.moreCheckedListener());
        moreLayout = this.findViewById(R.id.moreLayout);

        Morse.initMorse();
        ClearData();


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



    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable()) {
            transformImage(textureView.getWidth(), textureView.getHeight());
            openCamera();
        } else
            textureView.setSurfaceTextureListener(textureListener);

    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    // Listener na surfaceTexture TextureView
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            transformImage(textureView.getWidth(), textureView.getHeight());
            openCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            pictureBitmap = textureView.getBitmap();
            //BitmapTransfer.Bitmap = pictureBitmap;
            byte[] array = BitmapOperations.getArrayFromBitmap(pictureBitmap);
            BitmapOperations.toGrayscale(array);
            pictureBitmapGray =BitmapOperations.getBitmapFromArray(array,textureView.getWidth() ,textureView.getHeight() );
            imageViewGray.setImageBitmap(BitmapOperations.getBitmapFromArray(array,textureView.getWidth() ,textureView.getHeight()));

            if(run == 1){

            shinning = BitmapOperations.getCountOfWhiteFromBitmap(pictureBitmap, setBrightVal, setCountPXVal);

            int valueMorse = shinning;
            morseValue.append(valueMorse);
            AllRereceiveData.append(valueMorse);

            if ((morseValue.length() > 1) && valueMorse == 0) {
                Morse.SolveSymbol(morseValue.toString(), morseValue.length());

                morseValue.delete(0, morseValue.length());
                AllRereceiveData.append(",");
            }
            onProgressUpdate();
            textViewMeasureValue.setText("" + shinning);
            }
        }
    };

    private void ClearData() {
        morseValue.delete(0, morseValue.length());
        AllRereceiveData.delete(0, AllRereceiveData.length());
        morseValueView.setText("");
        textViewMeasureValue.setText("");

        Morse.clearMorseChar();
        morseSymbol.setText("");

        Morse.clearSolving();
        morseText.setText("");

        //setUpSndInTime();
        //counterToRefresh = 0;
    }



    protected void onProgressUpdate() {
        try {
            int value = maxIndex;

            //this.drawSndInTime();

            // Tisk textu
            morseValueView.setText(AllRereceiveData.toString());
            morseSymbol.setText(Morse.getMorseChar());
            morseText.setText(Morse.getSolving());

        } catch (Throwable e) {
            e.printStackTrace();
            Log.e("Graph", "Selhalo vykresleni");
        }
    }

    // Vytvoření tvz. call-backu(zpětné odezvy) pro práci s kamera2 api
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            setUpCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void openCamera() {
        // Najití sensoru - kamery a všechny dostupné úkony
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];

            // Získá parametry kamery
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // assert - slouží pro určení statement
            assert map != null;

            // Zjištění rozlišení kamery
            cameraDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            // Tisk Info
            System.out.println("\n Char: " + characteristics + "\n map: " + map);
            System.out.println("\n CameraID: " + cameraId + " /// rozlišení: " + cameraDimension);

            // Zjištění oprávnění k přistupu ke kameře pro API >=23
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA//,
                        //Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CAMERA_PERMISSION);
                return;
            }

            // Otevření kamery s cameraId a poslání do funkce stateCallback, ta zavolá createCameraPreview
            manager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void setUpCameraPreview() {
        try {
            // Získání surface z objektu TextureView
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(cameraDimension.getWidth(), cameraDimension.getHeight());
            Surface surface = new Surface(texture);

            System.out.println("\n texture: " + texture + "\n surface: " + surface + "\n \n");

            // Vytvoření požadavku a přiřazení cíle našeho surface
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            // Další parametry pro pořízení snímku
            final ImageReader reader = ImageReader.newInstance(cameraDimension.getWidth(), cameraDimension.getHeight(), ImageFormat.JPEG, 1);
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());

            // Vytvoření události pro získávání sekvenčních snímků
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    cameraVideo();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(DecodeCamera.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void cameraVideo() {
        if (cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            // zacyklení pořizování snímků - video
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void transformImage(int width, int height) {
        if(textureView == null) {
            return;
        }

        Matrix matrix = new Matrix();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        RectF textureRectF = new RectF(0, 0, width, height);
        RectF previewRectF = new RectF(0, 0, textureView.getHeight(), textureView.getWidth());
        float centerX = textureRectF.centerX();
        float centerY = textureRectF.centerY();
        if(rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            previewRectF.offset(centerX - previewRectF.centerX(),
                    centerY - previewRectF.centerY());
            matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float)width / textureView.getWidth(),
                    (float)height / textureView.getHeight());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    private void updateTextureMatrix(int width, int height)
    {
        int orgPreviewWidth = 0;
        int orgPreviewHeight = 0;

        boolean isPortrait = false;

        Display display = getWindowManager().getDefaultDisplay();
        if (display.getRotation() == Surface.ROTATION_0 || display.getRotation() == Surface.ROTATION_180) isPortrait = true;
        else if (display.getRotation() == Surface.ROTATION_90 || display.getRotation() == Surface.ROTATION_270) isPortrait = false;

        int previewWidth = orgPreviewWidth;
        int previewHeight = orgPreviewHeight;

        if (isPortrait)
        {
            previewWidth = orgPreviewHeight;
            previewHeight = orgPreviewWidth;
        }

        float ratioSurface = (float) width / height;
        float ratioPreview = (float) previewWidth / previewHeight;

        float scaleX;
        float scaleY;

        if (ratioSurface > ratioPreview)
        {
            scaleX = (float) height / previewHeight;
            scaleY = 1;
        }
        else
        {
            scaleX = 1;
            scaleY = (float) width / previewWidth;
        }

        Matrix matrix = new Matrix();

        matrix.setScale(scaleX, scaleY);
        textureView.setTransform(matrix);

        float scaledWidth = width * scaleX;
        float scaledHeight = height * scaleY;

        float dx = (width - scaledWidth) / 2;
        float dy = (height - scaledHeight) / 2;
        textureView.setTranslationX(dx);
        textureView.setTranslationY(dy);
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

}
