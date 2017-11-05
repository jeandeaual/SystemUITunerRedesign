package com.zacharee1.systemuituner.misc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;

import com.zacharee1.systemuituner.R;
import com.zacharee1.systemuituner.activites.ItemListActivity;
import com.zacharee1.systemuituner.activites.MainActivity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Utils
{
    public static boolean isPackageInstalled(@SuppressWarnings("SameParameterValue") String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static float pxToDp(Context context, float px) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, r.getDisplayMetrics());
    }

    public static float pxToSp(Context context, float px) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, px, r.getDisplayMetrics());
    }

    public static String runCommand(String... strings) {
        try{
            Process comm = Runtime.getRuntime().exec("sh");
            DataOutputStream outputStream = new DataOutputStream(comm.getOutputStream());

            for (String s : strings) {
                outputStream.writeBytes(s+"\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();

            BufferedReader inputReader = new BufferedReader(new InputStreamReader(comm.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(comm.getErrorStream()));

            String ret = "";
            String line;

            while ((line = inputReader.readLine()) != null) {
                ret = ret.concat(line).concat("\n");
            }

            while ((line = errorReader.readLine()) != null) {
                ret = ret.concat(line).concat("\n");
            }

            try {
                comm.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("Err", e.getMessage());
            }
            outputStream.close();

            return ret;
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static String[] checkPermissions(Context context, String[] permissions) {
        ArrayList<String> notPerms = new ArrayList<>();

        for (String permission : permissions) {
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) notPerms.add(permission);
        }

        return notPerms.toArray(new String[] {});
    }

    public static void startUp(Activity context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean firstStart = sharedPreferences.getBoolean("first_start", true);
        if (firstStart && Build.MANUFACTURER.toLowerCase().contains("samsung") && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            sharedPreferences.edit().putBoolean("safe_mode", true).apply();
            new AlertDialog.Builder(context)
                    .setTitle(context.getResources().getString(R.string.notice))
                    .setMessage(context.getResources().getString(R.string.safe_mode_auto_enabled))
                    .setPositiveButton(context.getResources().getString(R.string.ok), null)
                    .show();
        }
        sharedPreferences.edit().putBoolean("first_start", false).apply();

        if (sharedPreferences.getBoolean("hide_welcome_screen", false)) {
            context.startActivity(new Intent(context, ItemListActivity.class));
        } else {
            context.startActivity(new Intent(context, MainActivity.class));
        }
    }
}
