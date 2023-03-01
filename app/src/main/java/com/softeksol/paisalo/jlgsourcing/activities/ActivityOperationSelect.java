package com.softeksol.paisalo.jlgsourcing.activities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.BackoffPolicy;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.DataAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.softeksol.paisalo.jlgsourcing.Global;
import com.softeksol.paisalo.jlgsourcing.R;
import com.softeksol.paisalo.jlgsourcing.SEILIGL;
import com.softeksol.paisalo.jlgsourcing.Utilities.IglPreferences;
import com.softeksol.paisalo.jlgsourcing.WebOperations;
import com.softeksol.paisalo.jlgsourcing.adapters.AdapterListManager;
import com.softeksol.paisalo.jlgsourcing.adapters.AdapterOperation;
import com.softeksol.paisalo.jlgsourcing.entities.Manager;
import com.softeksol.paisalo.jlgsourcing.entities.RangeCategory;
import com.softeksol.paisalo.jlgsourcing.entities.dto.OperationItem;
import com.softeksol.paisalo.jlgsourcing.handlers.DataAsyncResponseHandler;
import com.softeksol.paisalo.jlgsourcing.location.LocationJobService;
import com.softeksol.paisalo.jlgsourcing.location.LocationWork;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

public class ActivityOperationSelect extends AppCompatActivity {
    private AdapterListManager adapterListManager;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    private static final int PERMISSION_REQUEST_CODE = 200;

    private static final String TAG = "LocationUpdate";
    boolean registered = false, isServiceStarted=false;
    public static final String JOB_STATE_CHANGED = "jobStateChanged";
    public static final String LOCATION_ACQUIRED = "locAcquired";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation_select);
        drawerLayout = findViewById(R.id.my_drawer_layout);
        //startLocationWork();
      //  startBackgroundService();
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close){
            public void onDrawerClosed(View view)
            {
                Log.d("drawerToggle", "Drawer closed");
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); //Creates call to onPrepareOptionsMenu()
            }

            /*
             * Called when a drawer has settled in a completely open state
             */
            public void onDrawerOpened(View drawerView)
            {
                Log.d("drawerToggle", "Drawer opened");
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_dehaze_24);
        List<OperationItem> operationItems = new ArrayList<>();
        if (IglPreferences.getPrefString(this, SEILIGL.ALLOW_COLLECTION, "N").contains("S")) {
            operationItems.add(new OperationItem(1, "KYC", R.color.colorMenuKyc, "POSDB", "Getmappedfo"));
            operationItems.add(new OperationItem(2, "Application Form", R.color.colorMenuApplicationForm, "POSDB", "Getmappedfo"));
        }
        if (IglPreferences.getPrefString(this, SEILIGL.ALLOW_COLLECTION, "N").contains("C")) {
            operationItems.add(new OperationItem(3, "Collection", R.color.colorMenuCollection, "POSDB", "Getmappedfoforcoll"));
        }
        if (IglPreferences.getPrefString(this, SEILIGL.ALLOW_COLLECTION, "N").contains("D")) {
            operationItems.add(new OperationItem(4, "Deposit", R.color.colorMenuDeposit, "POSDB", "Getmappedfofordepo"));
        }
        if (IglPreferences.getPrefString(this, SEILIGL.ALLOW_COLLECTION, "N").contains("P")) {
            operationItems.add(new OperationItem(5, "Premature", R.color.colorMenuPremature, "casepremature", "FOForPreclosure"));
        }
        if (IglPreferences.getPrefString(this, SEILIGL.ALLOW_COLLECTION, "N").contains("C")) {
            operationItems.add(new OperationItem(6, "E-Sign", R.color.colorMenuPremature, "POSDB", "Getmappedfo"));
        }

        GridView lv = (GridView) findViewById(R.id.lvcOpSelect);
        lv.setAdapter(new AdapterOperation(ActivityOperationSelect.this, operationItems));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OperationItem operationItem = (OperationItem) parent.getAdapter().getItem(position);
                updateManagers(operationItem);
            }
        });
    }


    private void updateManagers(final OperationItem operationItem) {
        //if (WebOperations.checkSession(this)) {
        DataAsyncResponseHandler dataAsyncHttpResponseHandler = new DataAsyncResponseHandler(this, "Loan Financing", "Downloading Manager's List") {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String jsonString = new String(responseBody);
                Log.d("ManageDataLatest", jsonString);
                Type listType = new TypeToken<List<Manager>>() {
                }.getType();

                List<Manager> managers = WebOperations.convertToObjectArray(jsonString, listType);
                Log.d("CheckSizeList",managers.size()+"");

                SQLite.delete().from(Manager.class).query();
                for (Manager manager : managers) {
                    manager.insert();
                }

                Intent intent = null;
                switch (operationItem.getId()) {
                    case 1:
                        intent = new Intent(ActivityOperationSelect.this, ActivityManagerSelect.class);
                        break;
                    case 2:
                        intent = new Intent(ActivityOperationSelect.this, ActivityManagerSelect.class);
                        break;
                    case 3:
                        intent = new Intent(ActivityOperationSelect.this, ActivityManagerSelect.class);
                        break;
                    case 4:
                        intent = new Intent(ActivityOperationSelect.this, ActivityManagerSelect.class);
                        break;
                    case 5:
                        intent = new Intent(ActivityOperationSelect.this, ActivityManagerSelect.class);
                        break;
                    case 6:
                        intent = new Intent(ActivityOperationSelect.this, ActivityManagerSelect.class);
                        break;
                }

                assert intent != null;
                intent.putExtra(Global.OPTION_ITEM, operationItem);
                intent.putExtra("Title", operationItem.getOprationName());
                startActivity(intent);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("failure", String.valueOf(statusCode) + "\n" + (new String(responseBody, StandardCharsets.UTF_8)));
            }
        };
        RequestParams params = new RequestParams();
        params.add("UserId", IglPreferences.getPrefString(this, SEILIGL.USER_ID, ""));
        params.add("IMEINO", IglPreferences.getPrefString(this, SEILIGL.DEVICE_IMEI, "0"));
        //params.add("Operation", operationItem.getUrlEndpoint());
        (new WebOperations()).getEntity(this, operationItem.getUrlController(), operationItem.getUrlEndpoint(), params, dataAsyncHttpResponseHandler);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        return actionBarDrawerToggle.onOptionsItemSelected(item);
    }


