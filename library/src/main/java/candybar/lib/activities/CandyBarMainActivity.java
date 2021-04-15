package candybar.lib.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.danimahardhika.android.helpers.license.LicenseHelper;
import com.danimahardhika.android.helpers.permission.PermissionCode;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.List;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.databases.Database;
import candybar.lib.fragments.AboutFragment;
import candybar.lib.fragments.ApplyFragment;
import candybar.lib.fragments.FAQsFragment;
import candybar.lib.fragments.HomeFragment;
import candybar.lib.fragments.IconsBaseFragment;
import candybar.lib.fragments.RequestFragment;
import candybar.lib.fragments.SettingsFragment;
import candybar.lib.fragments.WallpapersFragment;
import candybar.lib.fragments.dialog.ChangelogFragment;
import candybar.lib.fragments.dialog.InAppBillingFragment;
import candybar.lib.fragments.dialog.IntentChooserFragment;
import candybar.lib.helpers.ConfigurationHelper;
import candybar.lib.helpers.IntentHelper;
import candybar.lib.helpers.LicenseCallbackHelper;
import candybar.lib.helpers.LocaleHelper;
import candybar.lib.helpers.NavigationViewHelper;
import candybar.lib.helpers.PlaystoreCheckHelper;
import candybar.lib.helpers.RequestHelper;
import candybar.lib.helpers.ThemeHelper;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.helpers.WallpaperHelper;
import candybar.lib.items.Home;
import candybar.lib.items.Icon;
import candybar.lib.items.InAppBilling;
import candybar.lib.items.Request;
import candybar.lib.preferences.Preferences;
import candybar.lib.receivers.CandyBarBroadcastReceiver;
import candybar.lib.services.CandyBarService;
import candybar.lib.services.CandyBarWallpapersService;
import candybar.lib.tasks.IconRequestTask;
import candybar.lib.tasks.IconsLoaderTask;
import candybar.lib.utils.Extras;
import candybar.lib.utils.InAppBillingProcessor;
import candybar.lib.utils.listeners.InAppBillingListener;
import candybar.lib.utils.listeners.RequestListener;
import candybar.lib.utils.listeners.SearchListener;
import candybar.lib.utils.listeners.WallpapersListener;
import candybar.lib.utils.views.HeaderView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

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

