package cz.vsb.kameratest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Preview;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;



public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    int JAS = 0;
    int COUNTPX = 0;
    public int shinning = 0;

    private TextureView textureView;
    private TextureView textureViewGray;
    private ImageView imageViewGray;
    private SeekBar seekBarJAS;
    private TextView textViewJas;
    private TextView textViewShinning;
    private EditText editTextJas;
    private EditText editTextCountPX;



    private CameraDevice cameraDevice;
    private Size cameraDimension;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;


    private int orgPreviewWidth;
    private int orgPreviewHeight;


    private Button btnCapture;
    private Bitmap pictureBitmap;
    private Bitmap pictureBitmapGray;

    private ImageView histogram;

    private int setJasVal = 0;
    private int setCountPXVal = 0;


    /************************
     * PROPERTIES
     **********************/
    /* Tag for Log */
    public static final String TAG = "PiccaFinal.EditPicturesActivity";
    /* Images Path */
    // private String[] _imagesPath;

    /* Original Images */
    private Bitmap[] _originalBitmaps = new Bitmap[2];
    /* Final Filtered Images */
    private Bitmap[] _filteredBitmaps = new Bitmap[2];

    /* Image View */
    private ImageView _filterImageView;

    private Preview mPreview;
    private DrawOnTop mDrawOnTop;
    private Boolean is_showing= false;
    private Bitmap bitmap_todecode=null;

    int[] pixels;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);





        //setContentView(R.layout.activity_edit_pictures);

        Intent intent = getIntent();
        //_imagesPath = intent.getStringArrayExtra(MainActivity.IMAGES_PATH);

        //for (int i = MainActivity.LEFT_IMAGE; i <= MainActivity.RIGHT_IMAGE; i++) {
        //    _originalBitmaps[i] = BitmapFactory.decodeFile(_imagesPath[i]);
        //}

        _filterImageView = (ImageView) findViewById(R.id.filter_image_view);
       // _filterImageView
        //        .setImageBitmap(_originalBitmaps[MainActivity.LEFT_IMAGE]);
       // setBitmap_todecode(_originalBitmaps[MainActivity.LEFT_IMAGE]);
        // --------------------------------------------------------------------------------

        // display_RGB
        LinearLayout ll = (LinearLayout) this.findViewById(R.id.lyt_color);

        Button display_RGBButton = (Button) findViewById(R.id.display_RGB);
        display_RGBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (is_showing) { //if is already showing you hide the layout
                    ll.setVisibility(View.GONE);
                } else {         //else you draw it with your custom view - the view should be in your class
                    ll.setVisibility(View.VISIBLE);
                    ll.addView(new DrawOnTop(getApplicationContext()));
                }
            }
            });



        textureView = findViewById(R.id.textureViewImage);
        textureView.setSurfaceTextureListener(textureListener);


        imageViewGray = findViewById(R.id.imageViewGray);
        //imageViewGray.setImageBitmap(pictureBitmapGray);


        textViewJas = (TextView) findViewById(R.id.textViewJas);

        textViewShinning = (TextView) findViewById(R.id.textViewShinning);

        editTextJas = (EditText) findViewById(R.id.editTextJas);
        editTextJas.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here

                String no=editTextJas.getText().toString();         //this will get a string
                if(no.length() != 0) {
                    setJasVal = Integer.parseInt(no);                   //this will get a no from the string
                    // yourEditText...
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        editTextCountPX = (EditText) findViewById(R.id.editTextCountPX);
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





        seekBarJAS = findViewById(R.id.seekBarJAS);
        seekBarJAS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBarJAS, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                JAS = progress;
                //textViewJas.setText(String.valueOf(JAS));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBarJAS) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBarJAS) {
                // TODO Auto-generated method stub
            }
        });



        btnCapture = findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //capture();

                            // a potentially time consuming task
                            pictureBitmap = textureView.getBitmap();
                            BitmapTransfer.Bitmap = pictureBitmap;
                            byte[] array = ImageAsMatrix.getArrayFromBitmap(pictureBitmap);
                            ImageActivity.toGrayscale(array);
                            pictureBitmapGray =ImageAsMatrix.getBitmapFromArray(array,textureView.getWidth() ,textureView.getHeight() );
                            imageViewGray.setImageBitmap(ImageAsMatrix.getBitmapFromArray(array,textureView.getWidth() ,textureView.getHeight()));

            }
        });
    }



    private void capture() {
        pictureBitmap = textureView.getBitmap();
        BitmapTransfer.Bitmap = pictureBitmap;
        Intent i = new Intent(getApplicationContext(), ImageActivity.class);
        startActivity(i);
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
            BitmapTransfer.Bitmap = pictureBitmap;
            byte[] array = ImageAsMatrix.getArrayFromBitmap(pictureBitmap);
            ImageActivity.toGrayscale(array);
            pictureBitmapGray =ImageAsMatrix.getBitmapFromArray(array,textureView.getWidth() ,textureView.getHeight() );
            imageViewGray.setImageBitmap(ImageAsMatrix.getBitmapFromArray(array,textureView.getWidth() ,textureView.getHeight()));
            shinning = ImageAsMatrix.GetCountOfWhiteFromBitmap(pictureBitmap, setJasVal, setCountPXVal);

            textViewShinning.setText("" + shinning);
            setBitmap_todecode(pictureBitmapGray);
        }
    };


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
                    Toast.makeText(MainActivity.this, "Changed", Toast.LENGTH_SHORT).show();
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




    /************************
     * ANDROID METHODS
     **********************/
    //@Override
    // protected void onCreate(Bundle savedInstanceState) {
    //     super.onCreate(savedInstanceState);

    // }

    /**
     * @return the bitmap_todecode
     */
    public Bitmap getBitmap_todecode() {
        return bitmap_todecode;
    }

    /**
     * @param bitmap_todecode the bitmap_todecode to set
     */
    public void setBitmap_todecode(Bitmap bitmap_todecode) {
        this.bitmap_todecode = bitmap_todecode;
    }

    public class DrawOnTop extends View {
        Bitmap mBitmap;
        Paint mPaintBlack;
        Paint mPaintYellow;
        Paint mPaintRed;
        Paint mPaintGreen;
        Paint mPaintBlue;
        byte[] mYUVData;
        int[] mRGBData;
        int mImageWidth, mImageHeight;
        int[] mRedHistogram;
        int[] mGreenHistogram;
        int[] mBlueHistogram;
        double[] mBinSquared;

        public DrawOnTop(Context context) {
            super(context);

            mPaintBlack = new Paint();
            mPaintBlack.setStyle(Paint.Style.FILL);
            mPaintBlack.setColor(Color.BLACK);
            mPaintBlack.setTextSize(25);

            mPaintYellow = new Paint();
            mPaintYellow.setStyle(Paint.Style.FILL);
            mPaintYellow.setColor(Color.YELLOW);
            mPaintYellow.setTextSize(25);

            mPaintRed = new Paint();
            mPaintRed.setStyle(Paint.Style.FILL);
            mPaintRed.setColor(Color.RED);
            mPaintRed.setTextSize(25);

            mPaintGreen = new Paint();
            mPaintGreen.setStyle(Paint.Style.FILL);
            mPaintGreen.setColor(Color.GREEN);
            mPaintGreen.setTextSize(25);

            mPaintBlue = new Paint();
            mPaintBlue.setStyle(Paint.Style.FILL);
            mPaintBlue.setColor(Color.BLUE);
            mPaintBlue.setTextSize(25);

            mBitmap = null;
            mYUVData = null;
            mRGBData = null;
            mRedHistogram = new int[256];
            mGreenHistogram = new int[256];
            mBlueHistogram = new int[256];
            mBinSquared = new double[256];
            for (int bin = 0; bin < 256; bin++)
            {
                mBinSquared[bin] = ((double)bin) * bin;
            } // bin
        }

        @Override
        protected void onDraw(Canvas canvas) {
            mBitmap= getBitmap_todecode();
            if (mBitmap != null)
            {
                int canvasWidth = getWidth(); //canvas.getWidth()
                int canvasHeight = getHeight(); //canvas.getHeight()
                int newImageWidth = canvasWidth;
                int newImageHeight = canvasHeight;
                int marginWidth = (canvasWidth - newImageWidth)/2;

                // Convert from YUV to RGB
                decodeYUV420SP(mRGBData, mYUVData, mImageWidth, mImageHeight);

                // Draw bitmap
                mBitmap.setPixels(mRGBData, 0, mImageWidth, 0, 0,
                        mImageWidth, mImageHeight);
                Rect src = new Rect(0, 0, mImageWidth, mImageHeight);
                Rect dst = new Rect(marginWidth, 0,
                        canvasWidth-marginWidth, canvasHeight);
                canvas.drawBitmap(mBitmap, src, dst, mPaintBlack);

                // Draw black borders
                canvas.drawRect(0, 0, marginWidth, canvasHeight, mPaintBlack);
                canvas.drawRect(canvasWidth - marginWidth, 0,
                        canvasWidth, canvasHeight, mPaintBlack);

                // Calculate histogram
                calculateIntensityHistogram(mRGBData, mRedHistogram,
                        mImageWidth, mImageHeight, 0);
                calculateIntensityHistogram(mRGBData, mGreenHistogram,
                        mImageWidth, mImageHeight, 1);
                calculateIntensityHistogram(mRGBData, mBlueHistogram,
                        mImageWidth, mImageHeight, 2);

                // Calculate mean
                double imageRedMean = 0, imageGreenMean = 0, imageBlueMean = 0;
                double redHistogramSum = 0, greenHistogramSum = 0, blueHistogramSum = 0;
                for (int bin = 0; bin < 256; bin++)
                {
                    imageRedMean += mRedHistogram[bin] * bin;
                    redHistogramSum += mRedHistogram[bin];
                    imageGreenMean += mGreenHistogram[bin] * bin;
                    greenHistogramSum += mGreenHistogram[bin];
                    imageBlueMean += mBlueHistogram[bin] * bin;
                    blueHistogramSum += mBlueHistogram[bin];
                } // bin
                imageRedMean /= redHistogramSum;
                imageGreenMean /= greenHistogramSum;
                imageBlueMean /= blueHistogramSum;

                // Calculate second moment
                double imageRed2ndMoment = 0, imageGreen2ndMoment = 0, imageBlue2ndMoment = 0;
                for (int bin = 0; bin < 256; bin++)
                {
                    imageRed2ndMoment += mRedHistogram[bin] * mBinSquared[bin];
                    imageGreen2ndMoment += mGreenHistogram[bin] * mBinSquared[bin];
                    imageBlue2ndMoment += mBlueHistogram[bin] * mBinSquared[bin];
                } // bin
                imageRed2ndMoment /= redHistogramSum;
                imageGreen2ndMoment /= greenHistogramSum;
                imageBlue2ndMoment /= blueHistogramSum;
                double imageRedStdDev = Math.sqrt( imageRed2ndMoment - imageRedMean*imageRedMean );
                double imageGreenStdDev = Math.sqrt( imageGreen2ndMoment - imageGreenMean*imageGreenMean );
                double imageBlueStdDev = Math.sqrt( imageBlue2ndMoment - imageBlueMean*imageBlueMean );

                // Draw mean
                String imageMeanStr = "Mean (R,G,B): " + String.format("%.4g", imageRedMean) + ", " + String.format("%.4g", imageGreenMean) + ", " + String.format("%.4g", imageBlueMean);
                canvas.drawText(imageMeanStr, marginWidth+10-1, 30-1, mPaintBlack);
                canvas.drawText(imageMeanStr, marginWidth+10+1, 30-1, mPaintBlack);
                canvas.drawText(imageMeanStr, marginWidth+10+1, 30+1, mPaintBlack);
                canvas.drawText(imageMeanStr, marginWidth+10-1, 30+1, mPaintBlack);
                canvas.drawText(imageMeanStr, marginWidth+10, 30, mPaintYellow);

                // Draw standard deviation
                String imageStdDevStr = "Std Dev (R,G,B): " + String.format("%.4g", imageRedStdDev) + ", " + String.format("%.4g", imageGreenStdDev) + ", " + String.format("%.4g", imageBlueStdDev);
                canvas.drawText(imageStdDevStr, marginWidth+10-1, 60-1, mPaintBlack);
                canvas.drawText(imageStdDevStr, marginWidth+10+1, 60-1, mPaintBlack);
                canvas.drawText(imageStdDevStr, marginWidth+10+1, 60+1, mPaintBlack);
                canvas.drawText(imageStdDevStr, marginWidth+10-1, 60+1, mPaintBlack);
                canvas.drawText(imageStdDevStr, marginWidth+10, 60, mPaintYellow);

                // Draw red intensity histogram
                float barMaxHeight = 3000;
                float barWidth = ((float)newImageWidth) / 256;
                float barMarginHeight = 2;
                RectF barRect = new RectF();
                barRect.bottom = canvasHeight - 200;
                barRect.left = marginWidth;
                barRect.right = barRect.left + barWidth;
                for (int bin = 0; bin < 256; bin++)
                {
                    float prob = (float)mRedHistogram[bin] / (float)redHistogramSum;
                    barRect.top = barRect.bottom -
                            Math.min(80,prob*barMaxHeight) - barMarginHeight;
                    canvas.drawRect(barRect, mPaintBlack);
                    barRect.top += barMarginHeight;
                    canvas.drawRect(barRect, mPaintRed);
                    barRect.left += barWidth;
                    barRect.right += barWidth;
                } // bin

                // Draw green intensity histogram
                barRect.bottom = canvasHeight - 100;
                barRect.left = marginWidth;
                barRect.right = barRect.left + barWidth;
                for (int bin = 0; bin < 256; bin++)
                {
                    barRect.top = barRect.bottom - Math.min(80, ((float)mGreenHistogram[bin])/((float)greenHistogramSum) * barMaxHeight) - barMarginHeight;
                    canvas.drawRect(barRect, mPaintBlack);
                    barRect.top += barMarginHeight;
                    canvas.drawRect(barRect, mPaintGreen);
                    barRect.left += barWidth;
                    barRect.right += barWidth;
                } // bin

                // Draw blue intensity histogram
                barRect.bottom = canvasHeight;
                barRect.left = marginWidth;
                barRect.right = barRect.left + barWidth;
                for (int bin = 0; bin < 256; bin++)
                {
                    barRect.top = barRect.bottom - Math.min(80, ((float)mBlueHistogram[bin])/((float)blueHistogramSum) * barMaxHeight) - barMarginHeight;
                    canvas.drawRect(barRect, mPaintBlack);
                    barRect.top += barMarginHeight;
                    canvas.drawRect(barRect, mPaintBlue);
                    barRect.left += barWidth;
                    barRect.right += barWidth;
                } // bin
            } // end if statement

            super.onDraw(canvas);

        } // end onDraw method

        public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
            final int frameSize = width * height;

            for (int j = 0, yp = 0; j < height; j++) {
                int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
                for (int i = 0; i < width; i++, yp++) {
                    int y = (0xff & ((int) yuv420sp[yp])) - 16;
                    if (y < 0) y = 0;
                    if ((i & 1) == 0) {
                        v = (0xff & yuv420sp[uvp++]) - 128;
                        u = (0xff & yuv420sp[uvp++]) - 128;
                    }

                    int y1192 = 1192 * y;
                    int r = (y1192 + 1634 * v);
                    int g = (y1192 - 833 * v - 400 * u);
                    int b = (y1192 + 2066 * u);

                    if (r < 0) r = 0; else if (r > 262143) r = 262143;
                    if (g < 0) g = 0; else if (g > 262143) g = 262143;
                    if (b < 0) b = 0; else if (b > 262143) b = 262143;

                    rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
                }
            }
        }

        public void decodeYUV420SPGrayscale(int[] rgb, byte[] yuv420sp, int width, int height)
        {
            final int frameSize = width * height;

            for (int pix = 0; pix < frameSize; pix++)
            {
                int pixVal = (0xff & ((int) yuv420sp[pix])) - 16;
                if (pixVal < 0) pixVal = 0;
                if (pixVal > 255) pixVal = 255;
                rgb[pix] = 0xff000000 | (pixVal << 16) | (pixVal << 8) | pixVal;
            } // pix
        }

        public void calculateIntensityHistogram(int[] rgb, int[] histogram, int width, int height, int component)
        {
            for (int bin = 0; bin < 256; bin++)
            {
                histogram[bin] = 0;
            } // bin
            if (component == 0) // red
            {
                for (int pix = 0; pix < width*height; pix += 3)
                {
                    int pixVal = (rgb[pix] >> 16) & 0xff;
                    histogram[ pixVal ]++;
                } // pix
            }
            else if (component == 1) // green
            {
                for (int pix = 0; pix < width*height; pix += 3)
                {
                    int pixVal = (rgb[pix] >> 8) & 0xff;
                    histogram[ pixVal ]++;
                } // pix
            }
            else // blue
            {
                for (int pix = 0; pix < width*height; pix += 3)
                {
                    int pixVal = rgb[pix] & 0xff;
                    histogram[ pixVal ]++;
                } // pix
            }
        }
    }



}
