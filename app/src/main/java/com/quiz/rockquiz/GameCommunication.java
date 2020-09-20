package com.quiz.rockquiz;

public interface GameCommunication {
    void goToQuestionFragment(Question question);
    void nextQuestion();
    int upScore();
    void addTime(long millis);
    void addStrike();
    void goToLevelSummary(int level, int score, int correctAnswers, int strikes);
    void levelUp();
    void exitGame();
    void loseGame(int score, int strikes, String reason);
    void winGame(int score, int strikes);
}
