package com.quiz.rockquiz;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Question implements Parcelable {
    private String question;
    private String correctAnswer;
    private ArrayList<String> wrongAnswers;
    private int code;

    public Question(){
        // Empty constructor
    }

    public Question(String question, String correctAnswer, ArrayList<String> wrongAnswers){
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.wrongAnswers = wrongAnswers;
    }

    // Public getters:
    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public ArrayList<String> getWrongAnswers() {
        return wrongAnswers;
    }

    public int getCode() {
        return code;
    }

    // Public setters:
    public void setQuestion(String question) {
        this.question = question;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public void setWrongAnswers(ArrayList<String> wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    // Parcelable methods:
    protected Question(Parcel in) {
        question = in.readString();
        correctAnswer = in.readString();
        wrongAnswers = in.createStringArrayList();
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeString(correctAnswer);
        dest.writeList(wrongAnswers);
    }
}
