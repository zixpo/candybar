package candybar.lib.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;

import candybar.lib.R;
import candybar.lib.adapters.IconPackAdapter;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.helpers.RequestHelper;
import candybar.lib.items.IconPack;
import candybar.lib.utils.AsyncTaskBase;
import candybar.lib.helpers.IconPackAppHelper;


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

//                    String[] iconPackNames = RequestHelper.getIconPackNames(context);
//                    for (String iconPackName : iconPackNames) {
//                        String[] colors = RequestHelper.getIconPackColors(context, iconPackName);
//                        if (colors.length > 0) {
//                            int drawableId = RequestHelper.getIconPackDrawableId(context, iconPackName);
//                            iconPacks.add(new IconPack(iconPackName,  drawableId, colors));
//                        }
//                    }

                    collectIconPacks(context, iconPacks);
//                    String mainIconPackName = context.getString(R.string.icon_pack);
//                    String mainIconPackColor = context.getString(R.string.icon_pack_color);
//                    int mainIconPackDrawableId = RequestHelper.getIconPackDrawableId(context, mainIconPackName);
//                    List<String> iconPackNames =
//
//                    List<String> extraIconPackNames = IconPackAppHelper.getIconPackAppPackageNames(context);
//                    for (String iconPackName : iconPackNames) {
//                        String[] colors = RequestHelper.getIconPackColors(context, iconPackName);
//                        if (colors.length > 0) {
//                            int drawableId = RequestHelper.getIconPackDrawableId(context, iconPackName);
//                            iconPacks.add(new IconPack(iconPackName,  drawableId, colors));
//                        }
//                    }

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
            return context.getResources().getDrawable(drawableId, context.getTheme());
        }
        return null;
    }

    public static void collectIconPacks(Context context, List<IconPack> iconPacks) {
        // Initialize a map to store icon pack names and their respective colors
        Map<String, Set<String>> iconPackColorsMap = new HashMap<>();

        // Add main app icon pack name and color to the map
        String mainIconPackName = context.getString(R.string.icon_pack);
        String mainIconPackColor = context.getString(R.string.icon_pack_color);
        iconPackColorsMap.computeIfAbsent(mainIconPackName, k -> new TreeSet<>()).add(mainIconPackColor);

        // Collect side app icon pack names and their colors
        List<String> sideAppIconPackageNames = IconPackAppHelper.getIconPackAppPackageNames(context);
        for (String sideAppPackageName : sideAppIconPackageNames) {
            String sideAppIconPackName = IconPackAppHelper.getIconPackName(context, sideAppPackageName);
            String sideAppColor = IconPackAppHelper.getIconPackColor(context, sideAppPackageName);
            iconPackColorsMap.computeIfAbsent(sideAppIconPackName, k -> new TreeSet<>()).add(sideAppColor);
        }

        // Convert the map entries to IconPack objects and add them to the iconPacks list
        for (Map.Entry<String, Set<String>> entry : iconPackColorsMap.entrySet()) {
            String iconPackName = entry.getKey();
            Set<String> colors = entry.getValue();
            int drawableId = RequestHelper.getIconPackDrawableId(context, iconPackName); // Replace with actual drawable retrieval

            // Create IconPack object and add to iconPacks list
            iconPacks.add(new IconPack(iconPackName, drawableId, colors.toArray(new String[0])));
        }
    }

}
