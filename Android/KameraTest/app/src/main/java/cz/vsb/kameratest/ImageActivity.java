package cz.vsb.kameratest;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class ImageActivity extends AppCompatActivity {

    private int w = 0;
    private int h = 0;
    private byte[] array;
    private ImageView imageView;
    private Button btnClose;
    private Button btnProcess;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        bitmap = BitmapTransfer.Bitmap;
        imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
        w = bitmap.getWidth();
        h = bitmap.getHeight();

        btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnProcess = findViewById(R.id.btnProcess);
        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                process();
            }
        });

    }

    private void process() {
        byte[] array = ImageAsMatrix.getArrayFromBitmap(bitmap);
        toGrayscale(array);
        imageView.setImageBitmap(ImageAsMatrix.getBitmapFromArray(array, w, h));
    }

    public static void toGrayscale(byte[] array)
    {
        int pixels = array.length / 4;
        int pos = 0;
        for (int i = 0; i < pixels; i++) {

            int r = array[pos] & 0xff;
            int g = array[pos + 1] & 0xff;
            int b = array[pos + 2] & 0xff;
            int y = (int)(0.3 * r + 0.59 * g + 0.11 * b); // Jasova rovnice
            array[pos] = (byte)y;
            array[pos + 1] = (byte)y;
            array[pos + 2 ] = (byte)y;
            pos += 4;
        }
    }
}