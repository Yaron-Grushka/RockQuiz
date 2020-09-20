package com.quiz.rockquiz;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends BottomNavigationBarManager implements NavigationHost, GameCommunication {

    private final String TAG_MAINACTIVITY = "MAINACTIVITY";

    // Firebase variables:
    private FirebaseAuth mAuth;
    DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("users");

    // Game variables:
    private Game game;
    private QuestionFragment questionFragment;
    private LevelSummaryFragment levelSummaryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        Button playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game = new Game(MainActivity.this);
                game.run();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        }

        currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    finish();
                    startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                }
            }
        });

    }

    // BottomNavigationBarManager abstract class methods:

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    int getBottomNavigationMenuItemId() {
        return R.id.play;
    }

    // NavigationHost interface methods;

    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainActivityLayout, fragment);
        if (addToBackstack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    // GameCommunication interface methods:

    @Override
    public void goToQuestionFragment(Question question) {
        // In case a new level is started:
        if (levelSummaryFragment != null) {
            getSupportFragmentManager().popBackStack();
            levelSummaryFragment = null;
        }
        if (questionFragment == null) {
            questionFragment = QuestionFragment.newInstance(question);
            navigateTo(questionFragment, true);
        } else {
            questionFragment.setQuestion(question);
        }
    }

    @Override
    public void nextQuestion() {
        if (game != null) {
            game.getQuestionFromDB();
        }
    }

    @Override
    public int upScore() {
        int addition = (game.getLevel() + game.getQuestionIndex()) * (60 - (int) game.getElapsedTime());
        game.increaseLevelScore(addition);
        game.increaseTotalScore(addition);
        game.addCorrectAnswer();
        return game.getLevelScore();
    }

    @Override
    public void addTime(long millis) {
        game.addTime(millis);
    }

    @Override
    public void addStrike() {
        game.addStrike();
    }

    @Override
    public void goToLevelSummary(int level, int score, int correctAnswers, int strikes) {
        getSupportFragmentManager().popBackStack();
        questionFragment = null;
        levelSummaryFragment = LevelSummaryFragment.newInstance(level, score, correctAnswers, strikes);
        navigateTo(levelSummaryFragment, true);
    }

    @Override
    public void levelUp() {
        game.levelUp();
    }

    @Override
    public void exitGame() {
        getSupportFragmentManager().popBackStack();
        questionFragment = null;
        levelSummaryFragment = null;
        game.killClock();
        game = null;
    }

    @Override
    public void loseGame(int score, int strikes, String reason) {
        exitGame();
        EndGameFragment endGameFragment = EndGameFragment.newInstance(false, score, strikes, reason);
        navigateTo(endGameFragment, true);
    }

    @Override
    public void winGame(int score, int strikes) {
        exitGame();
        EndGameFragment endGameFragment = EndGameFragment.newInstance(true, score, strikes, "");
        navigateTo(endGameFragment, true);
    }
}