package candybar.lib.items;

public class IconPack {
    private final String title;
    private final int iconResId;
    private final String[] colors;
    private final String[] packageNames;

    public IconPack(String title, int iconResId, String[] colors, String[] packageNames) {
        this.title = title;
        this.iconResId = iconResId;
        this.colors = colors;
        this.packageNames = packageNames;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String[] getColors() {
        return colors;
    }

    public String[] getPackageNames() {
        return packageNames;
    }
}
