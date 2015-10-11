package com.beacon.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.beacon.server.ServerHandler;
import com.beacon.util.RoundImage;
import com.example.saravanan.beaconsample.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {
    String serverURL = "http://beaconservice.elasticbeanstalk.com/getuserprofile";
    RoundImage roundedImage;
    ImageView imageView;
    ArrayAdapter<String> adapter = null;
    ListView listView = null;

    private String customerFirstName;
    private String customerLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        imageView = (ImageView) findViewById(R.id.imageView);

        /*imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.my_photo);
        roundedImage = new RoundImage(bm);
        imageView.setImageDrawable(roundedImage);*/

       /* imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.my_photo);
        imageView.setImageBitmap(icon);*/

        String []values = new String[0];
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(WelcomeActivity.this, CustomerServiceActivity.class);
                i.putExtra("customerFirstName", customerFirstName);
                i.putExtra("customerLastName", customerLastName);
                startActivity(i);
            }
        });

        new LongOperation().execute(serverURL);
    }

    private class LongOperation  extends AsyncTask<String, Void, Void> {
        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(WelcomeActivity.this);
        String result = "";

        protected void onPreExecute() {
            Log.d("Content ", "Starting onPreExecute  ");
            Dialog.setMessage("Please wait..");
            Dialog.show();
        }

        protected Void doInBackground(String... urls) {
            Log.d("Content ", "Starting doInBackground  ");
            ServerHandler serverHandler = new ServerHandler();
            result = serverHandler.makeServiceCall(serverURL, 1, new ArrayList<NameValuePair>());
            return null;
        }

        protected void onPostExecute(Void unused) {
            Log.d("Content ", "Starting onPostExecute  ");
            Dialog.dismiss();
            if (Error != null) {
                Log.e("Error ", Error);
            } else {
                parseJSon();
            }
        }


        private void parseJSon(){
            try {
                //String jsonString = "{\"userProfile\":{\"deviceId\":\"dev-10000\",\"userId\":\"10000\",\"firstName\":\"saravanan\",\"lastName\":\"shanmugavel\",\"bankCode\":\"B1\"},\"bankService\":{\"serviceList\":[{\"serviceName\":\"Personal Banking\"},{\"serviceName\":\"Investment Banking\"},{\"serviceName\":\"Insurance\"},{\"serviceName\":\"Others\"}]}}";
                JSONObject jsonObj = new JSONObject(result);
                JSONObject userProfile = jsonObj.getJSONObject("userProfile");
               /* String deviceID = userProfile.getString("deviceId");
                String userId = userProfile.getString("userId");*/
                customerFirstName  = userProfile.getString("firstName");
                customerLastName  = userProfile.getString("lastName");

                TextView welcomeText = (TextView) findViewById(R.id.welcometext);
                welcomeText.setText("Hi " + customerFirstName + " " + customerLastName + " welcome! Please fnd the list of services");

                byte[] imgBytes =Base64.decode(userProfile.getString("imageBytes").getBytes(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
                roundedImage = new RoundImage(bitmap);
                imageView.setImageDrawable(roundedImage);

                JSONObject bankService = jsonObj.getJSONObject("bankService");
                JSONArray array = bankService.getJSONArray("serviceList");
                String []values = new String[array.length()];
                for(int i = 0; i < array.length(); i++){
                    JSONObject serviceName = array.getJSONObject(i);
                    values[i] = serviceName.getString("serviceName");
                }
                adapter = new ArrayAdapter<String>(WelcomeActivity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, values);
                listView.setAdapter(adapter);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
