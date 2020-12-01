package cz.vsb.kameratest;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Random;

public class    ImageAsMatrix {

    // 4 bajty: R G B A
    // 4 bajty: 0 1 2 3
    // byte => -128 až 127
    // černá -> šedá -> šedá -> bílá
    //     0 ->  127 -> -128 -> -1
    // Převod Bitmap -> byteArray
    public static byte[] getArrayFromBitmap(Bitmap bm) {
        int h = bm.getHeight();
        int  bmpBytesPerRow = bm.getRowBytes();
        int size = bmpBytesPerRow * h;
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bm.copyPixelsToBuffer(byteBuffer);
        return byteBuffer.array();
    }

    public static Bitmap getBitmapFromArray(byte[] array, int w, int h) {
        // Převod byteArray -> bitmap
        //Bitmap.Config configBmp = Bitmap.Config.valueOf(bm.getConfig().name());
        Bitmap bm = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_4444);
        ByteBuffer buffer = ByteBuffer.wrap(array);
        bm.copyPixelsFromBuffer(buffer);
        return  bm;
    }

    public static int GetCountOfWhiteFromBitmap(Bitmap bm, int jas, int countPX)
    {
        byte array[] = getArrayFromBitmap(bm);
        int brightCount = 0;
        int pixels = array.length / 4;
        int pos = 0;
        for (int i = 0; i < pixels; i++) {

            int r = array[pos] & 0xff;
            int g = array[pos + 1] & 0xff;
            int b = array[pos + 2] & 0xff;
            int y = (int)(0.3 * r + 0.59 * g + 0.11 * b); // Jasova rovnice

            if(y>=jas) brightCount ++;

            //array[pos] = (byte)y;
            //array[pos + 1] = (byte)y;
            //array[pos + 2 ] = (byte)y;
            pos += 4;
        }

        if(brightCount>=countPX)
            return (1);
        else return(0);
    }

}