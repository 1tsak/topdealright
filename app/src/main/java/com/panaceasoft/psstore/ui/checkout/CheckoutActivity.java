package com.panaceasoft.psstore.ui.checkout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.panaceasoft.psstore.Config;
import com.panaceasoft.psstore.MainActivity;
import com.panaceasoft.psstore.R;
import com.panaceasoft.psstore.databinding.ActivityCheckoutBinding;
import com.panaceasoft.psstore.ui.common.PSAppCompactActivity;
import com.panaceasoft.psstore.ui.common.PSFragment;
import com.panaceasoft.psstore.utils.Api;
import com.panaceasoft.psstore.utils.Checksum;
import com.panaceasoft.psstore.utils.Constants;
import com.panaceasoft.psstore.utils.MyContextWrapper;
import com.panaceasoft.psstore.utils.PSDialogMsg;
import com.panaceasoft.psstore.utils.Paytm;
import com.panaceasoft.psstore.utils.Utils;
import com.panaceasoft.psstore.viewobject.TransactionObject;
import com.panaceasoft.psstore.viewobject.User;
import com.panaceasoft.psstore.viewobject.holder.TransactionValueHolder;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class CheckoutActivity extends PSAppCompactActivity implements PaytmPaymentTransactionCallback {

    public int number = 1;
    private int maxNumber = 4;
    User user;
    PSFragment fragment;
    public ActivityCheckoutBinding binding;
    public ProgressDialog progressDialog;
    private PSDialogMsg psDialogMsg;
    public TransactionValueHolder transactionValueHolder;
    public TransactionObject transactionObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_checkout);

        // Init all UI
        initUI(binding);

        progressDialog = new ProgressDialog(this);

        progressDialog.setCancelable(false);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE__MAIN_ACTIVITY
                && resultCode == Constants.RESULT_CODE__RESTART_MAIN_ACTIVITY) {

            finish();
            startActivity(new Intent(this, MainActivity.class));

        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String CURRENT_LANG_CODE = preferences.getString(Constants.LANGUAGE_CODE, Config.DEFAULT_LANGUAGE);
        String CURRENT_LANG_COUNTRY_CODE = preferences.getString(Constants.LANGUAGE_COUNTRY_CODE, Config.DEFAULT_LANGUAGE_COUNTRY_CODE);

        super.attachBaseContext(MyContextWrapper.wrap(newBase, CURRENT_LANG_CODE, CURRENT_LANG_COUNTRY_CODE, true));
    }

    private void initUI(ActivityCheckoutBinding binding) {

        transactionValueHolder = new TransactionValueHolder();

        psDialogMsg = new PSDialogMsg(this, false);

        // click close image button
        binding.closeImageButton.setOnClickListener(view -> finish());

        // fragment1 default initialize
        navigateFragment(binding, number);

        binding.nextButton.setOnClickListener(view -> {
            if (number < maxNumber) {
                number++;
                if (number == 3) {

                    navigateFragment(binding, number);

                } else if (number == 2) {

                    if (user.city.id.isEmpty()) {
                        psDialogMsg.showErrorDialog(getString(R.string.error_message__select_city), getString(R.string.app__ok));
                        psDialogMsg.show();
                        number--;
                    }else if (((CheckoutFragment1)fragment).checkShippingAddressEditTextIsEmpty()) {
                        psDialogMsg.showErrorDialog(getString(R.string.shipping_address_one_error_message), getString(R.string.app__ok));
                        psDialogMsg.show();
                        number--;
                    } else if (((CheckoutFragment1)fragment).checkBillingAddressEditTextIsEmpty()) {
                        psDialogMsg.showErrorDialog(getString(R.string.billing_address_one_error_message), getString(R.string.app__ok));
                        psDialogMsg.show();
                        number--;
                    } else if (((CheckoutFragment1)fragment).checkUserEmailEditTextIsEmpty()) {
                        psDialogMsg.showErrorDialog(getString(R.string.checkout__user_email_empty), getString(R.string.app__ok));
                        psDialogMsg.show();
                        number--;
                    }
                    else {
                        ((CheckoutFragment1) fragment).updateUserProfile();
                    }


                } else if (number == 4) {

                    if (((CheckoutFragment3) fragment).binding.get().checkBox.isChecked()) {

                        number--;

                        if (((CheckoutFragment3) fragment).clicked) {

                            psDialogMsg.showConfirmDialog(getString(R.string.confirm_to_Buy), getString(R.string.app__ok), getString(R.string.app__cancel));
                            psDialogMsg.show();

                            psDialogMsg.okButton.setOnClickListener(v -> {

                                psDialogMsg.cancel();

                                switch (((CheckoutFragment3) fragment).paymentMethod) {
                                    case Constants.PAYMENT_PAYPAL:

                                        ((CheckoutFragment3) fragment).getToken();

                                        break;

                                    case Constants.PAYMENT_CASH_ON_DELIVERY:

                                        ((CheckoutFragment3) fragment).sendData();

                                        break;

                                    case Constants.PAYMENT_STRIPE:

                                        navigationController.navigateToStripeActivity(CheckoutActivity.this);

                                        break;

                                    case Constants.PAYMENT_BANK:
                                        ((CheckoutFragment3) fragment).sendData();

                                        break;
                                    case Constants.PAYMENT_RAZOR:
                                        navigationController.navigateToRazorActivity(CheckoutActivity.this,(Math.round(transactionValueHolder.final_total)));
                                        break;

                                    case Constants.PAYMENT_PAYTM:
                                        generateCheckSum(Math.round(transactionValueHolder.final_total));
                                        break;
                                }

                            });

                            psDialogMsg.cancelButton.setOnClickListener(v -> psDialogMsg.cancel());
                        } else {

                            psDialogMsg.showErrorDialog(getString(R.string.checkout__choose_a_method), getString(R.string.app__ok));
                            psDialogMsg.show();
                        }

                    } else {

                        number--;

                        psDialogMsg.showInfoDialog(getString(R.string.not_checked), getString(R.string.app__ok));
                        psDialogMsg.show();

                    }

                } else {

                    navigateFragment(binding, number);
                }

            }
        });

        binding.prevButton.setOnClickListener(view -> {
            if (number > 1) {
                number--;
                binding.shippingImageView.setImageResource(R.drawable.baseline_circle_line_uncheck_24);
                binding.paymentImageView.setImageResource(R.drawable.baseline_circle_black_uncheck_24);
                navigateFragment(binding, number);
            }

        });

        binding.keepShoppingButton.setOnClickListener(v -> {

            navigationController.navigateBackToBasketActivity(CheckoutActivity.this);
            CheckoutActivity.this.finish();

        });

    }

    public void navigateFragment(ActivityCheckoutBinding binding, int position) {
        updateCheckoutUI(binding);

        if (position == 1) {

            CheckoutFragment1 fragment = new CheckoutFragment1();
            this.fragment = fragment;
            setupFragment(fragment);

        } else if (position == 2) {

            CheckoutFragment2 fragment = new CheckoutFragment2();
            this.fragment = fragment;
            setupFragment(fragment);

        } else if (position == 3) {

            CheckoutFragment3 fragment = new CheckoutFragment3();
            this.fragment = fragment;
            setupFragment(fragment);

        } else if (position == 4) {
            setupFragment(new CheckoutStatusFragment());
        }
    }

    private void updateCheckoutUI(ActivityCheckoutBinding binding) {
        if (number == 1) {
            binding.nextButton.setVisibility(View.VISIBLE);
            binding.prevButton.setVisibility(View.GONE);
            binding.keepShoppingButton.setVisibility(View.GONE);
            binding.step2View.setBackgroundColor(getResources().getColor(R.color.md_grey_300));
            binding.step3View.setBackgroundColor(getResources().getColor(R.color.md_grey_300));
            binding.nextButton.setText(R.string.checkout__next);

        } else if (number == 2) {

            binding.nextButton.setVisibility(View.VISIBLE);
            binding.prevButton.setVisibility(View.VISIBLE);
            binding.step2View.setBackgroundColor(getResources().getColor(R.color.global__primary));
            binding.step3View.setBackgroundColor(getResources().getColor(R.color.md_grey_300));
            binding.keepShoppingButton.setVisibility(View.GONE);
            binding.paymentImageView.setImageResource(R.drawable.baseline_circle_line_uncheck_24);
            binding.shippingImageView.setImageResource(R.drawable.baseline_circle_line_check_24);

            binding.nextButton.setText(R.string.checkout__next);
            binding.prevButton.setText(R.string.back);

        } else if (number == 3) {
            binding.nextButton.setVisibility(View.VISIBLE);
            binding.prevButton.setVisibility(View.VISIBLE);
            binding.keepShoppingButton.setVisibility(View.GONE);
            binding.step3View.setBackgroundColor(getResources().getColor(R.color.global__primary));
            binding.paymentImageView.setImageResource(R.drawable.baseline_circle_line_check_24);
            binding.successImageView.setImageResource(R.drawable.baseline_circle_line_uncheck_24);

            binding.nextButton.setText(R.string.checkout);
            binding.prevButton.setText(R.string.back);
        } else if (number == 4) {
            binding.constraintLayout25.setVisibility(View.GONE);
            binding.nextButton.setVisibility(View.GONE);
            binding.prevButton.setVisibility(View.GONE);
            binding.keepShoppingButton.setVisibility(View.VISIBLE);
            binding.paymentImageView.setImageResource(R.drawable.baseline_circle_line_check_24);
            binding.successImageView.setImageResource(R.drawable.baseline_circle_line_check_24);
        }

    }

    public void setCurrentUser(User user) {
        this.user = user;
    }

    public User getCurrentUser() {
        return this.user;
    }

    public void goToFragment4() {
        navigateFragment(binding, 4);
        number = 4;
    }

    private void generateCheckSum(int txnAmount) {


        //creating a retrofit object.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //creating the retrofit api service
        Api apiService = retrofit.create(Api.class);

        //creating paytm object
        //containing all the values required
        final Paytm paytm = new Paytm(
                Config.M_ID,
                Config.CHANNEL_ID,
                String.valueOf(txnAmount),
                Config.WEBSITE,
                Config.CALLBACK_URL,
                Config.INDUSTRY_TYPE_ID
        );

        //creating a call object from the apiService
        Call<Checksum> call = apiService.getChecksum(
                paytm.getmId(),
                paytm.getOrderId(),
                paytm.getCustId(),
                paytm.getChannelId(),
                paytm.getTxnAmount(),
                paytm.getWebsite(),
                paytm.getCallBackUrl(),
                paytm.getIndustryTypeId()
        );

        //making the call to generate checksum
        call.enqueue(new Callback<Checksum>() {
            @Override
            public void onResponse(Call<Checksum> call, Response<Checksum> response) {

                //once we get the checksum we will initiailize the payment.
                //the method is taking the checksum we got and the paytm object as the parameter
                initializePaytmPayment(response.body().getChecksumHash(), paytm);
            }

            @Override
            public void onFailure(Call<Checksum> call, Throwable t) {

            }
        });
    }

    private void initializePaytmPayment(String checksumHash, Paytm paytm) {

        //getting paytm service
        PaytmPGService Service = PaytmPGService.getProductionService();

        //use this when using for production
        //PaytmPGService Service = PaytmPGService.getProductionService();

        //creating a hashmap and adding all the values required
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("MID", Config.M_ID);
        paramMap.put("ORDER_ID", paytm.getOrderId());
        paramMap.put("CUST_ID", paytm.getCustId());
        paramMap.put("CHANNEL_ID", paytm.getChannelId());
        paramMap.put("TXN_AMOUNT", paytm.getTxnAmount());
        paramMap.put("WEBSITE", paytm.getWebsite());
        paramMap.put("CALLBACK_URL", paytm.getCallBackUrl());
        paramMap.put("CHECKSUMHASH", checksumHash);
        paramMap.put("INDUSTRY_TYPE_ID", paytm.getIndustryTypeId());


        //creating a paytm order object using the hashmap
        PaytmOrder order = new PaytmOrder(paramMap);

        //intializing the paytm service
        Service.initialize(order, null);

        //finally starting the payment transaction
        Service.startPaymentTransaction(this, true, true, this);

    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onTransactionResponse(Bundle inResponse) {

        try {
            String statusSt = inResponse.getString("STATUS");

            if (statusSt.equalsIgnoreCase("TXN_SUCCESS")) {

//                orderIdSt = inResponse.getString("ORDERID");
//                txnIdSt = inResponse.getString("TXNID");

                // Loading jsonarray in Background Thread
                //addTransactionDetails(orderIdSt, txnIdSt);

                ((CheckoutFragment3) fragment).paytmPaid(inResponse.getString("ORDERID"));


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public void networkNotAvailable() {
        Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
    }

    @Override
    public void clientAuthenticationFailed(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void someUIErrorOccurred(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onErrorLoadingWebPage(int i, String s, String s1) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressedCancelTransaction() {
        Toast.makeText(this, "Back Pressed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTransactionCancel(String s, Bundle bundle) {
        Toast.makeText(this, s + bundle.toString(), Toast.LENGTH_LONG).show();
    }
}
