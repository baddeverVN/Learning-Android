package com.beststicker.makebeautiful.snapdoggy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.beststicker.makebeautiful.snapdoggy.data.VarHolder;
import com.test.sdk.AppConfig;

import java.io.File;

public class MainActivity extends Activity implements View.OnClickListener {
    public static final int SELECT_PICTURE = 1;
    public static final int TAKE_PICTURE = 2;
    private static final String RATE = "rate";
    private static final int DIALOG_RATE = 3;

    private ImageButton btnGalerry;
    private ImageButton btnCamera;
    private ImageButton btnRate;

    private boolean rated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        AppConfig.config(this);

        btnGalerry = (ImageButton) findViewById(R.id.btn_gallery);
        btnCamera = (ImageButton) findViewById(R.id.btn_camera);
        btnRate = (ImageButton) findViewById(R.id.btn_rate);
        btnGalerry.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnRate.setOnClickListener(this);

        retriveRate();
    }

    private void saveRated() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(RATE, true);
        editor.commit();
    }

    private void retriveRate() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        rated = sharedPreferences.getBoolean(RATE, false);
    }

    @Override
    public void onBackPressed() {
        if (!rated)
        showDialog(DIALOG_RATE);
        else
            super.onBackPressed();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog alertDialog = null;
        switch (id) {
            case DIALOG_RATE:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.text_rate);
                builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setPositiveButton("Rate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        saveRated();
                        finish();
                    }
                });
                alertDialog = builder.create();
                break;
        }
        return alertDialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gallery:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
                break;
            case R.id.btn_camera:
                startCamera();
                break;
            case R.id.btn_rate:
                rateApp();
                break;
        }
    }

    public void rateApp() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
        saveRated();
//        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE) {
            if (data != null) {
                VarHolder.uriPhoto = data.getData();
                VarHolder.kindPhoto = SELECT_PICTURE;
                Intent intent = new Intent(MainActivity.this, CropActivity.class);
                startActivity(intent);
            }
        } else {
            if (requestCode == TAKE_PICTURE) {
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(cr, selectedImage);
                        VarHolder.bitmap = bitmap;
                        VarHolder.kindPhoto = TAKE_PICTURE;
                        Intent intent = new Intent(MainActivity.this, CropActivity.class);
                        startActivity(intent);

                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                                .show();
                        Log.e("Camera", e.toString());
                    }

                }
            }
        }

    }

    private Uri imageUri;

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }
}
