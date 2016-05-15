package com.examplesnake.snake;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

/**
 * Snake object. It contains snake positions and speed
 */
public class Snake {
    /* Array, which contains all segments of snake.
      Each segment - snakeParameter below */
    private ArrayList<int[]> snake;

    /* 0 - way, 1 - i index of field, 2 - j index of field
    ways: 0 - up, 1 - left, 2 - right, 3 - down*/
    private int[] snakeParameters;
    private byte[][] field; // Field from GameFragment
    private int speed = 0;  // Snake speed

    /**
     * Default constructor
     *
     * @param field   - Field from GameFragment
     * @param context - GameActivity context
     */
    public Snake(byte[][] field, Context context) {
        snake = new ArrayList<>();
        this.field = field;
        reset(context);
    }

    /**
     * Start configuration of the snake(3 segments)
     */
    public void reset(Context context) {
        snake.clear();
        for (int i = 0; i < 3; i++) {
            snakeParameters = new int[3];
            snakeParameters[0] = 0;
            snakeParameters[1] = GameFragment.FIELD_SIZE / 2 + i;
            snakeParameters[2] = (GameFragment.FIELD_SIZE / 2);
            snake.add(snakeParameters);
        }
        setWay((int) (Math.random() * 3));  // Random way of snake
        for (int[] i : snake) {
            field[i[1]][i[2]] = GameFragment.AT_SIGN;
        }

        /* Getting start speed from preferences*/
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        speed = Integer.parseInt(sp.getString(context.getString(R.string.pref_difficulty_key),
                context.getString(R.string.pref_default_difficulty)));
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void increaseSpeed() {
        this.speed -= GameFragment.FIELD_SIZE / 2;
    }

    public void reduceSpeed() {
        this.speed += GameFragment.FIELD_SIZE / 2;
    }

    public void updateSpeed() {
        this.speed -= GameFragment.FIELD_SIZE / 3;
    }

    public ArrayList<int[]> getSnake() {
        return snake;
    }

    public void setWay(int way) {
        snake.get(0)[0] = way;
    }

    public int getWay() {
        return snake.get(0)[0];
    }

    public void update() {
        //Tag of snake = SPACE
        field[snake.get(snake.size() - 1)[1]][snake.get(snake.size() - 1)[2]] = GameFragment.SPACE;

        // Define future coordinates of snake depending on way
        for (int i = 0; i < snake.size(); i++)
            switch (snake.get(i)[0]) {
                case 0:
                    snake.get(i)[1] -= 1; //up
                    break;
                case 1:
                    snake.get(i)[2] -= 1; //left
                    break;
                case 2:
                    snake.get(i)[2] += 1; //right
                    break;
                case 3:
                    snake.get(i)[1] += 1; //down
                    break;
            }

        //Update way for following segments of snake
        for (int i = snake.size() - 1; i > 0; i--) {
            snake.get(i)[0] = snake.get(i - 1)[0];
        }

        // Update snake on the screen
        for (int[] i : snake) {
            field[i[1]][i[2]] = GameFragment.AT_SIGN;
        }
    }

    /**
     * If snake head in food - true, else - false
     */
    public boolean isIntersectsWithFood(int[] food) {
        if (snake.get(0)[1] == food[0] && snake.get(0)[2] == food[1]) {
            return true;
        } else return false;
    }

    /**
     * If snake head in bonus - true, else - false
     */
    public boolean isIntersectsWithBonus(int[] bonus) {
        if (snake.get(0)[1] == bonus[0] && snake.get(0)[2] == bonus[1]) {
            return true;
        } else return false;
    }

    /**
     * If snake head in snake body - true, else - false
     */
    public boolean isCannibal() {
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(0)[1] == snake.get(i)[1] &&
                    snake.get(0)[2] == snake.get(i)[2])
                return true;
        }
        return false;
    }

    /**
     * Increment snake length
     */
    public void pushFood() {
        int[] last = new int[3];
        last[0] = snake.get(snake.size() - 1)[0];
        switch (last[0]) {
            case 0:
                last[1] = snake.get(snake.size() - 1)[1] + 1;
                last[2] = snake.get(snake.size() - 1)[2];
                break;
            case 1:
                last[1] = snake.get(snake.size() - 1)[1];
                last[2] = snake.get(snake.size() - 1)[2] + 1;
                break;
            case 2:
                last[1] = snake.get(snake.size() - 1)[1];
                last[2] = snake.get(snake.size() - 1)[2] - 1;
                break;
            case 3:
                last[1] = snake.get(snake.size() - 1)[1] - 1;
                last[2] = snake.get(snake.size() - 1)[2];
                break;
        }
        snake.add(last);
    }

    /**
     * Increment snake length
     */
    public void popFood() {
        if (snake.size() > 3) {
            field[snake.get(snake.size() - 1)[1]][snake.get(snake.size() - 1)[2]] = GameFragment.SPACE;
            snake.remove(snake.size() - 1);
        }
    }
}
