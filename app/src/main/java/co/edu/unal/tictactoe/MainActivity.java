package co.edu.unal.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Represents the internal state of the game
    private TicTacToeGame mGame;
    // Indicates if the current game is over
    private boolean mGameOver;
    // Various text displayed
    private TextView mInfoTextView;
    // Game history counters and texts
    private int mHumanWins;
    private int mAndroidWins;
    private int mTies;
    private TextView mHumanWinsTextView;
    private TextView mAndroidWinsTextView;
    private TextView mTiesTextView;

    private BoardView mBoardView;

    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;

    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;

    boolean isHumanTurn;

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfoTextView = (TextView) findViewById(R.id.information);
        mHumanWins = 0;
        mAndroidWins = 0;
        mTies = 0;
        isHumanTurn = true;
        mHumanWinsTextView = (TextView) findViewById(R.id.human_wins);
        mAndroidWinsTextView = (TextView) findViewById(R.id.android_wins);
        mTiesTextView = (TextView) findViewById(R.id.ties);
        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);
        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);
        if (savedInstanceState == null) {
            startNewGame();
        }
        else {
            // Restore the game's state
            mGame.setBoardState(savedInstanceState.getCharArray("board"));
            mGameOver = savedInstanceState.getBoolean("mGameOver");
            mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
            isHumanTurn = savedInstanceState.getBoolean("isHumanTurn");
        }
        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
        // Restore the scores
        mHumanWins = mPrefs.getInt("mHumanWins", 0);
        mAndroidWins = mPrefs.getInt("mAndroidWins", 0);
        mTies = mPrefs.getInt("mTies", 0);
        int selectedDifficulty = mTies = mPrefs.getInt("mDifficulty", 2);
        switch(selectedDifficulty) {
            case 0:
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                break;
            case 1:
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                break;
            default:
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
                break;
        }
        displayScores();
    }

    private void displayScores() {
        mHumanWinsTextView.setText("Human: " + mHumanWins);
        mAndroidWinsTextView.setText("Android: " + mAndroidWins);
        mTiesTextView.setText("Ties: " + mTies);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.ai_difficulty:
                showDialog(DIALOG_DIFFICULTY_ID);
                return true;
            case R.id.reset_scores:
                mHumanWins = 0;
                mAndroidWins = 0;
                mTies = 0;
                displayScores();
                return true;
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(id) {
            case DIALOG_DIFFICULTY_ID:
                builder.setTitle(R.string.difficulty_choose);
                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert)};
                // selected is the radio button that should be selected.
                int selected;
                switch(mGame.getDifficultyLevel()) {
                    case Easy:
                        selected = 0;
                        break;
                    case Harder:
                        selected = 1;
                        break;
                    default:
                        selected = 2;
                        break;
                }
                builder.setSingleChoiceItems(levels, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss(); // Close dialog
                                switch(item) {
                                    case 0:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                                        break;
                                    case 1:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                                        break;
                                    default:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
                                        break;
                                }
                                // Display the selected difficulty level
                                Toast.makeText(getApplicationContext(), levels[item],
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog = builder.create();
                break;
            case DIALOG_QUIT_ID:
                // Create the quit confirmation dialog
                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();
                break;
        }
        return dialog;
    }

    // Set up the game board.
    private void startNewGame() {
        mGame.clearBoard();
        mGameOver = false;

        mBoardView.invalidate(); // Redraw the board

        // Human goes first
        mInfoTextView.setText(R.string.first_human);
    } // End of startNewGame

    private boolean setMove(char player, int location) {
        if (mGame.setMove(player, location)) {
            mBoardView.invalidate(); // Redraw the board
            // Play the sound effect
            if(player == TicTacToeGame.HUMAN_PLAYER) mHumanMediaPlayer.start();
            else mComputerMediaPlayer.start();
            return true;
        }
        return false;
    }

    private void checkForWinnerAndUpdatePrompts(){
        int winner = mGame.checkForWinner();
        if (winner == 0)
            mInfoTextView.setText(R.string.turn_human);
        else if (winner == 1) {
            mInfoTextView.setText(R.string.result_tie);
            mTies++;
            mTiesTextView.setText("Ties: " + mTies);
            mGameOver = true;
        } else if (winner == 2) {
            mInfoTextView.setText(R.string.result_human_wins);
            mHumanWins++;
            mHumanWinsTextView.setText("Human: " + mHumanWins);
            mGameOver = true;
        } else {
            mInfoTextView.setText(R.string.result_computer_wins);
            mAndroidWins++;
            mAndroidWinsTextView.setText("Android: " + mAndroidWins);
            mGameOver = true;
        }
    }

    // Listen for touches on the board
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            // Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int location = row * 3 + col;
            if (!mGameOver && isHumanTurn && setMove(TicTacToeGame.HUMAN_PLAYER, location)) {
                setMove(TicTacToeGame.HUMAN_PLAYER, location);
                // If no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();
                if(winner != 0) checkForWinnerAndUpdatePrompts();
                else {
                    mInfoTextView.setText(R.string.turn_computer);
                    isHumanTurn = false;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            int move = mGame.getComputerMove();
                            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                            checkForWinnerAndUpdatePrompts();
                            isHumanTurn = true;
                        }
                    }, 1000);
                }
            }
            // So we aren't notified of continued events when finger is moved
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.deslizar);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pop);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mGameOver", mGameOver);
        outState.putCharSequence("info", mInfoTextView.getText());
        outState.putBoolean("isHumanTurn", isHumanTurn);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Save the current scores
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", mHumanWins);
        ed.putInt("mAndroidWins", mAndroidWins);
        ed.putInt("mTies", mTies);
        int selectedDifficulty;
        switch(mGame.getDifficultyLevel()) {
            case Easy:
                selectedDifficulty = 0;
                break;
            case Harder:
                selectedDifficulty = 1;
                break;
            default:
                selectedDifficulty = 2;
                break;
        }
        ed.putInt("mDifficulty", selectedDifficulty);
        ed.commit();
    }
}
