package candybar.lib.helpers;

import android.content.Context;
import android.util.Log;

import androidx.appsearch.app.AppSearchSchema;
import androidx.appsearch.app.AppSearchSchema.PropertyConfig;
import androidx.appsearch.app.AppSearchSchema.LongPropertyConfig;
import androidx.appsearch.app.AppSearchSchema.StringPropertyConfig;
import androidx.appsearch.app.AppSearchSession;
import androidx.appsearch.app.GenericDocument;
import androidx.appsearch.app.PutDocumentsRequest;
import androidx.appsearch.app.SearchResult;
import androidx.appsearch.app.SearchResults;
import androidx.appsearch.app.SearchSpec;
import androidx.appsearch.app.SetSchemaRequest;
import androidx.appsearch.localstorage.LocalStorage;

import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import candybar.lib.items.Icon;

public class AppSearchHelper {

    private static final String DATABASE_NAME = "icons";
    private static AppSearchHelper sInstance;

    private final ListenableFuture<AppSearchSession> mSessionFuture;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private boolean mSchemaSet = false;
    private final Object mInitLock = new Object();
    private boolean mInitialized = false;

    public static synchronized AppSearchHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AppSearchHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private AppSearchHelper(Context context) {
        mSessionFuture = LocalStorage.createSearchSessionAsync(
                new LocalStorage.SearchContext.Builder(context, DATABASE_NAME).build()
        );
    }

    public void initAsync(Context context) {
        mExecutor.execute(() -> {
            try {
                synchronized (mInitLock) {
                    if (mInitialized) return;
                    getSession();
                    mInitialized = true;
                    mInitLock.notifyAll();
                }
            } catch (Exception e) {
                LogUtil.e("Failed to pre-initialize AppSearch: " + Log.getStackTraceString(e));
                synchronized (mInitLock) {
                    mInitialized = true;
                    mInitLock.notifyAll();
                }
            }
        });
    }

    public void indexIconsAsync(List<Icon> sections) {
        mExecutor.execute(() -> {
            try {
                ensureInitialized();
                clearDatabase();
                indexIcons(sections);
            } catch (Exception e) {
                LogUtil.e("Failed to index icons asynchronously: " + Log.getStackTraceString(e));
            }
        });
    }