public abstract class CandyBarMainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, RequestListener, InAppBillingListener,
        SearchListener, WallpapersListener {

    private TextView mToolbarTitle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private String mFragmentTag;
    private int mPosition, mLastPosition;
    private CandyBarBroadcastReceiver mReceiver;
    private ActionBarDrawerToggle mDrawerToggle;
    private FragmentManager mFragManager;
    private LicenseHelper mLicenseHelper;

    private boolean mIsMenuVisible = true;
    private boolean prevIsDarkTheme;

    public static List<Request> sMissedApps;
    public static List<Icon> sSections;
    public static Home sHomeIcon;
    public static int sInstalledAppsCount;
    public static int sIconsCount;

    private ActivityConfiguration mConfig;

    @NonNull
    public abstract ActivityConfiguration onInit();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LocaleHelper.setLocale(this);

        prevIsDarkTheme = ThemeHelper.isDarkTheme(this);
        super.setTheme(ThemeHelper.isDarkTheme(this) ?
                R.style.AppThemeDark : R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ColorHelper.setupStatusBarIconColor(this);
        ColorHelper.setNavigationBarColor(this, ContextCompat.getColor(this,
                ThemeHelper.isDarkTheme(this) ?
                        R.color.navigationBarDark : R.color.navigationBar));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !ThemeHelper.isDarkTheme(this)) {
            int flags = 0;
            if (ColorHelper.isLightColor(ContextCompat.getColor(this, R.color.navigationBar)))
                flags = flags | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            if (ColorHelper.isLightColor(ContextCompat.getColor(this, R.color.colorPrimaryDark)))
                flags = flags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (flags != 0) {
                this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                this.getWindow().getDecorView().setSystemUiVisibility(flags);
                this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            }
        }

        registerBroadcastReceiver();
        startService(new Intent(this, CandyBarService.class));

        //Todo: wait until google fix the issue, then enable wallpaper crop again on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Preferences.get(this).setCropWallpaper(false);
        }

        mConfig = onInit();
        InAppBillingProcessor.get(this).init(mConfig.getLicenseKey());

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mToolbarTitle = findViewById(R.id.toolbar_title);

        toolbar.setPopupTheme(ThemeHelper.isDarkTheme(this) ?
                R.style.AppThemeDark : R.style.AppTheme);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mFragManager = getSupportFragmentManager();

        initNavigationView(toolbar);
        initNavigationViewHeader();

        mPosition = mLastPosition = 0;
        if (savedInstanceState != null) {
            mPosition = mLastPosition = savedInstanceState.getInt(Extras.EXTRA_POSITION, 0);
            onSearchExpanded(false);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int position = bundle.getInt(Extras.EXTRA_POSITION, -1);
            if (position >= 0 && position < 5) {
                mPosition = mLastPosition = position;
            }
        }

        IntentHelper.sAction = IntentHelper.getAction(getIntent());
        if (IntentHelper.sAction == IntentHelper.ACTION_DEFAULT) {
            setFragment(getFragment(mPosition));
        } else {
            setFragment(getActionFragment(IntentHelper.sAction));
        }

        checkWallpapers();
        IconRequestTask.start(this, AsyncTask.THREAD_POOL_EXECUTOR);
        IconsLoaderTask.start(this);

        new PlaystoreCheckHelper(this).run();

        if (Preferences.get(this).isFirstRun() && mConfig.isLicenseCheckerEnabled()) {
            mLicenseHelper = new LicenseHelper(this);
            mLicenseHelper.run(mConfig.getLicenseKey(), mConfig.getRandomString(), new LicenseCallbackHelper(this));
            return;
        }

        if (!Preferences.get(this).isPlaystoreCheckEnabled() && !mConfig.isLicenseCheckerEnabled()) {
            if (Preferences.get(this).isNewVersion()) {
                ChangelogFragment.showChangelog(mFragManager);
                File cache = this.getCacheDir();
                FileHelper.clearDirectory(cache);
            }
        }

        if (mConfig.isLicenseCheckerEnabled() && !Preferences.get(this).isLicensed()) {
            finish();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (prevIsDarkTheme != ThemeHelper.isDarkTheme(this)) {
            recreate();
            return;
        }
        LocaleHelper.setLocale(this);
        if (mIsMenuVisible) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        int action = IntentHelper.getAction(intent);
        if (action != IntentHelper.ACTION_DEFAULT)
            setFragment(getActionFragment(action));
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        RequestHelper.checkPiracyApp(this);
        IntentHelper.sAction = IntentHelper.getAction(getIntent());
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        InAppBillingProcessor.get(this).destroy();

        if (mLicenseHelper != null) {
            mLicenseHelper.destroy();
        }

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        CandyBarMainActivity.sMissedApps = null;
        CandyBarMainActivity.sHomeIcon = null;
        stopService(new Intent(this, CandyBarService.class));
        Database.get(this.getApplicationContext()).closeDatabase();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Extras.EXTRA_POSITION, mPosition);
        Database.get(this.getApplicationContext()).closeDatabase();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mFragManager.getBackStackEntryCount() > 0) {
            clearBackStack();
            return;
        }

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        if (!mFragmentTag.equals(Extras.TAG_HOME)) {
            mPosition = mLastPosition = 0;
            setFragment(getFragment(mPosition));
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!InAppBillingProcessor.get(this).handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionCode.STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
                return;
            }
            Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPiracyAppChecked(boolean isPiracyAppInstalled) {
        MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.navigation_view_request);
        if (menuItem != null) {
            menuItem.setVisible(getResources().getBoolean(
                    R.bool.enable_icon_request) || !isPiracyAppInstalled);
        }
    }

    @Override
    public void onRequestSelected(int count) {
        if (mFragmentTag.equals(Extras.TAG_REQUEST)) {
            String title = getResources().getString(R.string.navigation_view_request);
            if (count > 0) title += " (" + count + ")";
            mToolbarTitle.setText(title);
        }
    }

    @Override
    public void onBuyPremiumRequest() {
        if (Preferences.get(this).isPremiumRequest()) {
            RequestHelper.showPremiumRequestStillAvailable(this);
            return;
        }

        if (InAppBillingProcessor.get(this.getApplicationContext())
                .getProcessor().loadOwnedPurchasesFromGoogle()) {
            List<String> products = InAppBillingProcessor.get(this).getProcessor().listOwnedProducts();
            if (products != null) {
                boolean isProductIdExist = false;
                for (String product : products) {
                    for (String premiumRequestProductId : mConfig.getPremiumRequestProductsId()) {
                        if (premiumRequestProductId.equals(product)) {
                            isProductIdExist = true;
                            break;
                        }
                    }
                }

                if (isProductIdExist) {
                    RequestHelper.showPremiumRequestExist(this);
                    return;
                }
            }
        }

        InAppBillingFragment.showInAppBillingDialog(getSupportFragmentManager(),
                InAppBilling.PREMIUM_REQUEST,
                mConfig.getLicenseKey(),
                mConfig.getPremiumRequestProductsId(),
                mConfig.getPremiumRequestProductsCount());
    }

    @Override
    public void onPremiumRequestBought() {
        if (mFragmentTag.equals(Extras.TAG_REQUEST)) {
            RequestFragment fragment = (RequestFragment) mFragManager.findFragmentByTag(Extras.TAG_REQUEST);
            if (fragment != null) fragment.refreshIconRequest();
        }
    }

    @Override
    public void onRequestBuilt(Intent intent, int type) {
        if (type == IntentChooserFragment.ICON_REQUEST) {
            if (RequestFragment.sSelectedRequests == null)
                return;

            if (getResources().getBoolean(R.bool.enable_icon_request_limit)) {
                int used = Preferences.get(this).getRegularRequestUsed();
                Preferences.get(this).setRegularRequestUsed((used + RequestFragment.sSelectedRequests.size()));
            }

            if (Preferences.get(this).isPremiumRequest()) {
                int count = Preferences.get(this).getPremiumRequestCount() - RequestFragment.sSelectedRequests.size();
                Preferences.get(this).setPremiumRequestCount(count);
                if (count == 0) {
                    if (InAppBillingProcessor.get(this).getProcessor().consumePurchase(Preferences
                            .get(this).getPremiumRequestProductId())) {
                        Preferences.get(this).setPremiumRequest(false);
                        Preferences.get(this).setPremiumRequestProductId("");
                    } else {
                        RequestHelper.showPremiumRequestConsumeFailed(this);
                        return;
                    }
                }
            }

            if (mFragmentTag.equals(Extras.TAG_REQUEST)) {
                RequestFragment fragment = (RequestFragment) mFragManager.findFragmentByTag(Extras.TAG_REQUEST);
                if (fragment != null) fragment.refreshIconRequest();
            }
        }

        if (intent != null) {
            try {
                startActivity(intent);
            } catch (IllegalArgumentException e) {
                startActivity(Intent.createChooser(intent,
                        getResources().getString(R.string.app_client)));
            }
        }

        CandyBarApplication.sRequestProperty = null;
        CandyBarApplication.sZipPath = null;
    }

    @Override
    public void onRestorePurchases() {
        if (InAppBillingProcessor.get(this).getProcessor().loadOwnedPurchasesFromGoogle()) {
            List<String> productsId = InAppBillingProcessor.get(this).getProcessor().listOwnedProducts();
            if (productsId != null) {
                SettingsFragment fragment = (SettingsFragment) mFragManager.findFragmentByTag(Extras.TAG_SETTINGS);
                if (fragment != null) fragment.restorePurchases(productsId,
                        mConfig.getPremiumRequestProductsId(), mConfig.getPremiumRequestProductsCount());
            }
        }
    }

    @Override
    public void onInAppBillingSelected(int type, InAppBilling product) {
        Preferences.get(this).setInAppBillingType(type);
        if (type == InAppBilling.PREMIUM_REQUEST) {
            Preferences.get(this).setPremiumRequestCount(product.getProductCount());
            Preferences.get(this).setPremiumRequestTotal(product.getProductCount());
        }

        InAppBillingProcessor.get(this).getProcessor().purchase(this, product.getProductId());
    }

    @Override
    public void onInAppBillingConsume(int type, String productId) {
        if (InAppBillingProcessor.get(this).getProcessor().consumePurchase(productId)) {
            if (type == InAppBilling.DONATE) {
                new MaterialDialog.Builder(this)
                        .typeface(TypefaceHelper.getMedium(this),
                                TypefaceHelper.getRegular(this))
                        .title(R.string.navigation_view_donate)
                        .content(R.string.donation_success)
                        .positiveText(R.string.close)
                        .show();
            }
        }
    }

    @Override
    public void onInAppBillingRequest() {
        if (mFragmentTag.equals(Extras.TAG_REQUEST)) {
            RequestFragment fragment = (RequestFragment) mFragManager.findFragmentByTag(Extras.TAG_REQUEST);
            if (fragment != null) fragment.prepareRequest();
        }
    }

    @Override
    public void onWallpapersChecked(@Nullable Intent intent) {
        if (intent != null) {
            String packageName = intent.getStringExtra("packageName");
            LogUtil.d("Broadcast received from service with packageName: " + packageName);

            if (packageName == null)
                return;

            if (!packageName.equals(getPackageName())) {
                LogUtil.d("Received broadcast from different packageName, expected: " + getPackageName());
                return;
            }

            int size = intent.getIntExtra(Extras.EXTRA_SIZE, 0);
            int offlineSize = Database.get(this).getWallpapersCount();
            Preferences.get(this).setAvailableWallpapersCount(size);

            if (size > offlineSize) {
                if (mFragmentTag.equals(Extras.TAG_HOME)) {
                    HomeFragment fragment = (HomeFragment) mFragManager.findFragmentByTag(Extras.TAG_HOME);
                    if (fragment != null) fragment.resetWallpapersCount();
                }

                LinearLayout container = (LinearLayout) mNavigationView.getMenu().getItem(4).getActionView();
                if (container != null) {
                    TextView counter = container.findViewById(R.id.counter);
                    if (counter == null) return;

                    int newItem = (size - offlineSize);
                    counter.setText(this.getResources().getString(R.string.txt_new));
                    counter.append(" " + (newItem > 99 ? "99+" : newItem));
                    container.setVisibility(View.VISIBLE);
                    return;
                }
            }
        }

        LinearLayout container = (LinearLayout) mNavigationView.getMenu().getItem(4).getActionView();
        if (container != null) container.setVisibility(View.GONE);
    }

    @Override
    public void onSearchExpanded(boolean expand) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        mIsMenuVisible = !expand;

        if (expand) {
            int color = ColorHelper.getAttributeColor(this, R.attr.toolbar_icon);
            toolbar.setNavigationIcon(DrawableHelper.getTintedDrawable(
                    this, R.drawable.ic_toolbar_back, color));
            toolbar.setNavigationOnClickListener(view -> onBackPressed());
        } else {
            SoftKeyboardHelper.closeKeyboard(this);
            ColorHelper.setStatusBarColor(this, Color.TRANSPARENT, true);
            if (CandyBarApplication.getConfiguration().getNavigationIcon() == CandyBarApplication.NavigationIcon.DEFAULT) {
                mDrawerToggle.setDrawerArrowDrawable(new DrawerArrowDrawable(this));
            } else {
                toolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(this,
                        CandyBarApplication.getConfiguration().getNavigationIcon()));
            }

            toolbar.setNavigationOnClickListener(view ->
                    mDrawerLayout.openDrawer(GravityCompat.START));
        }

        mDrawerLayout.setDrawerLockMode(expand ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED :
                DrawerLayout.LOCK_MODE_UNLOCKED);
        supportInvalidateOptionsMenu();
    }

    public void showSupportDevelopmentDialog() {
        InAppBillingFragment.showInAppBillingDialog(mFragManager,
                InAppBilling.DONATE,
                mConfig.getLicenseKey(),
                mConfig.getDonationProductsId(),
                null);
    }

    private void initNavigationView(Toolbar toolbar) {
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.txt_open, R.string.txt_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                SoftKeyboardHelper.closeKeyboard(CandyBarMainActivity.this);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                selectPosition(mPosition);
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(this,
                CandyBarApplication.getConfiguration().getNavigationIcon()));
        toolbar.setNavigationOnClickListener(view ->
                mDrawerLayout.openDrawer(GravityCompat.START));

        if (CandyBarApplication.getConfiguration().getNavigationIcon() == CandyBarApplication.NavigationIcon.DEFAULT) {
            DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(this);
            drawerArrowDrawable.setColor(ColorHelper.getAttributeColor(this, R.attr.toolbar_icon));
            drawerArrowDrawable.setSpinEnabled(true);
            mDrawerToggle.setDrawerArrowDrawable(drawerArrowDrawable);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        NavigationViewHelper.initApply(mNavigationView);
        NavigationViewHelper.initIconRequest(mNavigationView);
        NavigationViewHelper.initWallpapers(mNavigationView);

        ColorStateList itemStateList = ContextCompat.getColorStateList(this,
                ThemeHelper.isDarkTheme(this) ?
                        R.color.navigation_view_item_highlight_dark :
                        R.color.navigation_view_item_highlight);
        mNavigationView.setItemTextColor(itemStateList);
        mNavigationView.setItemIconTintList(itemStateList);
        Drawable background = ContextCompat.getDrawable(this,
                ThemeHelper.isDarkTheme(this) ?
                        R.drawable.navigation_view_item_background_dark :
                        R.drawable.navigation_view_item_background);
        mNavigationView.setItemBackground(background);
        mNavigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_view_home) mPosition = 0;
            else if (id == R.id.navigation_view_apply) mPosition = 1;
            else if (id == R.id.navigation_view_icons) mPosition = 2;
            else if (id == R.id.navigation_view_request) mPosition = 3;
            else if (id == R.id.navigation_view_wallpapers) mPosition = 4;
            else if (id == R.id.navigation_view_settings) mPosition = 5;
            else if (id == R.id.navigation_view_faqs) mPosition = 6;
            else if (id == R.id.navigation_view_about) mPosition = 7;

            item.setChecked(true);
            mDrawerLayout.closeDrawers();
            return true;
        });
    }

    private void initNavigationViewHeader() {
        if (CandyBarApplication.getConfiguration().getNavigationViewHeader() == CandyBarApplication.NavigationViewHeader.NONE) {
            mNavigationView.removeHeaderView(mNavigationView.getHeaderView(0));
            return;
        }

        String imageUrl = getResources().getString(R.string.navigation_view_header);
        String titleText = getResources().getString(R.string.navigation_view_header_title);
        View header = mNavigationView.getHeaderView(0);
        HeaderView image = header.findViewById(R.id.header_image);
        LinearLayout container = header.findViewById(R.id.header_title_container);
        TextView title = header.findViewById(R.id.header_title);
        TextView version = header.findViewById(R.id.header_version);

        if (CandyBarApplication.getConfiguration().getNavigationViewHeader() == CandyBarApplication.NavigationViewHeader.MINI) {
            image.setRatio(16, 9);
        }

        if (titleText.length() == 0) {
            container.setVisibility(View.GONE);
        } else {
            title.setText(titleText);
            try {
                String versionText = "v" + getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionName;
                version.setText(versionText);
            } catch (Exception ignored) {
            }
        }

        if (ColorHelper.isValidColor(imageUrl)) {
            image.setBackgroundColor(Color.parseColor(imageUrl));
            return;
        }

        if (!URLUtil.isValidUrl(imageUrl)) {
            imageUrl = "drawable://" + DrawableHelper.getResourceId(this, imageUrl);
        }

        Glide.with(this)
                .load(imageUrl)
                .override(720)
                .optionalCenterInside()
                .diskCacheStrategy(imageUrl.contains("drawable://")
                        ? DiskCacheStrategy.NONE
                        : DiskCacheStrategy.RESOURCE)
                .into(image);
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(CandyBarBroadcastReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mReceiver = new CandyBarBroadcastReceiver();
        registerReceiver(mReceiver, filter);
    }

    private void checkWallpapers() {
        if (Preferences.get(this).isConnectedToNetwork()) {
            Intent intent = new Intent(this, CandyBarWallpapersService.class);
            startService(intent);
            return;
        }

        int size = Preferences.get(this).getAvailableWallpapersCount();
        if (size > 0) {
            onWallpapersChecked(new Intent()
                    .putExtra("size", size)
                    .putExtra("packageName", getPackageName()));
        }
    }

    private void clearBackStack() {
        if (mFragManager.getBackStackEntryCount() > 0) {
            mFragManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            onSearchExpanded(false);
        }
    }

    public void selectPosition(int position) {
        if (position == 3) {
            if (!getResources().getBoolean(R.bool.enable_icon_request) &&
                    getResources().getBoolean(R.bool.enable_premium_request)) {
                if (!Preferences.get(this).isPremiumRequestEnabled())
                    return;

                if (!Preferences.get(this).isPremiumRequest()) {
                    mPosition = mLastPosition;
                    mNavigationView.getMenu().getItem(mPosition).setChecked(true);
                    onBuyPremiumRequest();
                    return;
                }
            }
        }

        if (position == 4) {
            if (WallpaperHelper.getWallpaperType(this)
                    == WallpaperHelper.EXTERNAL_APP) {
                mPosition = mLastPosition;
                mNavigationView.getMenu().getItem(mPosition).setChecked(true);
                WallpaperHelper.launchExternalApp(CandyBarMainActivity.this);
                return;
            }
        }

        if (position != mLastPosition) {
            mLastPosition = mPosition = position;
            setFragment(getFragment(position));
        }
    }

    private void setFragment(Fragment fragment) {
        clearBackStack();

        FragmentTransaction ft = mFragManager.beginTransaction()
                .replace(R.id.container, fragment, mFragmentTag);
        try {
            ft.commit();
        } catch (Exception e) {
            ft.commitAllowingStateLoss();
        }

        Menu menu = mNavigationView.getMenu();
        menu.getItem(mPosition).setChecked(true);
        mToolbarTitle.setText(menu.getItem(mPosition).getTitle());
    }

    private Fragment getFragment(int position) {
        mFragmentTag = Extras.TAG_HOME;
        if (position == 0) {
            mFragmentTag = Extras.TAG_HOME;
            return new HomeFragment();
        } else if (position == 1) {
            mFragmentTag = Extras.TAG_APPLY;
            return new ApplyFragment();
        } else if (position == 2) {
            mFragmentTag = Extras.TAG_ICONS;
            return new IconsBaseFragment();
        } else if (position == 3) {
            mFragmentTag = Extras.TAG_REQUEST;
            return new RequestFragment();
        } else if (position == 4) {
            mFragmentTag = Extras.TAG_WALLPAPERS;
            return new WallpapersFragment();
        } else if (position == 5) {
            mFragmentTag = Extras.TAG_SETTINGS;
            return new SettingsFragment();
        } else if (position == 6) {
            mFragmentTag = Extras.TAG_FAQS;
            return new FAQsFragment();
        } else if (position == 7) {
            mFragmentTag = Extras.TAG_ABOUT;
            return new AboutFragment();
        }
        return new HomeFragment();
    }

    private Fragment getActionFragment(int action) {
        switch (action) {
            case IntentHelper.ICON_PICKER:
            case IntentHelper.IMAGE_PICKER:
                mPosition = mLastPosition = 2;
                mFragmentTag = Extras.TAG_ICONS;
                return new IconsBaseFragment();
            case IntentHelper.WALLPAPER_PICKER:
                if (WallpaperHelper.getWallpaperType(this) == WallpaperHelper.CLOUD_WALLPAPERS) {
                    mPosition = mLastPosition = 4;
                    mFragmentTag = Extras.TAG_WALLPAPERS;
                    return new WallpapersFragment();
                }
            default:
                mPosition = mLastPosition = 0;
                mFragmentTag = Extras.TAG_HOME;
                return new HomeFragment();
        }
    }

    public static class ActivityConfiguration {

        private boolean mIsLicenseCheckerEnabled;
        private byte[] mRandomString;
        private String mLicenseKey;
        private String[] mDonationProductsId;
        private String[] mPremiumRequestProductsId;
        private int[] mPremiumRequestProductsCount;

        public ActivityConfiguration setLicenseCheckerEnabled(boolean enabled) {
            mIsLicenseCheckerEnabled = enabled;
            return this;
        }

        public ActivityConfiguration setRandomString(@NonNull byte[] randomString) {
            mRandomString = randomString;
            return this;
        }

        public ActivityConfiguration setLicenseKey(@NonNull String licenseKey) {
            mLicenseKey = licenseKey;
            return this;
        }

        public ActivityConfiguration setDonationProductsId(@NonNull String[] productsId) {
            mDonationProductsId = productsId;
            return this;
        }

        public ActivityConfiguration setPremiumRequestProducts(@NonNull String[] ids, @NonNull int[] counts) {
            mPremiumRequestProductsId = ids;
            mPremiumRequestProductsCount = counts;
            return this;
        }

        public boolean isLicenseCheckerEnabled() {
            return mIsLicenseCheckerEnabled;
        }

        public byte[] getRandomString() {
            return mRandomString;
        }

        public String getLicenseKey() {
            return mLicenseKey;
        }

        public String[] getDonationProductsId() {
            return mDonationProductsId;
        }

        public String[] getPremiumRequestProductsId() {
            return mPremiumRequestProductsId;
        }

        public int[] getPremiumRequestProductsCount() {
            return mPremiumRequestProductsCount;
        }
    }
}
