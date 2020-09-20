package com.quiz.rockquiz;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
 * Use the {@link QuestionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuestionFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private final String TAG_QUESTION_FRAGMENT = "QUESTION_FRAGMENT";
    private static final String QUESTION_PARAM = "QUESTION";

    // TODO: Rename and change types of parameters
    private Question mQuestion;

    // General UI components:
    private CardView[] answerFrames;
    private CardView correctAnswerView;
    private TextView[] answerTexts;
    private TextView title;
    private TextView scoreTextView;
    private TextView clockView;

    // Question-specific UI components:
    private ImageView logo;
    private CardView quoteFrame;
    private TextView quoteTextView;
    private TextView artistTextView;

    private static GameCommunication communicator;
    private IntentFilter clockFilter;

    public QuestionFragment() {
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
    public static QuestionFragment newInstance(Question question) {
        QuestionFragment fragment = new QuestionFragment();
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_question, container, false);

        // General UI components:
        communicator = (GameCommunication) container.getContext();
        clockView = view.findViewById(R.id.regular_fragment_clock);
        title = view.findViewById(R.id.regular_question_title);
        answerTexts = new TextView[]
                {view.findViewById(R.id.regular_answer1), view.findViewById(R.id.regular_answer2),
                        view.findViewById(R.id.regular_answer3), view.findViewById(R.id.regular_answer4)};
        answerFrames = new CardView[]
                {view.findViewById(R.id.regular_answer1_frame), view.findViewById(R.id.regular_answer2_frame),
                        view.findViewById(R.id.regular_answer3_frame), view.findViewById(R.id.regular_answer4_frame)};
        scoreTextView = view.findViewById(R.id.score_keeper);
        scoreTextView.setText("Score: 0");

        // Question-specific UI components:
        logo = view.findViewById(R.id.logo_question_image_view);
        quoteFrame = view.findViewById(R.id.quote_frame);
        quoteTextView = view.findViewById(R.id.quote);
        artistTextView = view.findViewById(R.id.artist);

        updateUI();

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

    public void setQuestion(Question question) {
        mQuestion = question;
        updateUI();
    }

    private void updateUI() {

        // General UI:

        title.setText(mQuestion.getQuestion());
        String[] shuffledAnswers = shuffleAnswers(mQuestion.getCorrectAnswer(), mQuestion.getWrongAnswers());

        for (int i = 0; i < 4; i++) {
            answerTexts[i].setText(shuffledAnswers[i]);
            answerFrames[i].setCardBackgroundColor(getResources().getColor(R.color.white));
            answerFrames[i].setOnClickListener(this);
        }

        // Logo question specific UI:

        if (mQuestion.getCode() == Game.LOGO_CODE) {
            logo.setVisibility(View.VISIBLE);
            String logoURL = ((LogoQuestion)mQuestion).getLogoURL();
            if (logoURL != null) {
                Glide.with(this).load(logoURL).into(logo);
            }
            android.view.ViewGroup.LayoutParams layoutParams = logo.getLayoutParams();
            float density = getResources().getDisplayMetrics().density;
            layoutParams.width = Math.round((float) 100 * density);
            layoutParams.height = Math.round((float) 100 * density);
            logo.setLayoutParams(layoutParams);
        } else {
            if (logo != null) {
                logo.setVisibility(View.GONE);
            }
        }

        // Lyric question specific UI:

        if (mQuestion.getCode() == Game.LYRIC_CODE) {
            quoteFrame.setVisibility(View.VISIBLE);
            String quote = "\"" + ((LyricQuestion)mQuestion).getQuote() + "\"";
            String artist = "-" + ((LyricQuestion)mQuestion).getArtist();
            quoteTextView.setText(quote);
            artistTextView.setText(artist);
        } else {
            if (quoteFrame != null) {
                quoteFrame.setVisibility(View.GONE);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        CardView cardView = (CardView) v;

        // Deactivate buttons:
        for (CardView c : answerFrames) {
            c.setClickable(false);
        }

        if (cardView == correctAnswerView) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.correctAnswerGreen));
            int currentScore = communicator.upScore();
            scoreTextView.setText("Score: " + currentScore);
            communicator.addTime(5000);
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
                    if (Integer.parseInt(time) <= 10) {
                        clockView.setTextColor(getResources().getColor(R.color.wrongAnswerRed));
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };
}