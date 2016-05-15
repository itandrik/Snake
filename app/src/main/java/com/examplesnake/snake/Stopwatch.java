package com.examplesnake.snake;

import android.os.Handler;
import android.os.SystemClock;
import android.widget.TextView;

/**
 * Class, which work like simple stopwatch.
 * It can save time of food and bonus update.
 * Show stopwatch in the screen.
 */
public class Stopwatch {
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;

    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    private int secs = 0;
    private int mins = 0;
    private int milliseconds = 0;
    private long foodTime = 0L;
    private long bonusTime = 0L;
    private long bonusIsActiveTime = 0L;

    TextView stopwatchTv;
    Handler handler;

    public Stopwatch(TextView stopwatchTv) {
        this.stopwatchTv = stopwatchTv;
        handler = new Handler();
    }

    public void start() {
        startTime = SystemClock.uptimeMillis();
        handler.postDelayed(updateTimer, 0);
    }

    public void pause() {
        timeSwapBuff += timeInMilliseconds;
        handler.removeCallbacks(updateTimer);
    }

    public void reset() {
        startTime = 0L;
        timeInMilliseconds = 0L;
        timeSwapBuff = 0L;
        updatedTime = 0L;
        secs = 0;
        mins = 0;
        milliseconds = 0;
        handler.removeCallbacks(updateTimer);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getTimeSwapBuff() {
        return timeSwapBuff;
    }

    public void setTimeSwapBuff(long timeSwapBuff) {
        this.timeSwapBuff = timeSwapBuff;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setFoodTime(long foodTime) {
        this.foodTime = foodTime;
    }

    public void setBonusTime(long bonusTime) {
        this.bonusTime = bonusTime;
    }

    public void setTime(long time) {
        updatedTime = time;
    }

    public long getBonusTime() {
        return bonusTime;
    }

    public long getFoodTime() {
        return foodTime;
    }

    public long getTime() {
        return updatedTime;
    }

    public Runnable updateTimer = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            secs = (int) (updatedTime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (updatedTime % 1000);
            stopwatchTv.setText(String.format("%02d:%02d:%03d", mins, secs, milliseconds));
            handler.postDelayed(this, 0);
        }
    };

}
