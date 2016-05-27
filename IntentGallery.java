package com.beautiful.photosticker.screens;

import android.app.Activity;
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
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.beautiful.photosticker.EditPhotoActivity;
import com.beautiful.photosticker.R;
import com.beautiful.photosticker.helper.VarHolder;

import java.io.File;

public class MainActivity extends Activity implements View.OnClickListener
{
    private Uri imageUri;
    private boolean rated;
    private Button btnCamera,btnGallery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamera = (Button) findViewById(R.id.btn_main__camera);
        btnGallery = (Button) findViewById(R.id.btn_main__gallery);

        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);

        retriveRate();
    }

    private void retriveRate() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        rated = sharedPreferences.getBoolean(VarHolder.RATE,false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_main__camera:
                startCamera();
                break;
            case R.id.btn_main__gallery:
                getPic();
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VarHolder.FOCUS_ID) {
            if (data != null) {
                if (resultCode == Activity.RESULT_OK){
                VarHolder.uriPhoto = data.getData();
                Intent intent = new Intent(MainActivity.this, EditPhotoActivity.class);
                intent.putExtra(VarHolder.KIND_GET, VarHolder.GALLERY);
                startActivity(intent);
                }
            }
        } else {
            if (requestCode == VarHolder.TAKE_PICTURE) {
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(cr, selectedImage);

                        VarHolder.bitmap = bitmap;

                        Intent intent = new Intent(MainActivity.this, EditPhotoActivity.class);
                        intent.putExtra(VarHolder.KIND_GET,VarHolder.CAMERA);
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
    private void getPic() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, VarHolder.FOCUS_ID);
    }
    private void startCamera() {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
            imageUri = Uri.fromFile(photo);
            startActivityForResult(intent, VarHolder.TAKE_PICTURE);
    }

    @Override
    public void onBackPressed() {
        if (!rated)
        showDialog(VarHolder.DIALOG_EXIT);
        else
            super.onBackPressed();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog alertDialog = null;switch (id){
            case VarHolder.DIALOG_EXIT:
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
    private void saveRated() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(VarHolder.RATE,true);
        editor.commit();
    }
}
