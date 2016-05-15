package com.examplesnake.snake;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity, which shows leaderboard. Use SimpleCursorAdapter
 */
public class LeaderBoardActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Database db;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        db = new Database(this);
        db.open();                  // Open database connection

        String[] from = new String[]{Database.PLAYER_NAME_FIELD, Database.SCORES_FIELD};
        int[] to = new int[]{R.id.player_name_leaderboard_tv, R.id.scores_leaderboard_tv};
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.leader_board_item,
                null, from, to, 0
        );
        TextView tv = (TextView) findViewById(R.id.leaderboard_title);
        tv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/orange_juice.ttf")); // set font
        ListView leaderBoard = (ListView) findViewById(R.id.leader_board_listview);
        leaderBoard.setAdapter(adapter);
        leaderBoard.setEmptyView(findViewById(R.id.emptyElement));
        getSupportLoaderManager().initLoader(0, null, this);
    }

    /**
     * Close database connection
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    /**
     * Hide the status bar for different API versions
     */
    @Override
    protected void onResume() {
        super.onResume();
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(this, db);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    /**
     * Class which is needed by SipleCursorAdapter.
     * It load scores from database
     */
    static class MyCursorLoader extends CursorLoader {

        Database db;

        public MyCursorLoader(Context context, Database db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            return db.readLeaderBoard();
        }

    }
}
