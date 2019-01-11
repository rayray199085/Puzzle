package com.project.stephencao.puzzle.activity;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.project.stephencao.puzzle.R;
import com.project.stephencao.puzzle.view.PuzzleContainerLayout;

public class MainActivity extends AppCompatActivity {
    private PuzzleContainerLayout mPuzzleContainerLayout;
    private ProgressBar mProgressBar;
    private TextView mCurrentStage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mCurrentStage = findViewById(R.id.id_current_stage);
        mProgressBar = findViewById(R.id.id_progress_bar);
        mPuzzleContainerLayout = findViewById(R.id.id_puzzle);
        mPuzzleContainerLayout.setPuzzleStageListener(new PuzzleContainerLayout.PuzzleStageListener() {

            @Override
            public void updateTimer(int currentTime) {
                mProgressBar.setProgress(currentTime);
            }

            @Override
            public void setProgressBarMaxValue(int maxValue) {
                mProgressBar.setMax(maxValue);
            }

            @Override
            public void gameOver(boolean isGameOver) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false);
                builder.setMessage("Game Over!!! Do you want to try again?");
                builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPuzzleContainerLayout.toNextStage(true);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.show();
            }

            @Override
            public void setCurrentStageNotification(int currentStage) {
                mCurrentStage.setText(currentStage + "");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPuzzleContainerLayout.pauseTheGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPuzzleContainerLayout.continueTheGame();
    }
}
