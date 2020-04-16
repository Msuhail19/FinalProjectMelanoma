package com.example.finalprojectmelanoma;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/* Alter this class so has 2 constructors 1 for single image 1 for multiple images*/


public class RunDetails {
    /* Base variables */
    private String imagePath;
    private float malignantProbability;
    private float benignProbability;
    private String runDate;

    /* Variables for multiple images */
    private boolean multipleImages = false;
    private ArrayList<float[][]> probabilities = new ArrayList<>();
    private ArrayList<String> imagePaths = new ArrayList<>();

    public RunDetails(String paths, float mal_prob, float ben_prob, String date){
        this.imagePath = paths;
        this.malignantProbability = mal_prob;
        this.benignProbability = ben_prob;
        this.runDate = date;
    }

    public RunDetails(ArrayList<String> paths, ArrayList<float[][]> probabilities, String date){
        this.multipleImages = true;
        this.runDate = date;
        this.imagePaths = paths;
        this.probabilities = probabilities;
    }

    public String getDate(){
        return runDate;
    }

    public boolean isMultipleImages(){
        return multipleImages;
    }


    public float[] getProbability(){
        float[] probability = new float[2];
        probability[0] = malignantProbability;
        probability[1] = benignProbability;
        return probability;
    }

    public ArrayList<float[][]> getProbabilities(){
        return this.probabilities;
    }

    public ArrayList<String> getImagePaths(){
        return imagePaths;
    }

    public String getImagePath(){
        return imagePath;
    }
}
