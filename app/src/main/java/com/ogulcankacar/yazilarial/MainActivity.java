package com.ogulcankacar.yazilarial;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.ogulcankacar.yazilarial.databinding.ActivityMainBinding;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityMainBinding;
    private AlertDialog.Builder alertDialog;


    //ads
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private int adstatus = 0;

    //Image
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;
    private Uri image_uri;

    //Permission
    private String cameraPermission[];
    private String storagePermission[];
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = activityMainBinding.getRoot();
        setContentView(view);


        mAdView = findViewById(R.id.adView);
        alertDialog = new AlertDialog.Builder(MainActivity.this);


        activityMainBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageImportDialog();
            }
        });


        //ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //banner
        bannerAds();
        //interstitial
        ınterstitialAd();

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


    }//OnCreate

    //SentText for Whatsapp
    private void share() {

        activityMainBinding.share.setVisibility(View.VISIBLE);
        activityMainBinding.pdf.setVisibility(View.VISIBLE);
        activityMainBinding.showall.setVisibility(View.VISIBLE);
        activityMainBinding.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean installed = appInstallOrNot("com.whatsapp");
                if (installed) {
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.setPackage("com.whatsapp");

                    String sentMapsUrl = activityMainBinding.resultEt.getText().toString();
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, sentMapsUrl);
                    try {
                        startActivity(whatsappIntent);
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), R.string.sendfailed, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.Whatsappisnotinstalled, Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    //Create pdf
    private void createPdf(final String text) {

        activityMainBinding.pdf.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Document pdfDosyam = new Document();

                alertDialog.setTitle(R.string.CreatePDF);
                alertDialog.setCancelable(false);
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText pdfSize = new EditText(context);
                pdfSize.setHint(R.string.pageType);
                layout.addView(pdfSize);

                final EditText pdfTitle = new EditText(context);
                pdfTitle.setHint(R.string.pdfTitle);
                layout.addView(pdfTitle);

                final EditText descriptionPDF = new EditText(context);
                descriptionPDF.setHint(R.string.descriptionPDF);
                layout.addView(descriptionPDF);

                final EditText pdfAuthor = new EditText(context);
                pdfAuthor.setHint(R.string.pdfAuthor);
                layout.addView(pdfAuthor);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                alertDialog.setView(layout);
                alertDialog.setIcon(R.drawable.pdf);
                alertDialog.setPositiveButton(R.string.save,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String pdfType = pdfSize.getText().toString();
                                String pdfName = pdfTitle.getText().toString();
                                String pdfSubject = descriptionPDF.getText().toString();
                                String pdfAuthorr = pdfAuthor.getText().toString();

                                if (TextUtils.isEmpty(pdfType) && TextUtils.isEmpty(pdfName) && TextUtils.isEmpty(pdfSubject) && TextUtils.isEmpty(pdfAuthorr)) {
                                    Toast.makeText(context, R.string.Fillinallfields, Toast.LENGTH_SHORT).show();
                                } else {
                                    if (Objects.equals(pdfType, "A4") || Objects.equals(pdfType, "a4")) {
                                        pdfDosyam.setPageSize(PageSize.A4);
                                    } else {
                                        pdfDosyam.setPageSize(PageSize.A5);
                                    }
                                    pdfDosyam.addCreationDate();
                                    pdfDosyam.addTitle(pdfName);
                                    pdfDosyam.addSubject(pdfSubject);
                                    pdfDosyam.addAuthor(pdfAuthorr);
                                    pdfDosyam.addCreator("YazılarıAl - Oğulcan KAÇAR DEV");


                                    try {

                                        PdfWriter.getInstance(pdfDosyam, new FileOutputStream(new
                                                File(Environment.getExternalStorageDirectory(), "mypdf.pdf")));

                                    } catch (DocumentException | FileNotFoundException e) {
                                        e.printStackTrace();
                                    }

                                    pdfDosyam.open();
                                    Paragraph paragraf = new Paragraph(text);
                                    try {
                                        pdfDosyam.add(paragraf);
                                        pdfDosyam.close();
                                        Toast.makeText(context, R.string.createdPDF, Toast.LENGTH_LONG).show();
                                    } catch (DocumentException e) {
                                        e.printStackTrace();
                                    }


                                }


                            }
                        });
                alertDialog.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });


                AlertDialog dialogg = alertDialog.create();
                dialogg.show();


            }
        });


    }

    //Show all text
    private void showAllText(final String text) {

        activityMainBinding.showall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowAllActivity.class);
                intent.putExtra("text", text);
                startActivity(intent);
            }
        });

    }

    //banner ads
    private void bannerAds() {
        //bannerads ca-app-pub-4310209378038401/5504550530
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        if (!mAdView.isLoading()) {
            mAdView.setVisibility(View.INVISIBLE);
        } else {
            mAdView.setVisibility(View.VISIBLE);
        }
    }

    //interstitial ads
    private void ınterstitialAd() {

        //interstital ad ca-app-pub-4310209378038401/3310659704
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ad ca-app-pub-4310209378038401/3310659704");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
    }

    private void showImageImportDialog() {

        if (mInterstitialAd.isLoaded() && adstatus < 4) {
            mInterstitialAd.show();
            adstatus = adstatus + 1;
            if (adstatus == 10) {
                adstatus = 0;
            }

            String[] items = {getString(R.string.camera), getString(R.string.gallery)};
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.choosepicture);
            dialog.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        if (!checkCameraPermission()) {
                            requestCameraPermission();
                        } else {
                            pickCamera();
                        }
                    }
                    if (which == 1) {
                        if (!checkStoragePermission()) {
                            requestStoragePermission();
                        } else {
                            pickGallery();
                        }
                    }
                }
            });
            dialog.create().show();
        } else {
            String[] items = {getString(R.string.camera), getString(R.string.gallery)};
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.choosepicture);
            dialog.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        if (!checkCameraPermission()) {
                            requestCameraPermission();
                        } else {
                            pickCamera();
                        }
                    }
                    if (which == 1) {
                        if (!checkStoragePermission()) {
                            requestStoragePermission();
                        } else {
                            pickGallery();
                        }
                    }
                }
            });
            dialog.create().show();
        }


    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(this, String.valueOf(R.string.permissionDenied), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(this, String.valueOf(R.string.permissionDenied), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image to Text");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                activityMainBinding.imageIv.setImageURI(resultUri);

                BitmapDrawable bitmapDrawable = (BitmapDrawable) activityMainBinding.imageIv.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> items = recognizer.detect(frame);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    TextBlock myItem = items.valueAt(i);
                    sb.append(myItem.getValue());
                    sb.append("\n");
                }
                activityMainBinding.resultEt.setText(sb.toString());
                share();
                showAllText(sb.toString());
                createPdf(sb.toString());
            }
        }
    }

    //Whatsapp
    private boolean appInstallOrNot(String url) {
        PackageManager packageManager = getPackageManager();
        boolean app_installed;
        try {
            packageManager.getPackageInfo(url, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }


}//Main