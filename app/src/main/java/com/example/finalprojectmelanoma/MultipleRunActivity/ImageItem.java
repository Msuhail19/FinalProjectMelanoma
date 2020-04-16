package com.example.finalprojectmelanoma.MultipleRunActivity;

public class ImageItem {
    private String imagePath = "";
    private float[][] probability = new float[1][2];

    // Constructor with imagepath string
    public ImageItem(String path){
        this.imagePath = path;
    }

    // get probability
    public float[][] getProbability(){
        return probability;
    }

    public void setProbability(float[][] results){
        this.probability = results;
    }

    // get image path
    public String getImagePath(){
        return imagePath;
    }

}
