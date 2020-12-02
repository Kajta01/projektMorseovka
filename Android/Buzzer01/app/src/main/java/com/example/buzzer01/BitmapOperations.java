package com.example.buzzer01;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

public class BitmapOperations {


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

    public static int getCountOfWhiteFromBitmap(Bitmap bm, int jas, int countPX)
    {
        byte array[] = getArrayFromBitmap(bm);
        int brightCount = 0;
        int pixels = array.length / 4;
        int pos = 0;
        for (int i = 0; i < pixels; i++) {

            int r = array[pos] & 0xff;
            int g = array[pos + 1] & 0xff;
            int b = array[pos + 2] & 0xff;
            int y = (int)(0.3 * r + 0.59 * g + 0.11 * b);

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
