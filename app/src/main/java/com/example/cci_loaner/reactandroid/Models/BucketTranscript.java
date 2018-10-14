package com.example.cci_loaner.reactandroid.Models;

import java.util.ArrayList;
/**
 * Created by cci-loaner on 3/1/18.
 */

public class BucketTranscript {
    ArrayList<String> keyPhrases;

    public BucketTranscript() {
        keyPhrases = new ArrayList<>();
    }

    public void addKeyPhrase(String buck){
        keyPhrases.add(buck);
    }

    public ArrayList<String> getKeyPhrases() {
        return keyPhrases;
    }

    public void setKeyPhrases(ArrayList<String> keyPhrases) {
        this.keyPhrases = keyPhrases;
    }
}
