package com.asaadna.photogram;

import android.content.Context;
import android.widget.Toast;

public class utils {
    public static void toastDebug(Context c , String text) {
        Toast.makeText(c,text,Toast.LENGTH_SHORT).show();
    }
}
