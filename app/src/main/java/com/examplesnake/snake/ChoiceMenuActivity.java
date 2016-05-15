package com.examplesnake.snake;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ChoiceMenuActivity extends Activity implements View.OnClickListener {
    Button continueBtn;
    private Intent intent;
    final int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT; // Reducing code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_menu);

        Button newGameBtn = (Button) findViewById(R.id.newGameBtn);
        Button difficultyBtn = (Button) findViewById(R.id.difficultyBtn);

        newGameBtn.setOnClickListener(this);
        difficultyBtn.setOnClickListener(this);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/GoodDog.otf");
        newGameBtn.setTypeface(typeface);
        difficultyBtn.setTypeface(typeface);
        Database db = new Database(this);
        db.open();
        if (db.isContinueGame()) {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layoutChoiceButtons);
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            /*int marginInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    getResources().getDimension(R.dimen.margin_between_buttons),
                    this.getResources().getDisplayMetrics());
            layoutParams.setMargins(0, 0, 0, marginInPx);*/
            layoutParams.gravity = Gravity.CENTER;
            continueBtn = new Button(this);
            continueBtn.setId(R.id.continueBtn);
            continueBtn.setBackgroundResource(R.drawable.menu_button_selector);
            continueBtn.setText(R.string.continue_choice);
            continueBtn.setTextSize(24);
            continueBtn.setOnClickListener(this);
            continueBtn.setTypeface(typeface);
            linearLayout.addView(continueBtn, 0, layoutParams);
        }
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.continueBtn: // Load coordinates from database and continue previous game
                intent = new Intent(this, GameActivity.class)
                        .putExtra("isContinue", true);
                startActivity(intent);
                break;
            case R.id.newGameBtn:   // Starting new game
                final EditText nameEt = new EditText(this);
                nameEt.setHint("Your name");
                new AlertDialog.Builder(this) //AlertDialog, which get player name
                        .setTitle("Enter your name")
                        .setView(nameEt)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                intent = new Intent(getApplicationContext(), GameActivity.class);
                                intent.putExtra("name", nameEt.getText().toString());
                                startActivity(intent);
                            }
                        })
                        .show();
                break;
            case R.id.difficultyBtn: //Change difficult
                intent = new Intent(this, DifficultPreferenceActivity.class);
                startActivity(intent);
                break;
        }

    }
}
