package com.example.camerafb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    String currentPath;
    FirebaseStorage refST;
    StorageReference refFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 100);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 200);
        }

        refST = FirebaseStorage.getInstance();
        refFull = refST.getReference("camera/");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 200) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void takeFull(View view) {
        // creating local temporary file to store the full resolution photo
        String filename = "tempfile";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imgFile = File.createTempFile(filename, ".jpg", storageDir);
            currentPath = imgFile.getAbsolutePath();
            Uri imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.camerafb.fileprovider", imgFile);
            Intent takePicIntent = new Intent();
            takePicIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            takePicIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePicIntent, 19);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Failed to create temporary file", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data_back) {
        super.onActivityResult(requestCode, resultCode, data_back);

        if (resultCode == Activity.RESULT_OK) {
            final ProgressDialog pd;
            // Upload camera thumbnail image file
            // Upload camera full resolution image file
            if (requestCode == 19) {
                pd = ProgressDialog.show(this, "Upload image", "Uploading...", true);
                StorageReference refImg = refFull.child("image");
                Bitmap imageBitmap = BitmapFactory.decodeFile(currentPath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                refImg.putBytes(data)
                        .addOnSuccessListener(taskSnapshot -> {
                            pd.dismiss();
                            Toast.makeText(MainActivity.this, "Image Uploaded", Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(exception -> {
                            pd.dismiss();
                            Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_LONG).show();
                        });
            }
        }
    }
}
