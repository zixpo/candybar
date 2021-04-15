package candybar.lib.fragments.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.SkuDetails;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import candybar.lib.R;
import candybar.lib.adapters.dialog.InAppBillingAdapter;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.items.InAppBilling;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.InAppBillingProcessor;
import candybar.lib.utils.listeners.InAppBillingListener;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class InAppBillingFragment extends DialogFragment {

    private ListView mInAppList;
    private ProgressBar mProgress;

    private int mType;
    private String mKey;
    private String[] mProductsId;
    private int[] mProductsCount;

    private InAppBillingAdapter mAdapter;
    private AsyncTask<Void, Void, ?> mAsyncTask;

    private static final String TYPE = "type";
    private static final String KEY = "key";
    private static final String PRODUCT_ID = "product_id";
    private static final String PRODUCT_COUNT = "product_count";

    private static final String TAG = "candybar.dialog.inapp.billing";

    private static InAppBillingFragment newInstance(int type, String key, String[] productId, int[] productCount) {
        InAppBillingFragment fragment = new InAppBillingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        bundle.putString(KEY, key);
        bundle.putStringArray(PRODUCT_ID, productId);
        if (productCount != null)
            bundle.putIntArray(PRODUCT_COUNT, productCount);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showInAppBillingDialog(@NonNull FragmentManager fm,
                                              int type, @NonNull String key, @NonNull String[] productId,
                                              int[] productCount) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = InAppBillingFragment.newInstance(type, key, productId, productCount);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(TYPE);
            mKey = getArguments().getString(KEY);
            mProductsId = getArguments().getStringArray(PRODUCT_ID);
            mProductsCount = getArguments().getIntArray(PRODUCT_COUNT);
        }
    }

    @NonNull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.title(mType == InAppBilling.DONATE ?
                R.string.navigation_view_donate : R.string.premium_request)
                .customView(R.layout.fragment_inapp_dialog, false)
                .typeface(
                        TypefaceHelper.getMedium(getActivity()),
                        TypefaceHelper.getRegular(getActivity()))
                .positiveText(mType == InAppBilling.DONATE ?
                        R.string.donate : R.string.premium_request_buy)
                .negativeText(R.string.close)
                .onPositive((dialog, which) -> {
                    if (mAsyncTask == null) {
                        try {
                            InAppBillingListener listener = (InAppBillingListener) getActivity();
                            listener.onInAppBillingSelected(
                                    mType, mAdapter.getSelectedProduct());
                        } catch (Exception ignored) {
                        }
                        dismiss();
                    }
                })
                .onNegative((dialog, which) ->
                        Preferences.get(getActivity()).setInAppBillingType(-1));
        MaterialDialog dialog = builder.build();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        setCancelable(false);

        mInAppList = (ListView) dialog.findViewById(R.id.inapp_list);
        mProgress = (ProgressBar) dialog.findViewById(R.id.progress);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mType = savedInstanceState.getInt(TYPE);
            mKey = savedInstanceState.getString(KEY);
            mProductsId = savedInstanceState.getStringArray(PRODUCT_ID);
            mProductsCount = savedInstanceState.getIntArray(PRODUCT_COUNT);
        }

        mAsyncTask = new InAppProductsLoader().execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(TYPE, mType);
        outState.putString(KEY, mKey);
        outState.putStringArray(PRODUCT_ID, mProductsId);
        outState.putIntArray(PRODUCT_COUNT, mProductsCount);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDismiss(dialog);
    }

    @SuppressLint("StaticFieldLeak")
    private class InAppProductsLoader extends AsyncTask<Void, Void, Boolean> {

        private InAppBilling[] inAppBillings;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
            inAppBillings = new InAppBilling[mProductsId.length];
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        protected Boolean doInBackground(Void... voids) {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);

                    for (int i = 0; i < mProductsId.length; i++) {
                        SkuDetails product = InAppBillingProcessor.get(getActivity()).getProcessor()
                                .getPurchaseListingDetails(mProductsId[i]);
                        if (product != null) {
                            InAppBilling inAppBilling;
                            String title = product.title.substring(0, product.title.lastIndexOf("("));
                            if (mProductsCount != null) {
                                inAppBilling = new InAppBilling(product.priceText, mProductsId[i],
                                        title, mProductsCount[i]);
                            } else {
                                inAppBilling = new InAppBilling(product.priceText, mProductsId[i],
                                        title);
                            }
                            inAppBillings[i] = inAppBilling;
                        } else {
                            if (i == mProductsId.length - 1)
                                return false;
                        }
                    }
                    return true;
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            mProgress.setVisibility(View.GONE);
            if (aBoolean) {
                mAdapter = new InAppBillingAdapter(getActivity(), inAppBillings);
                mInAppList.setAdapter(mAdapter);
            } else {
                dismiss();
                Preferences.get(getActivity()).setInAppBillingType(-1);

                Toast.makeText(getActivity(), R.string.billing_load_product_failed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