    public void ensureInitialized() {
        synchronized (mInitLock) {
            while (!mInitialized) {
                try {
                    mInitLock.wait();
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public void clearDatabase() throws Exception {
        AppSearchSession session = getSession();
        session.removeAsync("", new SearchSpec.Builder().build()).get();
    }

    public AppSearchSession getSession() throws Exception {
        AppSearchSession session = mSessionFuture.get();
        synchronized (this) {
            if (!mSchemaSet) {
                setupSchema(session);
                mSchemaSet = true;
            }
        }
        return session;
    }

    private void setupSchema(AppSearchSession session) throws Exception {
        AppSearchSchema iconSchema = new AppSearchSchema.Builder("Icon")
                .addProperty(new StringPropertyConfig.Builder("drawableName")
                        .setCardinality(PropertyConfig.CARDINALITY_REQUIRED)
                        .build())
                .addProperty(new StringPropertyConfig.Builder("title")
                        .setCardinality(PropertyConfig.CARDINALITY_OPTIONAL)
                        .setIndexingType(StringPropertyConfig.INDEXING_TYPE_PREFIXES)
                        .setTokenizerType(StringPropertyConfig.TOKENIZER_TYPE_PLAIN)
                        .build())
                .addProperty(new StringPropertyConfig.Builder("customName")
                        .setCardinality(PropertyConfig.CARDINALITY_OPTIONAL)
                        .build())
                .addProperty(new LongPropertyConfig.Builder("resId")
                        .setCardinality(PropertyConfig.CARDINALITY_REQUIRED)
                        .build())
                .addProperty(new StringPropertyConfig.Builder("categories")
                        .setCardinality(PropertyConfig.CARDINALITY_REPEATED)
                        .build())
                .addProperty(new StringPropertyConfig.Builder("tags")
                        .setCardinality(PropertyConfig.CARDINALITY_REPEATED)
                        .setIndexingType(StringPropertyConfig.INDEXING_TYPE_PREFIXES)
                        .setTokenizerType(StringPropertyConfig.TOKENIZER_TYPE_PLAIN)
                        .build())
                .build();

        AppSearchSchema categorySchema = new AppSearchSchema.Builder("Category")
                .addProperty(new StringPropertyConfig.Builder("categoryName")
                        .setCardinality(PropertyConfig.CARDINALITY_REQUIRED)
                        .build())
                .addProperty(new LongPropertyConfig.Builder("order")
                        .setCardinality(PropertyConfig.CARDINALITY_REQUIRED)
                        .build())
                .build();

        SetSchemaRequest setSchemaRequest = new SetSchemaRequest.Builder()
                .addSchemas(iconSchema, categorySchema)
                .setForceOverride(true)
                .build();

        session.setSchemaAsync(setSchemaRequest).get();
    }

    public void indexIcons(List<Icon> sections) throws Exception {
        AppSearchSession session = getSession();

        List<GenericDocument> catDocuments = new ArrayList<>();
        int catOrder = 0;
        for (Icon section : sections) {
            String category = section.getTitle();
            if (category == null) continue;
            GenericDocument catDoc = new GenericDocument.Builder<>("categories", category, "Category")
                    .setPropertyString("categoryName", category)
                    .setPropertyLong("order", catOrder++)
                    .build();
            catDocuments.add(catDoc);
        }

        Map<String, GenericDocument.Builder<?>> docBuilders = new LinkedHashMap<>();
        Map<String, Set<String>> itemCategories = new LinkedHashMap<>();

        for (Icon section : sections) {
            String category = section.getTitle();
            for (Icon icon : section.getIcons()) {
                String drawableName = icon.getDrawableName();
                if (drawableName == null) continue;

                String title = icon.getTitle() != null ? icon.getTitle() : "";
                String combinationKey = drawableName + "|" + title;

                Set<String> catSet = itemCategories.get(combinationKey);
                if (catSet == null) {
                    catSet = new LinkedHashSet<>();
                    itemCategories.put(combinationKey, catSet);
                }
                if (category != null && !category.isEmpty()) {
                    catSet.add(category);
                }

                if (!docBuilders.containsKey(combinationKey)) {
                    GenericDocument.Builder<?> docBuilder = new GenericDocument.Builder<>("icons", combinationKey, "Icon")
                            .setPropertyString("drawableName", drawableName)
                            .setPropertyString("title", title)
                            .setPropertyString("customName", icon.getCustomName() != null ? icon.getCustomName() : "")
                            .setPropertyLong("resId", icon.getRes())
                            .setPropertyString("tags", icon.getTags().toArray(new String[0]));
                    docBuilders.put(combinationKey, docBuilder);
                }
            }
        }

        List<GenericDocument> documents = new ArrayList<>();
        for (Map.Entry<String, GenericDocument.Builder<?>> entry : docBuilders.entrySet()) {
            String key = entry.getKey();
            GenericDocument.Builder<?> builder = entry.getValue();
            Set<String> catSet = itemCategories.get(key);
            if (catSet != null && !catSet.isEmpty()) {
                builder.setPropertyString("categories", catSet.toArray(new String[0]));
            }
            documents.add(builder.build());
        }

        PutDocumentsRequest catPutRequest = new PutDocumentsRequest.Builder()
                .addGenericDocuments(catDocuments)
                .build();
        session.putAsync(catPutRequest).get();

        int batchSize = 500;
        for (int i = 0; i < documents.size(); i += batchSize) {
            List<GenericDocument> batch = documents.subList(i, Math.min(i + batchSize, documents.size()));
            PutDocumentsRequest putRequest = new PutDocumentsRequest.Builder()
                    .addGenericDocuments(batch)
                    .build();
            session.putAsync(putRequest).get();
        }
        LogUtil.d("AppSearch successfully indexed " + catDocuments.size() + " categories and " + documents.size() + " icon entries");
    }

    public List<Icon> queryIcons(String queryText, boolean substring) throws Exception {
        AppSearchSession session = getSession();
        List<Icon> results = new ArrayList<>();

        if (substring && queryText != null && !queryText.trim().isEmpty()) {
            List<Icon> allIcons = queryIcons("", false);
            String lowerQuery = queryText.toLowerCase(Locale.getDefault()).trim();
            for (Icon icon : allIcons) {
                boolean match = false;
                if (icon.getTitle() != null && icon.getTitle().toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                    match = true;
                } else {
                    for (String tag : icon.getTags()) {
                        if (tag.toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                            match = true;
                            break;
                        }
                    }
                }
                if (match) {
                    results.add(icon);
                }
            }
        } else {
            SearchSpec searchSpec = new SearchSpec.Builder()
                    .setResultCountPerPage(10000)
                    .setTermMatch(SearchSpec.TERM_MATCH_PREFIX)
                    .build();

            try (SearchResults searchResults = session.search(queryText != null ? queryText.trim() : "", searchSpec)) {
                List<SearchResult> page = searchResults.getNextPageAsync().get();
                while (!page.isEmpty()) {
                    for (SearchResult row : page) {
                        GenericDocument doc = row.getGenericDocument();
                        String drawableName = doc.getPropertyString("drawableName");
                        String title = doc.getPropertyString("title");
                        String customName = doc.getPropertyString("customName");
                        int resId = (int) doc.getPropertyLong("resId");
                        String[] tags = doc.getPropertyStringArray("tags");

                        Icon icon = new Icon(drawableName, customName, resId);
                        icon.setTitle(title);
                        if (tags != null) {
                            Set<String> tagSet = new LinkedHashSet<>();
                            Collections.addAll(tagSet, tags);
                            icon.setTags(tagSet);
                        }
                        results.add(icon);
                    }
                    page = searchResults.getNextPageAsync().get();
                }
            }
        }

        if (queryText != null && !queryText.trim().isEmpty()) {
            String cleanQuery = queryText.trim().toLowerCase(Locale.getDefault());
            Collections.sort(results, (icon1, icon2) -> {
                boolean exact1 = icon1.getTitle() != null && icon1.getTitle().toLowerCase(Locale.getDefault()).equals(cleanQuery);
                boolean exact2 = icon2.getTitle() != null && icon2.getTitle().toLowerCase(Locale.getDefault()).equals(cleanQuery);
                if (exact1 && !exact2) return -1;
                if (!exact1 && exact2) return 1;

                String title1 = icon1.getTitle() != null ? icon1.getTitle() : "";
                String title2 = icon2.getTitle() != null ? icon2.getTitle() : "";
                return title1.compareToIgnoreCase(title2);
            });
        } else {
            Collections.sort(results, (icon1, icon2) -> {
                String title1 = icon1.getTitle() != null ? icon1.getTitle() : "";
                String title2 = icon2.getTitle() != null ? icon2.getTitle() : "";
                return title1.compareToIgnoreCase(title2);
            });
        }

        return results;
    }
}
