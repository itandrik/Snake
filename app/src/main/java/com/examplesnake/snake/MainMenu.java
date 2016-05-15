package com.examplesnake.snake;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainMenu extends Activity implements
        View.OnClickListener {
    Button playBtn;
    Button leaderboardBtn;
    Button exitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        playBtn = (Button) findViewById(R.id.playBtn);
        leaderboardBtn = (Button) findViewById(R.id.leaderboardBtn);
        exitBtn = (Button) findViewById(R.id.exitBtn);
        playBtn.setOnClickListener(this);
        leaderboardBtn.setOnClickListener(this);
        exitBtn.setOnClickListener(this);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/GoodDog.otf");
        playBtn.setTypeface(typeface);
        leaderboardBtn.setTypeface(typeface);
        exitBtn.setTypeface(typeface);
    }

    @Override
    public void onResume() {
        super.onResume();
        GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        // Its a pity, I don't have 25$ for developer console :(
        // How to add GPGS support : https://habrahabr.ru/post/238327/

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
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.playBtn:
                intent = new Intent(this, ChoiceMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                break;
            case R.id.leaderboardBtn:
                intent = new Intent(this, LeaderBoardActivity.class);
                startActivity(intent);
                break;
            case R.id.exitBtn:
                finish();
                System.exit(0);
                break;
        }
    }
}