/*
    private  void startLocationWork(){

        // START Worker
        PeriodicWorkRequest periodicWork = new PeriodicWorkRequest.Builder(LocationWork.class, 15, TimeUnit.MINUTES)
                .addTag(TAG)
                .build();
        WorkManager.getInstance().enqueueUniquePeriodicWork("Location", ExistingPeriodicWorkPolicy.REPLACE, periodicWork);

        Toast.makeText(ActivityOperationSelect.this, "Location Worker Started : " + periodicWork.getId(), Toast.LENGTH_SHORT).show();


       */
/* OneTimeWorkRequest backGroundWork=new OneTimeWorkRequest.Builder(LocationWork.class)
                .addTag("Location")
                .setBackoffCriteria(BackoffPolicy.LINEAR,OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();*//*



    }
*/



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startBackgroundService() {
        if(!registered) {
            IntentFilter i = new IntentFilter(JOB_STATE_CHANGED);
            i.addAction(LOCATION_ACQUIRED);
            LocalBroadcastManager.getInstance(this).registerReceiver(jobStateChanged, i);
        }
        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        assert jobScheduler != null;
        jobScheduler.schedule(new JobInfo.Builder(LocationJobService.LOCATION_SERVICE_JOB_ID,
                new ComponentName(this, LocationJobService.class))
                .setOverrideDeadline(100)
                .setPersisted(true)
                .setRequiresDeviceIdle(false)
                .build());
    }

    private BroadcastReceiver jobStateChanged = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction()==null){
                return;
            }
            if(intent.getAction().equals(JOB_STATE_CHANGED)) {
               // changeServiceButton(intent.getExtras().getBoolean("isStarted"));
            }else if (intent.getAction().equals(LOCATION_ACQUIRED)){
                if(intent.getExtras()!=null){
                    Bundle b = intent.getExtras();
                    Location l = b.getParcelable("location");
                   // updateMarker(l);
                }else{
                    Log.d("intent","null");
                }
            }
        }
    };


}
