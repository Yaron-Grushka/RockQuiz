package com.quiz.rockquiz;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EndGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EndGameFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String WIN_PARAM = "WIN";
    private static final String SCORE_PARAM = "SCORE";
    private static final String STRIKES_PARAM = "STRIKES";
    private static final String REASON_PARAM = "REASON";

    // TODO: Rename and change types of parameters
    private boolean mWin;
    private int mScore;
    private int mStrikes;
    private String mReason;

    private FirebaseUser user;
    private DatabaseReference db;

    private final int[] highScore = new int[1];

    public EndGameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param win Whether player won or lost
     * @param score Player's total score
     * @return A new instance of fragment EndGameFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EndGameFragment newInstance(boolean win, int score, int strikes, String reason) {
        EndGameFragment fragment = new EndGameFragment();
        Bundle args = new Bundle();
        args.putBoolean(WIN_PARAM, win);
        args.putInt(SCORE_PARAM, score);
        args.putInt(STRIKES_PARAM, strikes);
        args.putString(REASON_PARAM, reason);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWin = getArguments().getBoolean(WIN_PARAM);
            mScore = getArguments().getInt(SCORE_PARAM);
            mStrikes = getArguments().getInt(STRIKES_PARAM);
            mReason = getArguments().getString(REASON_PARAM);
        }

        db = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_end_game, container, false);

        TextView winOrLose = view.findViewById(R.id.win_or_lose);
        if (mWin) {
            winOrLose.setText("Game complete!");
        } else if (mReason.equals("time")) {
                winOrLose.setText("Time out!");
        } else if (mReason.equals("strikes")){
            winOrLose.setText("Three strikes!");
        }

        final TextView gameScore = view.findViewById(R.id.game_score);
        gameScore.setText("Score: " + mScore);
        final TextView highScoreTextView = view.findViewById(R.id.high_score);
        final ImageView banner = view.findViewById(R.id.high_score_banner);

        db.child("users").child(user.getUid()).child("highScore")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        highScore[0] = snapshot.getValue(Integer.class);
                        if (highScore[0] < mScore) {
                            gameScore.setTextColor(getResources().getColor(R.color.correctAnswerGreen));
                            highScoreTextView.setTextColor(getResources().getColor(R.color.correctAnswerGreen));
                            banner.setVisibility(View.VISIBLE);
                            updateHighScore();
                        }
                        highScoreTextView.setText("High score: " + Math.max(mScore, highScore[0]));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("", error.getMessage());
                    }
                });

        TextView strikes = view.findViewById(R.id.strikes_total);
        strikes.setText("Strikes: " + mStrikes + "/3");

        return view;
    }

    private void updateHighScore() {
        db.child("users").child(user.getUid()).child("highScore").setValue(mScore)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Error:" + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}