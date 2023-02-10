package com.softeksol.paisalo.jlgsourcing.activities;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.softeksol.paisalo.jlgsourcing.Global;
import com.softeksol.paisalo.jlgsourcing.R;
import com.softeksol.paisalo.jlgsourcing.WebOperations;
import com.softeksol.paisalo.jlgsourcing.entities.ESignBorrower;
import com.softeksol.paisalo.jlgsourcing.entities.ESigner;
import com.softeksol.paisalo.jlgsourcing.fragments.FragmentCollection;
import com.softeksol.paisalo.jlgsourcing.handlers.DataAsyncResponseHandler;
import com.softeksol.paisalo.jlgsourcing.retrofit.ApiClient;
import com.softeksol.paisalo.jlgsourcing.retrofit.ApiInterface;
import com.softeksol.paisalo.jlgsourcing.retrofit.BorrowerData;
import com.softeksol.paisalo.jlgsourcing.retrofit.CheckCrifData;
import com.softeksol.paisalo.jlgsourcing.retrofit.ScrifData;

import java.util.Random;

import cz.msebera.android.httpclient.Header;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrifScore extends AppCompatActivity {

    ProgressBar progressBar,progressBarsmall;
    TextView textView7,textView8,textView13,textView6,text_srifScore,textView5,text_serverMessage,textView_valueEmi,text_wait;
    GifImageView gifImageView;
    String amount,emi,score,message;
    int scrifScore=0;
    LinearLayout layout_design,layout_design_pending;
    Button btnTryAgain,btnSrifScore;
    TextView textView_emi;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Intent i;
    String ficode,creator;
    CheckCrifData checkCrifData=new CheckCrifData();
    ESignBorrower eSignerborower;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crif_score);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Loan Eligibility");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        i=getIntent();
        Log.d("TAG", "onCreate: "+i.getStringExtra("ficode"));
        ficode=i.getStringExtra("FIcode");
        creator=i.getStringExtra("creator");
        eSignerborower = (ESignBorrower) i.getSerializableExtra("ESIGN_BORROWER");
        sharedPreferences = getSharedPreferences("KYCData",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        progressBar=findViewById(R.id.circular_determinative_pb);
        progressBarsmall=findViewById(R.id.progressBar);
        textView7=findViewById(R.id.textView7);
        textView_valueEmi=findViewById(R.id.textView_valueEmi);
        textView_emi=findViewById(R.id.textView_emi);
        textView8=findViewById(R.id.textView8);
        gifImageView=findViewById(R.id.gifImageView);
        textView6=findViewById(R.id.textView6);
        text_wait=findViewById(R.id.text_wait);
        textView13=findViewById(R.id.textView13);
        textView5=findViewById(R.id.textView5);
        text_serverMessage=findViewById(R.id.text_serverMessage);
        text_srifScore=findViewById(R.id.text_srifScore);
        layout_design=findViewById(R.id.layout_design);
        layout_design_pending=findViewById(R.id.layout_design_pending);
        btnTryAgain=findViewById(R.id.btnTryAgain);
        layout_design.setVisibility(View.GONE);
        btnTryAgain.setVisibility(View.GONE);
        layout_design_pending.setVisibility(View.VISIBLE);


        //Toast.makeText(this,borrowerdata.getTietAadhar(), Toast.LENGTH_SHORT).show();

        btnSrifScore=findViewById(R.id.btnSrifScore);
        btnSrifScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnSrifScore.getText().toString().equals("CLOSE")){
                    finish();
                }else if(btnSrifScore.getText().toString().equals("PROCEED")){
                    DataAsyncResponseHandler asyncResponseHandler = new DataAsyncResponseHandler(CrifScore.this, "Data Submitting", "Saving Loan Details") {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            if (statusCode == 200) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CrifScore.this);
                            builder.setTitle("Thanks for choosing us!!");
                            builder.setMessage("Submitted Loan Request has been submitted");
                            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(CrifScore.this, error.getMessage() , Toast.LENGTH_LONG).show();
                        }
                    };
                    (new WebOperations()).postEntity(CrifScore.this, "BreEligibility", "SaveBreEligibility" ,String.valueOf(getJsonForCrif(ficode,creator,amount,emi)), asyncResponseHandler);

                }else{
                    text_wait.setVisibility(View.VISIBLE);
                    text_serverMessage.setText("");
                    layout_design.setVisibility(View.GONE);
                    btnTryAgain.setVisibility(View.GONE);
                    layout_design_pending.setVisibility(View.VISIBLE);
                    layout_design.setVisibility(View.GONE);
                    getCrifScore(checkCrifData);
                }

               /* text_wait.setVisibility(View.VISIBLE);
                text_serverMessage.setText("");
                btnTryAgain.setVisibility(View.GONE);
                progressBarsmall.setVisibility(View.VISIBLE);
                checkCrifScore(borrowerdata);*/

            }
        });

        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_wait.setVisibility(View.VISIBLE);
                text_serverMessage.setText("");
                btnTryAgain.setVisibility(View.GONE);
                progressBarsmall.setVisibility(View.VISIBLE);
                checkCrifScore();

            }
        });
        checkCrifScore();


        String[] arraySpinner = new String[] {
                "UCO", "BOB","PNB","SBI"
        };


        Spinner s = (Spinner) findViewById(R.id.spinSelectBank);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        int spinnerBankPos=adapter.getPosition(sharedPreferences.getString("Bank",""));
        s.setSelection(spinnerBankPos);


        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putString("Bank",parent.getSelectedItem().toString());
                editor.apply();
                btnSrifScore.setText("TRY AGAIN");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private JsonObject getJsonForCrif(String ficode, String creator, String amount, String emi) {
        JsonObject jsonObject=new JsonObject();
        jsonObject.addProperty("Ficode",ficode);
        jsonObject.addProperty("Creator",creator);
        jsonObject.addProperty("Loan_Amt",amount);
        jsonObject.addProperty("Emi",emi);
        return jsonObject;
    }

    private void checkCrifScore(){
        //String address=borrowerdata.getTietAddress1()+" "+borrowerdata.getTietAddress2()+" "+borrowerdata.getTietAddress3();
        ApiInterface apiInterface= ApiClient.getClient("https://agra.paisalo.in:8462/creditmatrix/api/").create(ApiInterface.class);
        Log.d("TAG", "checkCrifScore: "+getJsonOfKyc());
        Call<CheckCrifData> call=apiInterface.checkCrifScore(getJsonOfKyc());
        call.enqueue(new Callback<CheckCrifData>() {
            @Override
            public void onResponse(Call<CheckCrifData> call, Response<CheckCrifData> response) {
                Log.d("TAG", "onResponse: "+response.body());
                if(response.body() != null){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkCrifData=response.body();
                            getCrifScore(checkCrifData);
                        }
                    },25000);
                }else{
                    layout_design.setVisibility(View.GONE);
                    layout_design_pending.setVisibility(View.VISIBLE);
                    text_serverMessage.setText("Server Error!!");
                    btnTryAgain.setVisibility(View.VISIBLE);
                    text_wait.setVisibility(View.GONE);

                }



            }

            @Override
            public void onFailure(Call<CheckCrifData> call, Throwable t) {
                Log.d("TAG", "onFailure: "+t.getMessage());
                layout_design.setVisibility(View.GONE);
                progressBarsmall.setVisibility(View.GONE);
                layout_design_pending.setVisibility(View.VISIBLE);
                btnTryAgain.setVisibility(View.VISIBLE);
                text_serverMessage.setText(t.getMessage());
                text_wait.setVisibility(View.GONE);
            }
        });
    }


    private void getCrifScore(CheckCrifData checkCrifData) {
        //String address=borrowerdata.getTietAddress1()+" "+borrowerdata.getTietAddress2()+" "+borrowerdata.getTietAddress3();
        ApiInterface apiInterface= ApiClient.getClient("https://agra.paisalo.in:8462/creditmatrix/api/").create(ApiInterface.class);
        Call<ScrifData> call=apiInterface.getCrifScore(getJSOnOfCheckDataResponse(checkCrifData));
        call.enqueue(new Callback<ScrifData>() {
            @Override
            public void onResponse(Call<ScrifData> call, Response<ScrifData> response) {
                Log.d("TAG", "onResponse: "+response.body());
                if(response.body() != null){
                    ScrifData scrifData=response.body();
                    String data=scrifData.getData();
                    if(data == null){
                        layout_design.setVisibility(View.GONE);
                        layout_design_pending.setVisibility(View.VISIBLE);
                        text_serverMessage.setText("Not Eligible. Please try Again!!");
                        btnTryAgain.setVisibility(View.VISIBLE);
                        text_wait.setVisibility(View.GONE);
                        progressBarsmall.setVisibility(View.GONE);
                    }else{
                        if (data.equals("0")){
                          /* // Toast.makeText(CrifScore.this, ""+scrifData.getMessage(), Toast.LENGTH_SHORT).show();
                            layout_design.setVisibility(View.GONE);
                            layout_design_pending.setVisibility(View.VISIBLE);
                            text_serverMessage.setText(scrifData.getMessage());
                            btnTryAgain.setVisibility(View.VISIBLE);
                            text_wait.setVisibility(View.GONE);
                            progressBarsmall.setVisibility(View.GONE);*/
                            message=scrifData.getMessage();
                            gifImageView.setImageResource(R.drawable.crosssign);
                            textView8.setText("Sorry!!");
                            textView8.setTextColor(ContextCompat.getColor(CrifScore.this,R.color.red));
                            textView7.setText(message);
                            textView13.setVisibility(View.GONE);
                            textView6.setVisibility(View.GONE);
                            textView_valueEmi.setVisibility(View.GONE);
                            textView_emi.setVisibility(View.GONE);
                            btnSrifScore.setText("TRY AGAIN");
                            layout_design.setVisibility(View.VISIBLE);
                            layout_design_pending.setVisibility(View.GONE);

                        }else {
                            message=scrifData.getMessage();
                            String[] dataSplitString=data.split("_");
                            amount=dataSplitString[0];
                            emi=dataSplitString[1];
                            score=dataSplitString[2];
                            scrifScore=Integer.parseInt(score);
                            text_srifScore.setText(score);
                            textView5.setText(score);


                            if(Double.parseDouble(amount)>0){
                                gifImageView.setImageResource(R.drawable.checksign);
                                textView8.setText("Congrats!!");
                                textView8.setTextColor(ContextCompat.getColor(CrifScore.this,R.color.green));
                                textView7.setText(message);
                                textView13.setVisibility(View.VISIBLE);
                                textView6.setVisibility(View.VISIBLE);

                                textView_emi.setVisibility(View.VISIBLE);
                                textView_valueEmi.setVisibility(View.VISIBLE);
                                textView6.setText(amount+" ₹");
                                textView_valueEmi.setText(emi+" ₹");
                                btnSrifScore.setText("PROCEED");
                            }else{
                                gifImageView.setImageResource(R.drawable.crosssign);
                                textView8.setText("Sorry!!");
                                textView8.setTextColor(ContextCompat.getColor(CrifScore.this,R.color.red));
                                textView7.setText(message);
                                textView13.setVisibility(View.GONE);
                                textView6.setVisibility(View.GONE);
                                textView_valueEmi.setVisibility(View.GONE);
                                textView_emi.setVisibility(View.GONE);
                                btnSrifScore.setText("TRY AGAIN");

                            }


                            progressBar.setMax(1000);
                            progressBar.setProgress(0);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i=0;i<=scrifScore;i++){
                                        progressBar.setProgress(i);
                                        try {
                                            sleep(10);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }).start();
                            layout_design.setVisibility(View.VISIBLE);
                            layout_design_pending.setVisibility(View.GONE);                        }

                    }
                }else{
                    layout_design.setVisibility(View.GONE);
                    layout_design_pending.setVisibility(View.VISIBLE);
                    text_serverMessage.setText("Server Error!!");
                    btnTryAgain.setVisibility(View.VISIBLE);
                    text_wait.setVisibility(View.GONE);
                }



            }

            @Override
            public void onFailure(Call<ScrifData> call, Throwable t) {
                Log.d("TAG", "onFailure: "+t.getMessage());
                layout_design.setVisibility(View.GONE);
                progressBarsmall.setVisibility(View.GONE);
                layout_design_pending.setVisibility(View.VISIBLE);
                btnTryAgain.setVisibility(View.VISIBLE);
                text_serverMessage.setText(t.getMessage());
                text_wait.setVisibility(View.GONE);
            }
        });
    }

    private JsonObject getJSOnOfCheckDataResponse(CheckCrifData checkCrifData) {
       JsonObject jsonObject=new JsonObject();
       jsonObject.addProperty("fiCode",checkCrifData.getData().getFiCode());
       jsonObject.addProperty("creator",checkCrifData.getData().getCreator());
       jsonObject.addProperty("err_code",checkCrifData.getData().getErrCode());
       jsonObject.addProperty("is_success",checkCrifData.getData().getIsSuccess());
       jsonObject.addProperty("message",checkCrifData.getData().getMessage());
       jsonObject.addProperty("bank",sharedPreferences.getString("Bank",""));
       jsonObject.addProperty("duration",checkCrifData.getData().getDuration());
       jsonObject.addProperty("income",checkCrifData.getData().getIncome());
       jsonObject.addProperty("dob",checkCrifData.getData().getDob());
       jsonObject.addProperty("expense",checkCrifData.getData().getExpense());
       jsonObject.addProperty("loan_amount",checkCrifData.getData().getLoanAmount());
     //  Log.e("BANK",sharedPreferences.getString("Bank",""));
       return  jsonObject;
    }


    private JsonObject getJsonOfKyc() {
       JsonObject jsonObject=new JsonObject();
       jsonObject.addProperty("ficode",ficode);
       jsonObject.addProperty("full_name",eSignerborower.PartyName);
       jsonObject.addProperty("dob",eSignerborower.D);
       jsonObject.addProperty("co",eSignerborower.FatherName);
       jsonObject.addProperty("address",eSignerborower.Address);
       jsonObject.addProperty("city",eSignerborower.C);
       jsonObject.addProperty("state",eSignerborower.S);
       jsonObject.addProperty("pin",eSignerborower.P);
       jsonObject.addProperty("loan_amount",eSignerborower.SanctionedAmt);
       jsonObject.addProperty("mobile",eSignerborower.MobileNo);
       jsonObject.addProperty("creator",creator);
       jsonObject.addProperty("pancard",eSignerborower.P);
       jsonObject.addProperty("voter_id",eSignerborower.V);
       jsonObject.addProperty("BrCode",eSignerborower.FoCode);
       jsonObject.addProperty("GrpCode",eSignerborower.CityCode);
       jsonObject.addProperty("AadharID",eSignerborower.AadharNo);
       jsonObject.addProperty("Gender",eSignerborower.G);
       jsonObject.addProperty("Bank",eSignerborower.B);
       jsonObject.addProperty("Income",eSignerborower.I);
       jsonObject.addProperty("Expense",eSignerborower.E);
       jsonObject.addProperty("LoanReason",eSignerborower.R);
       jsonObject.addProperty("Duration",eSignerborower.Period);
       return jsonObject;

    }
    public static String getRandomSixNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }

    public static String getRandomTwoNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(99);

        // this will convert any number sequence into 6 character.
        return String.format("%02d", number);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}