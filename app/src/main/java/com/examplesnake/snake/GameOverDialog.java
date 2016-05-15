package com.examplesnake.snake;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Its a dialog, which show player score,name and time.
 * We can choose - go to main menu of try again.
 */
public class GameOverDialog extends DialogFragment implements DialogInterface.OnClickListener {
    private String message; //Result message

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        message = getArguments().getString("name") + "\n\tYour score : " +
                getArguments().getInt("scores") +
                "\n\tYour time : " + getArguments().getString("time");
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setTitle("Game Over!")
                .setPositiveButton("Retry", this)
                .setNegativeButton("Main menu", this)
                .setIcon(R.drawable.ic_highlight_off_black_24dp)
                .setCancelable(false)
                .setNeutralButton("Share", this)
                .setMessage(message);
        return adb.create();
    }

    /**
     * Making Dialog non cancelable
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Handle click "Go to main menu", "Share"
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_NEGATIVE:
                getActivity().finish();
                break;
            case Dialog.BUTTON_NEUTRAL:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
        }
    }
}
