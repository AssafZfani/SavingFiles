package zfani.assaf.saving_files;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION_CODE = 10001;
    private static final int REQUEST_IMAGE_CAPTURE_CODE = 10002;
    private EditText etText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etText = findViewById(R.id.etText);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveOnExternalStorage(etText.getText().toString());
        } else {
            Toast.makeText(this, "You have to grant storage permission in order to save files", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "The picture has successfully!", Toast.LENGTH_LONG).show();
        }
    }

    public void saveTextFile(View view) {
        // Getting text from edit text view
        final String textToSave = etText.getText().toString();
        // Checking if the text contains empty string or blank spaces
        if (textToSave.trim().isEmpty()) {
            Toast.makeText(this, "Please type a valid input", Toast.LENGTH_SHORT).show();
            return;
        }
        // Showing dialog with 2 options of saving files (on internal or external storage)
        new AlertDialog.Builder(this).setTitle("Choose between these options:").setSingleChoiceItems(new String[]{"Internal Storage", "External Storage"}, 0, (dialog, which) -> {
            dialog.dismiss();
            if (which == 0) {
                saveOnInternalStorage(textToSave);
            } else {
                saveOnExternalStorage(textToSave);
            }
        }).show();
    }

    private void saveOnInternalStorage(String textToSave) {
        try {
            // Create a new OutputStreamWriter called document.txt
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("document.txt", Context.MODE_PRIVATE));
            // Write something in the file
            outputStreamWriter.write(textToSave);
            // Close the stream
            outputStreamWriter.close();
            Toast.makeText(this, "File write succeeded: " + getFilesDir().getAbsolutePath() + "/document.txt", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "File write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveOnExternalStorage(String textToSave) {
        // Checking if the user granted storage permissions or not and requesting them
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_CODE);
            return;
        }
        // Creating docs folder on external storage
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/docs");
        boolean isFileExisted = dir.exists();
        if (!isFileExisted) {
            isFileExisted = dir.mkdirs();
        }
        if (!isFileExisted) {
            Toast.makeText(this, "File creation failed", Toast.LENGTH_SHORT).show();
            return;
        }
        // Creating new file called document.txt
        File file = new File(dir, "document.txt");
        try {
            // Create a new FileOutputStreamWriter
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            // Create a new PrintWriter
            PrintWriter printWriter = new PrintWriter(fileOutputStream);
            // Print something int the file
            printWriter.print(textToSave);
            // flush the PrintWriter
            printWriter.flush();
            // close the PrintWriter
            printWriter.close();
            // close the stream
            fileOutputStream.close();
            Toast.makeText(this, "File write succeeded: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "File write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void takePicture(View view) {
        // Showing dialog with 2 options of saving files (on internal or external storage)
        new AlertDialog.Builder(this).setTitle("Choose between these options:").setSingleChoiceItems(new String[]{"Internal Storage", "External Storage"}, 0, (dialog, which) -> {
            dialog.dismiss();
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile(which == 0);
                } catch (Exception e) {
                    // Error occurred while creating the File
                    Toast.makeText(this, "File creation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this, "zfani.assaf.saving_files.file_provider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_CODE);
                }
            }
        }).show();
    }

    private File createImageFile(boolean isInternal) throws IOException {
        // Creating timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        // Creating image file name
        String imageFileName = "JPEG_" + timeStamp + "_";
        // Creating a folder reference of device pictures
        File storageDir = isInternal ? getExternalFilesDir(Environment.DIRECTORY_PICTURES) : new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        // Creating and returning temp file
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}