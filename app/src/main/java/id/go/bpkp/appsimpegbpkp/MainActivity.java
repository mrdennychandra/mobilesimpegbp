package id.go.bpkp.appsimpegbpkp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import id.go.bpkp.appsimpegbpkp.adapter.KaryawanAdapter;
import id.go.bpkp.appsimpegbpkp.http.ApiInterface;
import id.go.bpkp.appsimpegbpkp.http.Karyawan;
import id.go.bpkp.appsimpegbpkp.http.RestClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView list;
    private List<Karyawan> karyawans;
    private KaryawanAdapter adapter;
    private ApiInterface api;
    ProgressDialog mProgressDialog;
    SwipeRefreshLayout swipeLayout;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,FormKaryawanActivity.class);
                startActivity(intent);
            }
        });

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler()
                        .postDelayed(new Runnable() {
                    @Override public void run() {
                        getData();
                    }
                }, 500);
            }
        });

        karyawans = new ArrayList<>();
        adapter = new KaryawanAdapter(this,karyawans);
        api = RestClient.getClient().create(ApiInterface.class);
        list = (RecyclerView) findViewById(R.id.list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
        getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Konfirmasi");
            builder.setMessage("Logout dari aplikasi?");
            builder.setCancelable(false);
            builder.setPositiveButton("logout", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.clear();
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            builder.setNegativeButton("tidak", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.show();

        }

        return super.onOptionsItemSelected(item);
    }

    private void getData(){
        Call<List<Karyawan>> call = api.getKaryawan();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("loading...");
        mProgressDialog.show();
        call.enqueue(new Callback<List<Karyawan>>() {
            @Override
            public void onResponse(Call<List<Karyawan>> call, Response<List<Karyawan>>
                    response) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                karyawans = response.body();
                if(karyawans != null) {
                    adapter = new KaryawanAdapter(MainActivity.this,karyawans);
                    list.setAdapter(adapter);
                    swipeLayout.setRefreshing(false);
                }else{
                    Toast.makeText(MainActivity.this,response.message(),Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Karyawan>> call, Throwable t) {
                Log.e("Retrofit Get", t.toString());
                Toast.makeText(MainActivity.this,t.toString(),Toast.LENGTH_LONG).show();
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

}
