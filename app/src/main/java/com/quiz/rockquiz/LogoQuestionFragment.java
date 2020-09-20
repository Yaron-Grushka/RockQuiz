package com.quiz.rockquiz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LogoQuestionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogoQuestionFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String QUESTION_PARAM = "QUESTION";

    // TODO: Rename and change types of parameters
    private LogoQuestion mQuestion;

    private CardView[] answerFrames;
    private CardView correctAnswerView;

    private TextView clockView;

    private GameCommunication communicator;
    IntentFilter clockFilter;

    public LogoQuestionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param question: The question to display on screen
     * @return A new instance of fragment RegularQuestionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogoQuestionFragment newInstance(LogoQuestion question) {
        LogoQuestionFragment fragment = new LogoQuestionFragment();
        Bundle args = new Bundle();
        args.putParcelable(QUESTION_PARAM, question);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mQuestion = getArguments().getParcelable(QUESTION_PARAM);
        }
        clockFilter = new IntentFilter("com.quiz.rockquiz.CLOCK_TICK");
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(clockReceiver, clockFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(clockReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_logo_question, container, false);
        communicator = (GameCommunication) container.getContext();
        clockView = view.findViewById(R.id.logo_fragment_clock);

        // Display question:
        TextView title = view.findViewById(R.id.logo_question_title);
        title.setText(mQuestion.getQuestion());

        // Display image:
        ImageView logo = view.findViewById(R.id.logo);
        String logoURL = mQuestion.getLogoURL();
        if (logoURL != null) {
            Glide.with(this).load(logoURL).into(logo);
        }
        android.view.ViewGroup.LayoutParams layoutParams = logo.getLayoutParams();
        float density = getResources().getDisplayMetrics().density;
        layoutParams.width = Math.round((float) 100 * density);
        layoutParams.height = Math.round((float) 100 * density);
        logo.setLayoutParams(layoutParams);

        // Display answers:
        TextView[] answerTexts = new TextView[]
                {view.findViewById(R.id.logo_answer1), view.findViewById(R.id.logo_answer2),
                        view.findViewById(R.id.logo_answer3), view.findViewById(R.id.logo_answer4)};
        answerFrames = new CardView[]
                {view.findViewById(R.id.logo_answer1_frame), view.findViewById(R.id.logo_answer2_frame),
                        view.findViewById(R.id.logo_answer3_frame), view.findViewById(R.id.logo_answer4_frame)};

        String[] shuffledAnswers = shuffleAnswers(mQuestion.getCorrectAnswer(), mQuestion.getWrongAnswers());

        for (int i = 0; i < 4; i++) {
            answerTexts[i].setText(shuffledAnswers[i]);
            answerFrames[i].setOnClickListener(this);
        }

        return view;
    }

    /**
     * Shuffle the answers to be displayed on screen.
     * @param correctAnswer: The correct answer (must be included).
     * @param wrongAnswers: List of wrong answers, 3 of which will be displayed.
     * @return shuffled array of size 4, has the correct answer and 3 wrong answers in some order.
     */
    private String[] shuffleAnswers(String correctAnswer, ArrayList<String> wrongAnswers) {
        String[] shuffledAnswers = new String[4];
        Collections.shuffle(wrongAnswers);
        int index = (int) (Math.random() * 4);
        wrongAnswers.add(index, correctAnswer);
        for (int i = 0; i < 4; i++) {
            shuffledAnswers[i] = wrongAnswers.get(i);
            if (i == index) {
                correctAnswerView = answerFrames[index];
            }
        }
        return shuffledAnswers;
    }

    @Override
    public void onClick(View v) {
        CardView cardView = (CardView) v;

        // Deactivate buttons:
        for (CardView c : answerFrames) {
            c.setClickable(false);
        }

        if (cardView == correctAnswerView) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.correctAnswerGreen));
        } else {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.wrongAnswerRed));
            communicator.addStrike();
        }

        // Wait 600 millis then continue:
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                communicator.nextQuestion();
            }
        }, 600);
    }


    private final BroadcastReceiver clockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equalsIgnoreCase("com.quiz.rockquiz.CLOCK_TICK")) {
                    String time = intent.getStringExtra("com.quiz.rockquiz.TIME");
                    clockView.setText(time);
                    assert time != null;
                    if (Integer.parseInt(time) <= 5) {
                        clockView.setTextColor(getResources().getColor(R.color.wrongAnswerRed));
                    }
                } else if (intent.getAction().equalsIgnoreCase("com.quiz.rockquiz.TIME_OUT")) {
                    // TBA
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };
}