package com.ogulcankacar.yazilarial;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ShowAllActivity extends AppCompatActivity {

    private TextView textView;
    private Button btn_share, btn_pdf;
    private Context context = this;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all);

        textView = findViewById(R.id.textView);
        btn_pdf = findViewById(R.id.pdf);
        btn_share = findViewById(R.id.share);
        mAdView = findViewById(R.id.adView);

        Intent intent = getIntent();
        String text = intent.getStringExtra("text");
        textView.setText(text);


        //ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //Banner ADS
        bannerAds();



        //CreatePDF
        btn_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createPdf();

            }
        });

        //ShareText
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextforWp();
            }
        });



    }//Oncreate

    //SendText
    private void sendTextforWp() {

        boolean installed = appInstallOrNot("com.whatsapp");
        if (installed) {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");


            String sentMapsUrl = textView.getText().toString();
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, sentMapsUrl);
            try {
                startActivity(whatsappIntent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(getApplicationContext(), R.string.sendfailed, Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(ShowAllActivity.this, R.string.Whatsappisnotinstalled, Toast.LENGTH_SHORT).show();
        }
    }

    //CreatePdf
    private void createPdf() {

        final Document pdfDosyam = new Document();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShowAllActivity.this);
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
                    public void onClick(DialogInterface dialog,int which) {
                        String pdfType = pdfSize.getText().toString();
                        String pdfName = pdfTitle.getText().toString();
                        String pdfSubject = descriptionPDF.getText().toString();
                        String pdfAuthorr = pdfAuthor.getText().toString();

                        if (TextUtils.isEmpty(pdfType) && TextUtils.isEmpty(pdfName) && TextUtils.isEmpty(pdfSubject) && TextUtils.isEmpty(pdfAuthorr)){
                            Toast.makeText(context, R.string.Fillinallfields, Toast.LENGTH_SHORT).show();
                        }else{
                            if (pdfType == "A4" || pdfType == "a4"){
                                pdfDosyam.setPageSize(PageSize.A4);
                            }else{
                                pdfDosyam.setPageSize(PageSize.A5);
                            }
                            pdfDosyam.addCreationDate();
                            pdfDosyam.addTitle(pdfName);
                            pdfDosyam.addSubject(pdfSubject);
                            pdfDosyam.addAuthor(pdfAuthorr);
                            pdfDosyam.addCreator("YazılarıAl - Oğulcan KAÇAR DEV");


                            try {

                                PdfWriter.getInstance(pdfDosyam, new FileOutputStream(new
                                        File(Environment.getExternalStorageDirectory(),"mypdf.pdf")));

                            } catch (DocumentException | FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            pdfDosyam.open();
                            Paragraph paragraf = new Paragraph(textView.getText().toString());
                            try {
                                pdfDosyam.add(paragraf);
                                pdfDosyam.close();
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

    //banner ads
    private void bannerAds() {
        //banner ad ıd ca-app-pub-4310209378038401/5504550530

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        if (!mAdView.isLoading()){
            mAdView.setVisibility(View.INVISIBLE);
        }else{
            mAdView.setVisibility(View.VISIBLE);
        }
    }

    //Whatsapp
    private boolean appInstallOrNot(String url){
        PackageManager packageManager = getPackageManager();
        boolean app_installed;
        try{
            packageManager.getPackageInfo(url,PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }catch (PackageManager.NameNotFoundException e){
            app_installed = false;
        }
        return  app_installed;
    }

}//Main