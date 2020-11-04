package com.panaceasoft.psstore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.panaceasoft.psstore.ui.product.detail.ProductActivity;
import com.panaceasoft.psstore.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.services.concurrency.AsyncTask;
import rubikstudio.library.LuckyWheelView;
import rubikstudio.library.model.LuckyItem;

public class SpinNWinActivity extends AppCompatActivity {


    private static final String URL_PRIZE = "";
    ArrayList<Bitmap> prizeImages = new ArrayList<Bitmap>();
    ArrayList<String> prizeNames = new ArrayList<String>();
    ArrayList<String> product_id = new ArrayList<String>();
    LuckyWheelView luckyWheelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spin_n_win);
        luckyWheelView = (LuckyWheelView) findViewById(R.id.luckyWheel);

        luckyWheelView.setLuckyRoundItemSelectedListener(new LuckyWheelView.LuckyRoundItemSelectedListener() {
            @Override
            public void LuckyRoundItemSelected(int index) {
                // do something with index
//                if (interstitialAd.isAdLoaded()) interstitialAd.show();
//                addCoins(String.valueOf(index + 1));
                Intent intent = new Intent(SpinNWinActivity.this, ProductActivity.class);
                intent.putExtra(Constants.PRODUCT_ID, product_id.get(index));
                intent.putExtra(Constants.PRODUCT_NAME, prizeNames.get(index));
                intent.putExtra(Constants.HISTORY_FLAG, Constants.ONE);
                intent.putExtra(Constants.BASKET_FLAG, Constants.ZERO);
                startActivity(intent);
            }
        });

        getPrizeItems();
    }

    private void getPrizeItems() {
        //  banners.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_PRIZE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject banner = array.getJSONObject(i);

                                prizeNames.add(banner.getString("title"));
                                product_id.add(banner.getString("Pro_id"));
                                new DownloadPrizeImages().execute(banner.getString("image"));
                                
                            }
                                setupWheel();
                            
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SpinNWinActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        //adding our stringrequest to queue
        Volley.newRequestQueue(SpinNWinActivity.this).add(stringRequest);
    }

    private void setupWheel() {
        List<LuckyItem> spinitems = new ArrayList<>();
        for (int i = 0;i<product_id.size();i++){
            LuckyItem luckyItem = new LuckyItem();
            luckyItem.topText = prizeNames.get(i);
            luckyItem.icon = prizeImages.get(i);
            luckyItem.color = Color.parseColor("#29b6f6");
            spinitems.add(luckyItem);
            luckyWheelView.setData(spinitems);
            luckyWheelView.setRound(product_id.size());
        }
    }

    private class DownloadPrizeImages extends AsyncTask<String, String, Bitmap> {
        Bitmap bmImg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL ImageUrl = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) ImageUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                bmImg = BitmapFactory.decodeStream(is, null, options);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmImg;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            prizeImages.add(bitmap);

        }
    }
}