package com.hfad.lookafter;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Mohru on 21.11.2016.
 */

public class Utils {

    public static void showSnackbar(View view, int stringResource, final Context context) {
        Snackbar.make(view, stringResource, Snackbar.LENGTH_LONG).setAction("Akcja", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "tekst", Toast.LENGTH_SHORT).show();
            }
        }).show();
    }
}
