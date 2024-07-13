package com.candybar.dev.hydro_navy_blue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import android.os.Build;
import android.view.Window;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the dialog
        showInstallationDialog();
    }

    private void showInstallationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Thank you for installing Hydro - Navy Blue Icons");
        builder.setMessage("Please return to the main app to apply your newly installed theme.");
        builder.setPositiveButton("Open dashboard app", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // Intent to open main app
                Intent launchMainAppIntent = getPackageManager().getLaunchIntentForPackage("com.candybar.dev");
                if (launchMainAppIntent != null) {
                    startActivity(launchMainAppIntent);
                } else {
                    // Handle case where main app is not installed
                    Toast.makeText(MainActivity.this, "Main app is not installed", Toast.LENGTH_SHORT).show();
                }

                finish(); // Finish current activity
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Determine the appropriate status bar color based on dark mode
        int statusBarColor = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            statusBarColor = getResources().getConfiguration().isNightModeActive()
                    ? ContextCompat.getColor(this, R.color.black)
                    : ContextCompat.getColor(this, R.color.white);
        }

        // Set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(statusBarColor);
        }
    }
}
