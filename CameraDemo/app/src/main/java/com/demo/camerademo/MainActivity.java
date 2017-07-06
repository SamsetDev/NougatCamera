package com.demo.camerademo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.R.attr.path;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private int REQUEST_CAMERA = 101;
    private int PIC_CROP = 102;
    private Uri photoURI = null;
    private String IMAGE_DIRECTORY_NAME = "sample";
    //String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.iv);
        findViewById(R.id.btncamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    private void takePicture() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = getOutputMediaFile();
                photoURI = FileProvider.getUriForFile(MainActivity.this, getString(R.string.file_provider_authority), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoURI = getOutputMediaFileUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(intent, REQUEST_CAMERA);

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageView.setImageURI(photoURI);

                } else {
                    try {

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 8;
                        final Bitmap bitmap = BitmapFactory.decodeFile(photoURI.getPath(), options);
                        //imageView.setImageBitmap(bitmap);

                        // If you want croping use this
                        performCrop(photoURI);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == PIC_CROP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri selectedImageUri = data.getData();
                    imageView.setImageURI(selectedImageUri);
                    Log.e("TAG", " onResult after crop pixel " + selectedImageUri);

                } else {
                    Uri selectedImageUri = data.getData();


                    if (selectedImageUri != null) {
                        imageView.setImageURI(selectedImageUri);

                    } else {

                        Bitmap selectedBitmap = (Bitmap) data.getExtras().get("data");
                        imageView.setImageBitmap(selectedBitmap);
                    }
                }

            }
        }
    }

    private void performCrop(Uri selectedImageUri) {
        try {
            Log.e("TAG", " get data crop " + selectedImageUri);
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(selectedImageUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 128);
            cropIntent.putExtra("outputY", 128);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            if (getPackageManager() != null) {
                startActivityForResult(cropIntent, PIC_CROP);
            }

        } catch (Exception anfe) {
            anfe.printStackTrace();
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Log.e("TAG", " Croping error " + errorMessage);

        }
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    /*
     * returning image / video
     */
    private File getOutputMediaFile() {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "+ IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }
}
