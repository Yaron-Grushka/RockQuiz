package com.quiz.rockquiz;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LevelSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LevelSummaryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String LEVEL_PARAM = "LEVEL";
    private static final String SCORE_PARAM = "SCORE";
    private static final String CORRECT_ANSWERS_PARAM = "CORRECT_ANSWERS";
    private static final String STRIKES_PARAM = "STRIKES";

    // TODO: Rename and change types of parameters
    private int mLevel;
    private int mScore;
    private int mCorrectAnswers;
    private int mStrikes;

    private static GameCommunication communicator;

    public LevelSummaryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param score
     * @return A new instance of fragment LevelSummaryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LevelSummaryFragment newInstance(int level, int score, int correctAnswers, int strikes) {
        LevelSummaryFragment fragment = new LevelSummaryFragment();
        Bundle args = new Bundle();
        args.putInt(LEVEL_PARAM, level);
        args.putInt(SCORE_PARAM, score);
        args.putInt(CORRECT_ANSWERS_PARAM, correctAnswers);
        args.putInt(STRIKES_PARAM, strikes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLevel = getArguments().getInt(LEVEL_PARAM);
            mScore = getArguments().getInt(SCORE_PARAM);
            mCorrectAnswers = getArguments().getInt(CORRECT_ANSWERS_PARAM);
            mStrikes = getArguments().getInt(STRIKES_PARAM);
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ExitGameDialog dialog = new ExitGameDialog(communicator);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    transaction.remove(prev);
                }
                dialog.show(transaction, "dialog");
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_level_summary, container, false);

        communicator = (GameCommunication) container.getContext();

        TextView levelComplete = (TextView) view.findViewById(R.id.level_x_complete);
        levelComplete.setText("Level " + mLevel + " complete!");

        TextView scoreTextView = (TextView) view.findViewById(R.id.score);
        scoreTextView.setText(" Total score: " + mScore);

        TextView correctAnswersTextView = (TextView) view.findViewById(R.id.correct_answers);
        correctAnswersTextView.setText("Correct answers: " + mCorrectAnswers + "/7");

        TextView wrongAnswersTextView = (TextView) view.findViewById(R.id.strikes);
        wrongAnswersTextView.setText("Strikes: " + mStrikes  + "/3");

        Button nextLevelButton = (Button) view.findViewById(R.id.next_level_button);
        nextLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                communicator.levelUp();
            }
        });

        return view;
    }
}