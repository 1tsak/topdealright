package com.panaceasoft.psstore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.panaceasoft.psstore.ui.common.NavigationController;
import com.panaceasoft.psstore.viewobject.holder.TransactionValueHolder;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.Random;

public class RazorPay extends Activity implements PaymentResultListener {

    private static final String TAG ="TAG" ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransactionValueHolder transactionValueHolder = new TransactionValueHolder();

        Intent intent = getIntent();
        int amount = intent.getIntExtra("Amount",0);

        int FinalAmount = amount * 100;

        startPayment(FinalAmount);
    }

    public void startPayment(int amount) {


        Checkout checkout = new Checkout();

        /**
         * Instantiate Checkout
         */

        /**
         * Set your logo here
         */
        checkout.setImage(R.drawable.icon);

        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            /**
             * Merchant Name
             * eg: ACME Corp || HasGeek etc.
             */
            options.put("name", "City Service");

            /**
             * Description can be anything
             * eg: Reference No. #123123 - This order number is passed by you for your internal reference. This is not the `razorpay_order_id`.
             *     Invoice Payment
             *     etc.
             */
            options.put("description", "Reference No. #CityServices"+gen());
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            //options.put("order_id", "order_9A33XWu170gUtm");
            options.put("currency", "INR");

            /**
             * Amount is always passed in currency subunits
             * Eg: "500" = INR 5.00
             */
            options.put("amount", amount);

            checkout.open(activity, options);
        } catch(Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String s) {

        NavigationController navigationController = new NavigationController();

        navigationController.navigateRazorBackToCheckoutFrag(RazorPay.this, String.valueOf(gen()));

      finish();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment Failed,"+""+s+i, Toast.LENGTH_LONG).show();
        finish();

    }
    public int gen() {
        Random r = new Random( System.currentTimeMillis() );
        return 10000000 + r.nextInt(20000000);
    }
}
