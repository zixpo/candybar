package candybar.lib.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import candybar.lib.R;
import candybar.lib.adapters.IconPackAdapter;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.helpers.RequestHelper;
import candybar.lib.items.IconPack;
import candybar.lib.utils.AsyncTaskBase;
import android.graphics.drawable.Drawable;

public class IconPackFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private AsyncTaskBase mAsyncTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icon_pack, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerview);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                "view",
                new HashMap<String, Object>() {{
                    put("section", "icon_pack");
                }}
        );

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));

        if (CandyBarApplication.getConfiguration().getApplyGrid() == CandyBarApplication.GridStyle.FLAT) {
            int padding = requireActivity().getResources().getDimensionPixelSize(R.dimen.card_margin);
            mRecyclerView.setPadding(padding, padding, 0, 0);
        }

        mAsyncTask = new IconPacksLoader(getContext()).executeOnThreadPool();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    private class IconPacksLoader extends AsyncTaskBase {

        private Context context;
        private List<IconPack> iconPacks;

        public IconPacksLoader(Context context) {
            this.context = context;
        }

        @Override
        protected void preRun() {
            iconPacks = new ArrayList<>();
        }

        @Override
        protected boolean run() {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);

                    // Load icon packs from the RequestHelper
                    String[] iconPackNames = RequestHelper.getIconPackNames(context);
                    for (String iconPackName : iconPackNames) {
                        String[] colors = RequestHelper.getIconPackColors(context, iconPackName);
                        if (colors.length > 0) {
                            int drawableId = RequestHelper.getIconPackDrawableId(context, iconPackName);
                            iconPacks.add(new IconPack(iconPackName,  drawableId, colors));
                        }
                    }

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void postRun(boolean ok) {
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            if (ok) {
                mRecyclerView.setAdapter(new IconPackAdapter(getActivity(), iconPacks));
            }
        }
    }

    public static Drawable getIconPackDrawable(Context context, String iconPackName) {
        int drawableId = RequestHelper.getIconPackDrawableId(context, iconPackName);
        if (drawableId != 0) {
            // Return the drawable
            return context.getResources().getDrawable(drawableId, context.getTheme());
        }
        return null;
    }

}
