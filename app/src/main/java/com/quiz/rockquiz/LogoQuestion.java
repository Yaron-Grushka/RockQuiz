package com.quiz.rockquiz;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class LogoQuestion extends Question {
    private String logoURL;

    public LogoQuestion() {
        // Empty Constructor
    }

    public LogoQuestion(String question, String correctAnswer, ArrayList<String> wrongAnswers, String logoURL) {
        super(question, correctAnswer, wrongAnswers);
        this.logoURL = logoURL;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    public static final Parcelable.Creator<LogoQuestion> CREATOR = new Parcelable.Creator<LogoQuestion>() {

        @Override
        public LogoQuestion createFromParcel(Parcel source) {
            return new LogoQuestion(source);
        }

        @Override
        public LogoQuestion[] newArray(int size) {
            return new LogoQuestion[size];
        }
    };

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(logoURL);
    }

    private LogoQuestion(Parcel in) {
        super(in);
        logoURL = in.readString();
    }
}
