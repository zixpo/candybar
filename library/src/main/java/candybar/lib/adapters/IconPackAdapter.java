package candybar.lib.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import candybar.lib.R;
import candybar.lib.fragments.dialog.ChangeIconColorFragment;
import candybar.lib.items.IconPack;
import candybar.lib.helpers.StringUtils;

import java.util.Arrays;

public class IconPackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<IconPack> mIconPacks;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTENT = 1;
    private static final int TYPE_FOOTER = 2;

    public IconPackAdapter(Context context, List<IconPack> iconPacks) {
        mContext = context;
        mIconPacks = iconPacks;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == mIconPacks.size() + 1) {
            return TYPE_FOOTER;
        } else {
            return TYPE_CONTENT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(mContext).inflate(R.layout.fragment_apply_item_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_CONTENT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.fragment_icon_pack_item_list, parent, false);
            return new ContentViewHolder(view);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.fragment_apply_item_footer, parent, false);
            return new FooterViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            String title = "Icon Packs";
            headerHolder.title.setText(title);
        } else if (holder.getItemViewType() == TYPE_CONTENT) {
            ContentViewHolder contentHolder = (ContentViewHolder) holder;
            IconPack iconPack = mIconPacks.get(position - 1);

            String iconPackTitle = iconPack.getTitle();
            String[] iconPackPackages = iconPack.getPackageNames();
            String matchingPackageName = findMatchingPackageName(iconPackTitle, iconPackPackages, mContext);

            // Check if the iconPackTitle matches the value from resources
            String mainIconPackTitle = mContext.getString(R.string.icon_pack);
            if (iconPackTitle.equals(mainIconPackTitle)) {
                // Load drawable from the main app
                Glide.with(mContext)
                        .load(R.drawable.ic_icon_pack)
                        .into(contentHolder.icon);
            } else {
                // Load drawable from the other app
                loadIconFromOtherApp(matchingPackageName, contentHolder.icon);
            }

            contentHolder.title.setText(StringUtils.capitalize(iconPack.getTitle()));
            contentHolder.itemView.setOnClickListener(v -> {
                String[] colors = iconPack.getColors();
                String iconPackName = iconPack.getTitle();
                String[] packageNames = iconPack.getPackageNames();
                ChangeIconColorFragment.showChangeIconColorDialog(
                        ((FragmentActivity) mContext).getSupportFragmentManager(), colors, iconPackName, packageNames);
            });
        }
    }


    private void loadIconFromOtherApp(String packageName, ImageView imageView) {
        try {
            Context otherAppContext = mContext.createPackageContext(packageName, 0);
            int drawableId = otherAppContext.getResources().getIdentifier("ic_icon_pack", "drawable", packageName);
            Drawable drawable = otherAppContext.getResources().getDrawable(drawableId, otherAppContext.getTheme());

            Glide.with(mContext)
                    .load(drawable)
                    .into(imageView);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            // Handle the exception, maybe load a default image or show an error
        }
    }

    private static String findMatchingPackageName(String iconPackTitle, String[] iconPackPackages, Context context) {
        String baseIdentifier = context.getPackageName(); // Dynamically get the package name

        for (String packageName : iconPackPackages) {
            if (packageName.startsWith(baseIdentifier)) {
                String remainingPart = packageName.substring(baseIdentifier.length());
                if (remainingPart.startsWith("." + iconPackTitle)) {
                    return packageName;
                }
            }
        }
        return null; // Return null if no match is found
    }

    @Override
    public int getItemCount() {
        return mIconPacks.size() + 2;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.name);
        }
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;

        ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.name);
            icon = itemView.findViewById(R.id.icon);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
