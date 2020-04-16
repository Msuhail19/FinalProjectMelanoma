package com.example.finalprojectmelanoma.MultipleRunActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class InterpreterHandler {
    private Interpreter tflite;
    private Context mContext;
    private List<ImageItem> mData;
    private final String TAG = "MODEL LOG";

    private final int HEIGHT = 299, WIDTH = 299;
    private final int CHANNELS = 3;
    private float getStd = 128.f;
    private float getMean = 128.f;
    private int inputSize = HEIGHT*WIDTH*CHANNELS*4;

    /*
    * Constructor
    **/
    public InterpreterHandler(Activity name, String filePath){
        try {
            tflite = new Interpreter(loadModelFile(name, filePath));
            Log.v(TAG," Model Loaded ! ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * Run and return result
    * */
    public float[][] run(ByteBuffer image){
        float[][] result = new float[1][2];
        try {
            tflite.run(image, result);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /* Run with bitmap and return result */
    public float[][] run(Bitmap img){
        float[][] result = new float[1][2];
        ByteBuffer imageByte;
        try {
            imageByte = bitmapToModelsMatchingByteBuffer(img);
            tflite.run(imageByte, result);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /* Load our model file */
    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /* Transforms bitmap to bytebuffer to be used by interpreter
    * Also rescales image to dimensions specified */
    private ByteBuffer bitmapToModelsMatchingByteBuffer(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, 299, 299, false);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(inputSize);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[WIDTH * HEIGHT];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < WIDTH; ++i) {
            for (int j = 0; j < HEIGHT; ++j) {
                int pixelVal = intValues[pixel++];
                for (float channelVal : pixelToChannelValues(pixelVal)) {
                    byteBuffer.putFloat(channelVal);
                }
            }
        }
        return byteBuffer;
    }

    /* For use by bitmap to bytebuffer method */
    private float[] pixelToChannelValues(int pixel) {
        float[] rgbVals = new float[3];
        rgbVals[0] = ((((pixel >> 16) & 0xFF) - getMean) / getStd);
        rgbVals[1] = ((((pixel >> 8) & 0xFF) - getMean) / getStd);
        rgbVals[2] = ((((pixel) & 0xFF) - getMean) / getStd);
        return rgbVals;
    }



}
