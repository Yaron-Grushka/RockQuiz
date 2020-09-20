package com.quiz.rockquiz;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Game implements Runnable {
    private final String TAG_GAME = "rockquiz.GAME";

    // Question type codes:
    final static int REGULAR_CODE = 101;
    final static int LOGO_CODE = 102;
    final static int LYRIC_CODE = 103;

    // Firebase variables:
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    StorageReference storage = FirebaseStorage.getInstance().getReference();

    // General game variables:
    private int level;
    private int correctAnswers;
    private int strikes;
    private int totalScore;
    private int levelScore;
    private GameClock clock;
    private long elapsedTime;

    // Question variables:
    private Question currentQuestion;
    private int questionIndex;
    private int[] orderOfQuestions; // The order in which the questions will be displayed in the current level
    private final int[] totalNumOfQuestions = new int[1]; // Total number of questions that exist for current level

    private GameCommunication communicator;
    private Context context;

    public Game (Context context) {
        this.context = context;
        this.communicator = (GameCommunication) context;
        level = 1;
        strikes = 0;
        totalScore = 0;
        questionIndex = 0;
        elapsedTime = 0;
    }

    public void addCorrectAnswer() {
        correctAnswers++;
    }

    public int getStrikes() {
        return strikes;
    }

    public void addStrike() {
        strikes++;
        if (strikes == 3) {
            communicator.loseGame(totalScore, strikes, "strikes");
        }
    }

    public void increaseTotalScore(int addition) {
        totalScore += addition;
    }

    public int getLevelScore() {
        return levelScore;
    }

    public void increaseLevelScore(int addition) {
        this.levelScore += addition;
    }

    public int getLevel() {
        return level;
    }

    public void levelUp() {
        level++;
        if (level == 4) {
            communicator.winGame(totalScore, strikes);
            return;
        }
        prepareLevel();
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public void prepareLevel() {
        questionIndex = 0;
        levelScore = 0;
        correctAnswers = 0;
        elapsedTime = 0;
        db.child("questions").child("level" + level).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /* Create array with numbers from 1 to the amount of questions in this level
                that exist in the DB and shuffle it: */
                totalNumOfQuestions[0] = (int) dataSnapshot.getChildrenCount();
                orderOfQuestions = new int[totalNumOfQuestions[0]];
                for (int i = 0; i < orderOfQuestions.length; i++) {
                    orderOfQuestions[i] = i + 1;
                }
                shuffleArray(orderOfQuestions);
                getQuestionFromDB();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG_GAME, databaseError.getMessage());
            }
        });
    }

    public void getQuestionFromDB() {
        // If level is complete:
        if (questionIndex > 6) {
            clock.cancel();
            communicator.goToLevelSummary(level, totalScore, correctAnswers, strikes);
            return;
        }

        int currentQuestion = orderOfQuestions[questionIndex];
        db.child("questions").child("level" + level).child("question" + currentQuestion).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                int code = dataSnapshot.child("code").getValue(Integer.class);
                switch (code) {
                    case REGULAR_CODE: {
                        Question question = dataSnapshot.getValue(Question.class);
                        communicator.goToQuestionFragment(question);
                        break;
                    }
                    case LOGO_CODE: {
                        String imageName = dataSnapshot.child("imageName").getValue(String.class);

                        // Get image from storage and create the question:
                        storage.child("logosForQuestions/" + imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                LogoQuestion question = dataSnapshot.getValue(LogoQuestion.class);
                                question.setQuestion("The following logo belongs to:");
                                question.setLogoURL(uri.toString());
                                communicator.goToQuestionFragment(question);
                            }
                        });
                        break;
                    }
                    case LYRIC_CODE: {
                        LyricQuestion question = dataSnapshot.getValue(LyricQuestion.class);
                        question.setQuestion("Finish the lyric:");
                        communicator.goToQuestionFragment(question);
                        break;
                    }
                }

                // If it's a new level:
                if (questionIndex == 0) {
                    startClock(60000);
                }
                questionIndex++;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG_GAME, databaseError.getMessage());
            }
        });
    }

    private static void shuffleArray(int[] array) {
        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            if (index != i) {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
    }

    public long getRemainingTime() {
        return clock.getRemainingTime();
    }

    public long getElapsedTime() {
        elapsedTime += clock.getElapsedTime();
        return elapsedTime;
    }

    public void startClock(long millis) {
        clock = new GameClock(millis, 500);
        clock.start();
    }

    public void addTime(long millis) {
        long remaining = clock.getRemainingTime() * 1000;
        clock.cancel();
        startClock(remaining + millis);
    }

    public void killClock() {
        clock.cancel();
        clock = null;
    }

    @Override
    public void run() {
        prepareLevel();
    }

    public class GameClock extends CountDownTimer {

        private long secondsUntilFinished;
        private long elapsedTime;
        private long countDownInterval;

        public GameClock(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            this.secondsUntilFinished = millisInFuture;
            this.countDownInterval = countDownInterval;
            elapsedTime = 0;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            elapsedTime += countDownInterval;
            secondsUntilFinished = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
            String formattedTime = "" + secondsUntilFinished;
            if (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) < 10) {
                formattedTime = "0" + secondsUntilFinished;
            }

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("com.quiz.rockquiz.CLOCK_TICK");
            broadcastIntent.putExtra("com.quiz.rockquiz.TIME", formattedTime);
            context.sendBroadcast(broadcastIntent);
        }

        @Override
        public void onFinish() {
            communicator.loseGame(totalScore, strikes, "time");
        }

        public long getRemainingTime() {
            return secondsUntilFinished;
        }

        public long getElapsedTime() {
            return TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
        }
    }
}
