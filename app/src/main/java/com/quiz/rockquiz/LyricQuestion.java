package com.quiz.rockquiz;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class LyricQuestion extends Question {
    private String quote;
    private String artist;

    public LyricQuestion() {
        // Empty Constructor
    }

    public LyricQuestion(String question, String correctAnswer, ArrayList<String> wrongAnswers, String quote, String artist) {
        super(question, correctAnswer, wrongAnswers);
        this.quote = quote;
        this.artist = artist;
    }

    public String getQuote() {
        return quote;
    }

    public String getArtist() {
        return artist;
    }

    public static final Parcelable.Creator<LyricQuestion> CREATOR = new Parcelable.Creator<LyricQuestion>() {

        @Override
        public LyricQuestion createFromParcel(Parcel source) {
            return new LyricQuestion(source);
        }

        @Override
        public LyricQuestion[] newArray(int size) {
            return new LyricQuestion[size];
        }
    };

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(quote);
        out.writeString(artist);
    }

    private LyricQuestion(Parcel in) {
        super(in);
        quote = in.readString();
        artist = in.readString();
    }
}
