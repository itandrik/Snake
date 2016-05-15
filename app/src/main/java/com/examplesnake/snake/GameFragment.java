package com.examplesnake.snake;

import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

/**
 * This fragment is main part of program. It contains main functions, which
 * generate game field and properly fill it.
 * This class is like a controller, which use models(snake, stopwatch ...) and
 * outputs result into the screen
 */
public class GameFragment extends Fragment implements View.OnTouchListener,
        View.OnClickListener, OnBackPressedListener {
    public static final int FIELD_SIZE = 20;//Height and width of field
    public final static byte SPACE = 32;    //ANSI code of space
    public final static byte DOLLAR = 36;   //ANSI code of $
    public final static byte AT_SIGN = 64;  //ANSI code of @
    /*70(F) - fast, 83(S) - slow, 88(X) - death,
     80(P) - points, 71(G) - grow, 82(R) - reduce*/
    public final static byte[] BONUS_SET = {70, 83, 88, 80, 71, 82};
    private HashMap<Byte, String> bonusDescribe;    //HashMap, which contains code of bonus and its desctiption
    private EditText fieldTv;                 //Edit text, which output the result
    private TextView stopwatchTv;             //TextView, which output game duration
    private TextView bonusTv;                 //TextView, which output bonus description
    private TextView scoresTv;                //TextView, which output player's scores
    private Button btnPause;                  //Pause/Play button
    private byte[][] field;                   //Array, which contains field content
    private Snake snake;                      //Snake position, speed
    private Stopwatch stopwatch;              //Calculates time
    private int[] food;                       //Position of food [0] - i, [1] - j
    private int[] bonus;                      //Position of bonus [0] - i, [1] - j [2] - kind of bonus
    private Handler h;                        //Handler for game thread
    private boolean isRunning = false;        //While true { play }
    private boolean isHeadGoesBack = false;   //For correct snake ways
    private Database db;
    private String name;                      //Plaer name
    private boolean isSave = true;            //For database
    private int scores;                       //Player scores


    /**
     * Saving all parameters of the game, which are needed.
     * Using for changing screen rotation
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isRunning = false;
        stopwatch.pause();
        h.removeCallbacks(updateArray);

        outState.putString("name", name);
        outState.putBoolean("isRunning", isRunning);
        outState.putLong("updatedtime", stopwatch.getTime());
        outState.putLong("timeswapbuf", stopwatch.getTimeSwapBuff());
        outState.putLong("starttime", stopwatch.getStartTime());
        outState.putLong("foodtime", stopwatch.getFoodTime());
        outState.putLong("bonustime", stopwatch.getBonusTime());
        outState.putString("textTime", stopwatchTv.getText().toString());
        outState.putIntArray("food", food);
        outState.putIntArray("bonus", bonus);
        outState.putInt("scores", scores);
        outState.putInt("speed", snake.getSpeed());

        /*Save field in the database*/
        if (isSave) {
            db.updatePlayer(name, scores, snake.getSpeed(),
                    stopwatch.getTime(), stopwatch.getStartTime(), stopwatch.getTimeSwapBuff());
            db.updatePositions(snake);
        }
        //Update screen
        fieldTv.setText(fillString(field));
    }

    /**
     * Get information about field, food, time, snake after screen
     * rotation and closing the program.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            isRunning = savedInstanceState.getBoolean("isRunning");
            name = savedInstanceState.getString("name");
            stopwatch.setTime(savedInstanceState.getLong("updatedtime"));
            stopwatch.setStartTime(savedInstanceState.getLong("starttime"));
            stopwatch.setTimeSwapBuff(savedInstanceState.getLong("timeswapbuf"));
            stopwatch.setFoodTime(savedInstanceState.getLong("foodtime"));
            stopwatch.setBonusTime(savedInstanceState.getLong("bonustime"));
            stopwatchTv.setText(savedInstanceState.getString("textTime"));
            scores = savedInstanceState.getInt("scores");
            scoresTv.setText(String.format("%d", scores));
            food = savedInstanceState.getIntArray("food");
            bonus = savedInstanceState.getIntArray("bonus");
            snake.setSpeed(savedInstanceState.getInt("speed"));
        }
        /*If last activity puts isContinue to true, that we must load field parameters from database*/
        if (getActivity().getIntent().getBooleanExtra("isContinue", false)) {

            //Reading player parameters(name, speed, time)
            Cursor playerParameters = db.readPlayer();
            if (playerParameters.moveToNext()) {
                name = playerParameters.
                        getString(playerParameters.getColumnIndex(Database.PLAYER_NAME_FIELD));
                snake.setSpeed(playerParameters
                        .getInt(playerParameters.getColumnIndex(Database.SPEED_FIELD)));
                scores = playerParameters
                        .getInt(playerParameters.getColumnIndex(Database.SCORES_FIELD));
                stopwatch.setTime(playerParameters
                        .getLong(playerParameters.getColumnIndex(Database.UPDATED_TIME_FIELD)));
                stopwatch.setStartTime(playerParameters
                        .getLong(playerParameters.getColumnIndex(Database.START_TIME_FIELD)));
                stopwatch.setTimeSwapBuff(playerParameters
                        .getLong(playerParameters.getColumnIndex(Database.SWAPBUF_TIME_FIELD)));
                int secs = (int) (stopwatch.getTime() / 1000);
                long mins = secs / 60;
                secs = secs % 60;
                long milliseconds = (int) (stopwatch.getTime() % 1000);
                //Updating the stopwatch TextView
                stopwatchTv.setText(String.format("%02d:%02d:%03d", mins, secs, milliseconds));
                playerParameters.close();
            }
            scoresTv.setText(String.format("%d", scores));

            //Reading snake parameters(way, i, j) of every segment of snake
            Cursor cursorPosition = db.readSnake();
            if (cursorPosition.moveToNext()) {
                for (int[] i : snake.getSnake()) {
                    field[i[1]][i[2]] = SPACE;
                }
                snake.getSnake().clear();
                do {

                    snake.getSnake().add(new int[]{
                            cursorPosition.getInt(cursorPosition.getColumnIndex(Database.WAY_FIELD)),
                            cursorPosition.getInt(cursorPosition.getColumnIndex(Database.I_FIELD)),
                            cursorPosition.getInt(cursorPosition.getColumnIndex(Database.J_FIELD))
                    });
                } while (cursorPosition.moveToNext());
                cursorPosition.close();
            }
            //Update snake on the field
            for (int[] i : snake.getSnake()) {
                field[i[1]][i[2]] = GameFragment.AT_SIGN;
            }
        }
        // Condition was maden for that correct show food after screen rotation
        if (food[0] == 0 && food[1] == 0)
            updateFood();
        //Updating the field on the screen
        fieldTv.setText((fillString(field)));
    }

    /**
     * Updating the field on the screen
     * isSave must be false only when player lose
     */
    @Override
    public void onResume() {
        super.onResume();
        fieldTv.setText(fillString(field));
        isSave = true;
    }

    /**
     * Initializations of all fields
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        field = new byte[FIELD_SIZE][FIELD_SIZE];//
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                field[i][j] = SPACE;
            }
        }
        name = getActivity().getIntent().getStringExtra("name"); // correct name after screen rotation
        snake = new Snake(field, getActivity());
        food = new int[2];
        scores = 0;
        bonus = new int[3];
        bonusDescribe = new HashMap<>();
        bonusDescribe.put((byte) 70, "Speed increased");
        bonusDescribe.put((byte) 83, "Deceleration");
        bonusDescribe.put((byte) 88, "Death");
        bonusDescribe.put((byte) 80, "+100 points");
        bonusDescribe.put((byte) 71, "Length increased");
        bonusDescribe.put((byte) 82, "Length reduced");
        db = new Database(getActivity());
        db.open();
    }

    /**
     * Saving all parameters in the database and dont save if player lose
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isSave) {
            db.updatePlayer(name, scores, snake.getSpeed(),
                    stopwatch.getTime(), stopwatch.getStartTime(), stopwatch.getTimeSwapBuff());
            db.updatePositions(snake);
        }
        db.close();
    }

    /**
     * Initializations of all views on the screen in this fragment.
     * Setting the text appearance.
     * Setting the field size depending on screen parameters
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        btnPause = (Button) v.findViewById(R.id.pauseBtn);
        btnPause.setText("\u25B7");
        btnPause.setOnClickListener(this);

        stopwatchTv = (TextView) v.findViewById(R.id.watcherTv);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/TypoCollege.otf");
        stopwatchTv.setTypeface(typeface);
        stopwatchTv.setText(R.string.start_time_tv);
        stopwatch = new Stopwatch(stopwatchTv);

        bonusTv = (TextView) v.findViewById(R.id.bonusTv);
        bonusTv.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/orange_juice.ttf"));

        scoresTv = (TextView) v.findViewById(R.id.scoresTv);
        scoresTv.setTypeface(typeface);
        scoresTv.setText(R.string.start_score_tv);

        fieldTv = (EditText) v.findViewById(R.id.fieldTv);
        fieldTv.setTypeface(Typeface.MONOSPACE);
        fieldTv.setCursorVisible(false);
        fieldTv.setOnTouchListener(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if (getActivity().getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            fieldTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, metrics.heightPixels / FIELD_SIZE);
            fieldTv.setLineSpacing(0,
                    ((fieldTv.getTextSize() * (FIELD_SIZE - 4)) / metrics.heightPixels)
            );
        } else {
            fieldTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, metrics.widthPixels / FIELD_SIZE);
        }

        //Updating the field on the screen
        fieldTv.setText(fillString(field));
        h = new Handler();
        return v;
    }

    /**
     * Handle clicking on the start/pause button
     *
     * @param v - view which will be handled
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.pauseBtn) {
            if (!isRunning) {
                isRunning = true;
                stopwatch.start();  //Start game time
                h.postDelayed(updateArray, snake.getSpeed()); // Start runnable function
                btnPause.setText("| |");// Changes the button text
            } else {
                isRunning = false;
                stopwatch.pause();
                h.removeCallbacks(updateArray);
                btnPause.setText("\u25B7");
            }
        }
    }

    /**
     * Main function in this program. It starts thread, which move the snake
     */
    Runnable updateArray = new Runnable() {
        @Override
        public void run() {
            try {
                if (isRunning) {
                    h.postDelayed(this, snake.getSpeed());
                    /*If snake head is in snake body - game over*/
                    if (snake.isCannibal()) {
                        isRunning = false;
                        h.removeCallbacks(this);
                        gameOver();
                        return;
                    }
                    isHeadGoesBack = false;

                    /* If snake head in the food - incrementing snake length*/
                    if (snake.isIntersectsWithFood(food)) {
                        snake.pushFood();
                        snake.updateSpeed();
                        updateScores();
                        updateFood();
                    } else if ((int) ((stopwatch.getTime() - stopwatch.getFoodTime()) / 1000) % 60 >= 10) {
                        updateFood(); //Update food position after 10 seconds
                    }

                    /* If snake head in the food - bonus activating*/
                    if (snake.isIntersectsWithBonus(bonus)) {
                        applyBonus();
                        updateBonus();
                    } else if ((int) ((stopwatch.getTime() - stopwatch.getBonusTime()) / 1000) % 60 >= 10) {
                        updateBonus();  //Update bonus position after 10 seconds
                    }
                    snake.update(); //Update snake position
                    fieldTv.setText(fillString(field));//Update field on the screen
                } else h.removeCallbacks(this);
            } catch (IndexOutOfBoundsException e) {
                //If snake is rammed the wall - game over
                e.printStackTrace();
                isRunning = false;
                h.removeCallbacks(this);
                gameOver();
            }
        }
    };

    /**
     * Converting byte [][] field into the string.
     * If Screen has landscape rotation we fill a little bit another
     *
     * @param field - byte array presentation of field
     * @return String, which contains the field
     */
    private String fillString(byte[][] field) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < FIELD_SIZE; i++) {
            if (getActivity().getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE) {
                result.append('|');
            }
            for (int j = 0; j < FIELD_SIZE; j++) {
                result.append((char) field[i][j]);
            }
            if (getActivity().getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE) {
                result.append('|');
            }
            if (i != FIELD_SIZE - 1) result.append('\n');
        }
        return result.toString();
    }

    /**
     * Change player scores following the formula below
     */
    private void updateScores() {
        scores += snake.getSnake().size() * 2.75;
        scoresTv.setText(String.format("%d", scores));
    }

    /**
     * Updating food position on the field
     */
    private void updateFood() {
        // Update food position with random time
        if (!(food[0] == 0 && food[1] == 0) && Math.random() > 0.6) return;

        // Clear food position and clear on the screen
        field[food[0]][food[1]] = SPACE;
        food[0] = 0;
        food[1] = 0;

        // Definition correct food position(not in snake, not in bonus)
        boolean isInSnake = false;
        stopwatch.setFoodTime(stopwatch.getTime());
        while (true) {
            food[0] = (int) (Math.random() * FIELD_SIZE);
            food[1] = (int) (Math.random() * FIELD_SIZE);
            for (int[] i : snake.getSnake())
                if (food[0] == i[1] && food[1] == i[2]) {
                    isInSnake = true;
                    break;
                }
            // Set dollar in the food position on the screen
            if (!isInSnake && food[0] != bonus[0] && food[1] != bonus[1]) {
                field[food[0]][food[1]] = DOLLAR;
                return;
            } else isInSnake = false;
        }
    }

    /**
     * Updating bonus position on the field
     */
    private void updateBonus() {
        // Update bonus position with random time
        if (!(bonus[0] == 0 && bonus[1] == 0) && Math.random() > 0.3) return;

        // Clear bonus position and clear on the screen
        field[bonus[0]][bonus[1]] = SPACE;
        bonus[0] = 0;
        bonus[1] = 0;

        // Definition correct bonus position(not in snake, not in food)
        boolean isInSnake = false;
        stopwatch.setBonusTime(stopwatch.getTime()); // Its for 10 secs remaining for bonus
        bonus[2] = BONUS_SET[(int) (Math.random() * 6)];// Random kind of bonus
        while (true) {
            bonus[0] = (int) (Math.random() * FIELD_SIZE);
            bonus[1] = (int) (Math.random() * FIELD_SIZE);
            for (int[] i : snake.getSnake())
                if (bonus[0] == i[1] && bonus[1] == i[2]) {
                    isInSnake = true;
                    break;
                }
            // Set bonus symbol in the food position on the screen
            if (!isInSnake && (food[0] != bonus[0] && food[1] != bonus[1])) {
                field[bonus[0]][bonus[1]] = (byte) bonus[2];
                return;
            } else isInSnake = false;
        }
    }

    /**
     * Change snake conditions depending on bonus
     */
    public void applyBonus() {
        bonusTv.setText(bonusDescribe.get((byte) bonus[2]));
        switch (bonus[2]) {
            case 70:
                snake.increaseSpeed();
                break;
            case 83:
                snake.reduceSpeed();
                break;
            case 88:
                isRunning = false;  //Death - game over
                h.removeCallbacks(updateArray);
                gameOver();
                break;
            case 80:
                scores += 100;      // Add more score points
                scoresTv.setText(String.format("%d", scores));
                break;
            case 71:
                snake.pushFood();   //Snake size += 2;
                snake.pushFood();
                break;
            case 82:
                snake.popFood();    //Snake size -= 2;
                snake.popFood();
                break;
        }
        // Animation of bonus
        OnAnimationListener listener = new OnAnimationListener(); //Its for closing textView after animation
        listener.setView(bonusTv);

        Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.scores_anim);
        anim.setAnimationListener(listener);

        bonusTv.startAnimation(anim);
    }

    /**
     * Handle correct snake ways depending on touch position
     * I divide screen into 4 parts by 2 diagonal lines. These parts has triangle form.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int way = 0;
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (x > v.getLeft() && x < v.getPivotX() && x < y && x < v.getBottom() - y)
                way = 1;//left
            else if (x < v.getRight() &&
                    x > v.getPivotX() &&
                    v.getRight() - x < y &&
                    v.getRight() - x < v.getBottom() - y) {
                way = 2;//right
            } else if (y > v.getTop() && y < v.getPivotY())
                way = 0;//up

            else if (y < v.getBottom() && y > v.getPivotY())
                way = 3;//down

            if (!isHeadGoesBack && !(way == 0 && snake.getWay() == 3) &&
                    !(way == 1 && snake.getWay() == 2) &&
                    !(way == 3 && snake.getWay() == 0) &&
                    !(way == 2 && snake.getWay() == 1)) {

                snake.setWay(way);
                isHeadGoesBack = true;
            }
        }
        return true;
    }

    /**
     * Starting GameOverDialog, which gave possibility to try again or go to
     * main menu. Also it shows player scores, name and time
     */
    public void gameOver() {
        GameOverDialog gameOver = new GameOverDialog();
        stopwatch.pause(); //pause the game time
        isSave = false;    //dont save positions into the database(not scores)
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putInt("scores", scores);
        bundle.putString("time", stopwatchTv.getText().toString());
        gameOver.setArguments(bundle);

        // If player got less points than in the database, that dont update it
        Cursor playerCursor = db.readPlayer(name);
        if (playerCursor != null) {
            if (playerCursor.moveToNext()) {
                int lastScore = playerCursor.getInt(playerCursor.getColumnIndex(Database.SCORES_FIELD));
                if (scores < lastScore) scores = lastScore;
            }
            playerCursor.close();
        }
        db.updatePlayer(name, scores, snake.getSpeed(),
                stopwatch.getTime(), stopwatch.getStartTime(), stopwatch.getTimeSwapBuff());
        db.clearPosition();
        // Show GameOverDialog
        gameOver.show(getFragmentManager(), "gameOverDialog");
        reset();
    }


    @Override
    public void onBackPressed() {
        getActivity().finish();
        /*I don't know what is better : to go to main menu when back is pressed
        or pause the game*/

        /*isRunning = false;
        stopwatch.pause();
        h.removeCallbacks(updateArray);
        btnPause.setText("\u25B7");*/
    }

    /**
     * Reset game characteristics(class fields)
     */
    public void reset() {
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                field[i][j] = SPACE;
            }
        }
        scores = 0;
        scoresTv.setText(getResources().getString(R.string.start_score_tv));
        stopwatch.reset();
        stopwatchTv.setText(getResources().getString(R.string.start_time_tv));
        food[0] = 0;
        food[1] = 0;
        bonus[0] = 0;
        bonus[1] = 0;
        fieldTv.setText(fillString(field));
        snake.reset(getActivity());
        fieldTv.setText(fillString(field));
        btnPause.setText("\u25B7");
    }
}

