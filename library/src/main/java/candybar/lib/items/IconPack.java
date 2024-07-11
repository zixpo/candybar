package candybar.lib.items;

public class IconPack {
    private final String title;
    private final int iconResId;
    private final String[] colors;

    public IconPack(String title, int iconResId, String[] colors) {
        this.title = title;
        this.iconResId = iconResId;
        this.colors = colors;
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
}
