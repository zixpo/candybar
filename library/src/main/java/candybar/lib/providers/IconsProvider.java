package candybar.lib.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appsearch.app.AppSearchSession;
import androidx.appsearch.app.GenericDocument;
import androidx.appsearch.app.SearchResult;
import androidx.appsearch.app.SearchResults;
import androidx.appsearch.app.SearchSpec;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import candybar.lib.helpers.AppSearchHelper;

public class IconsProvider extends ContentProvider {

    private static final int ICONS = 1;
    private static final int CATEGORIES = 2;
    private static UriMatcher sUriMatcher;

    private AppSearchHelper mAppSearchHelper;

    private synchronized UriMatcher getUriMatcher() {
        if (sUriMatcher == null) {
            sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            String authority = getContext().getPackageName() + ".icons";
            sUriMatcher.addURI(authority, "icons", ICONS);
            sUriMatcher.addURI(authority, "categories", CATEGORIES);
        }
        return sUriMatcher;
    }

    @Override
    public boolean onCreate() {
        mAppSearchHelper = AppSearchHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = getUriMatcher().match(uri);
        if (match == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        mAppSearchHelper.ensureInitialized();

        if (match == ICONS) {
            String queryText = uri.getQueryParameter("query");
            String categoryParam = uri.getQueryParameter("category");
            boolean substring = Boolean.parseBoolean(uri.getQueryParameter("substring"));

            String[] cols = projection != null ? projection : new String[] {
                "drawable_name", "title", "custom_name", "res_id", "category", "tags"
            };

            MatrixCursor cursor = new MatrixCursor(cols);
            try {
                AppSearchSession session = mAppSearchHelper.getSession();
                List<SearchResult> allResults = new ArrayList<>();

                SearchSpec.Builder specBuilder = new SearchSpec.Builder()
                        .setResultCountPerPage(10000)
                        .addFilterSchemas("Icon");

                specBuilder.setTermMatch(SearchSpec.TERM_MATCH_PREFIX);

                String appsearchQuery = (substring || queryText == null) ? "" : queryText.trim();

                try (SearchResults searchResults = session.search(appsearchQuery, specBuilder.build())) {
                    List<SearchResult> page = searchResults.getNextPageAsync().get();
                    while (!page.isEmpty()) {
                        allResults.addAll(page);
                        page = searchResults.getNextPageAsync().get();
                    }
                }

                List<GenericDocument> matchedDocs = new ArrayList<>();
                if (substring && queryText != null && !queryText.trim().isEmpty()) {
                    String cleanQuery = queryText.toLowerCase(Locale.getDefault()).trim();
                    for (SearchResult row : allResults) {
                        GenericDocument doc = row.getGenericDocument();
                        String title = doc.getPropertyString("title");
                        String[] tags = doc.getPropertyStringArray("tags");
                        boolean matchFound = false;
                        if (title != null && title.toLowerCase(Locale.getDefault()).contains(cleanQuery)) {
                            matchFound = true;
                        } else if (tags != null) {
                            for (String tag : tags) {
                                if (tag.toLowerCase(Locale.getDefault()).contains(cleanQuery)) {
                                    matchFound = true;
                                    break;
                                }
                            }
                        }
                        if (matchFound) {
                            matchedDocs.add(doc);
                        }
                    }
                } else {
                    for (SearchResult row : allResults) {
                        matchedDocs.add(row.getGenericDocument());
                    }
                }

                if (categoryParam != null && !categoryParam.trim().isEmpty()) {
                    String targetCategory = categoryParam.trim();
                    List<GenericDocument> filteredByCat = new ArrayList<>();
                    for (GenericDocument doc : matchedDocs) {
                        String[] catArray = doc.getPropertyStringArray("categories");
                        if (catArray != null) {
                            for (String c : catArray) {
                                if (targetCategory.equalsIgnoreCase(c)) {
                                    filteredByCat.add(doc);
                                    break;
                                }
                            }
                        }
                    }
                    matchedDocs = filteredByCat;
                }

                if (queryText != null && !queryText.trim().isEmpty()) {
                    String cleanQuery = queryText.toLowerCase(Locale.getDefault()).trim();
                    Collections.sort(matchedDocs, (doc1, doc2) -> {
                        String t1 = doc1.getPropertyString("title");
                        String t2 = doc2.getPropertyString("title");
                        boolean exact1 = t1 != null && t1.toLowerCase(Locale.getDefault()).equals(cleanQuery);
                        boolean exact2 = t2 != null && t2.toLowerCase(Locale.getDefault()).equals(cleanQuery);
                        if (exact1 && !exact2) return -1;
                        if (!exact1 && exact2) return 1;

                        String title1 = t1 != null ? t1 : "";
                        String title2 = t2 != null ? t2 : "";
                        return title1.compareToIgnoreCase(title2);
                    });
                } else {
                    Collections.sort(matchedDocs, (doc1, doc2) -> {
                        String title1 = doc1.getPropertyString("title");
                        String title2 = doc2.getPropertyString("title");
                        String t1 = title1 != null ? title1 : "";
                        String t2 = title2 != null ? title2 : "";
                        return t1.compareToIgnoreCase(t2);
                    });
                }

                for (GenericDocument doc : matchedDocs) {
                    MatrixCursor.RowBuilder row = cursor.newRow();
                    for (String col : cols) {
                        if ("drawable_name".equals(col)) {
                            row.add(doc.getPropertyString("drawableName"));
                        } else if ("title".equals(col)) {
                            row.add(doc.getPropertyString("title"));
                        } else if ("custom_name".equals(col)) {
                            row.add(doc.getPropertyString("customName"));
                        } else if ("res_id".equals(col) || "_id".equals(col)) {
                            row.add((int) doc.getPropertyLong("resId"));
                        } else if ("category".equals(col) || "categories".equals(col)) {
                            String[] cats = doc.getPropertyStringArray("categories");
                            row.add(cats != null ? TextUtils.join(",", cats) : "");
                        } else if ("tags".equals(col)) {
                            String[] tags = doc.getPropertyStringArray("tags");
                            row.add(tags != null ? TextUtils.join(",", tags) : "");
                        } else {
                            row.add(null);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.e("IconsProvider query failed: " + Log.getStackTraceString(e));
            }
            return cursor;
        } else if (match == CATEGORIES) {
            String[] cols = projection != null ? projection : new String[] { "_id", "category_name" };
            MatrixCursor cursor = new MatrixCursor(cols);
            try {
                AppSearchSession session = mAppSearchHelper.getSession();
                SearchSpec spec = new SearchSpec.Builder()
                        .setResultCountPerPage(10000)
                        .setTermMatch(SearchSpec.TERM_MATCH_PREFIX)
                        .addFilterSchemas("Category")
                        .build();

                List<GenericDocument> catDocs = new ArrayList<>();
                try (SearchResults searchResults = session.search("", spec)) {
                    List<SearchResult> page = searchResults.getNextPageAsync().get();
                    while (!page.isEmpty()) {
                        for (SearchResult row : page) {
                            catDocs.add(row.getGenericDocument());
                        }
                        page = searchResults.getNextPageAsync().get();
                    }
                }

                Collections.sort(catDocs, (doc1, doc2) -> {
                    long order1 = doc1.getPropertyLong("order");
                    long order2 = doc2.getPropertyLong("order");
                    return Long.compare(order1, order2);
                });

                int id = 1;
                for (GenericDocument doc : catDocs) {
                    String category = doc.getPropertyString("categoryName");
                    MatrixCursor.RowBuilder row = cursor.newRow();
                    for (String col : cols) {
                        if ("category_name".equals(col)) {
                            row.add(category);
                        } else if ("_id".equals(col)) {
                            row.add(id++);
                        } else {
                            row.add(null);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.e("IconsProvider categories query failed: " + Log.getStackTraceString(e));
            }
            return cursor;
        }

        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = getUriMatcher().match(uri);
        if (match == ICONS) {
            return "vnd.android.cursor.dir/vnd.icons";
        } else if (match == CATEGORIES) {
            return "vnd.android.cursor.dir/vnd.categories";
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("Insert not supported");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete not supported");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Update not supported");
    }
}
