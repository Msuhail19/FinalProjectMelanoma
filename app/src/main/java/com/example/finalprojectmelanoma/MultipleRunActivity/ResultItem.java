package com.example.finalprojectmelanoma.MultipleRunActivity;

public class ResultItem {
    private String imagePath = "";
    float[][] probability;
    String mal_prob;
    String ben_prob;

    // Constructor with imagepath string
    public ResultItem(String path, float[][] probability){
        this.imagePath = path;
        this.probability = probability;
    }

    // get probability
    public float[][] getProbability(){
        return probability;
    }

    // get image path
    public String getImagePath(){
        return imagePath;
    }
}
