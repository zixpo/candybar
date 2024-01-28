package candybar.lib.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Patterns;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;

import static com.danimahardhika.android.helpers.core.DrawableHelper.getTintedDrawable;

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

public class UrlHelper {

    @Nullable
    public static Drawable getSocialIcon(@NonNull Context context, @NonNull Type type) {
        int color = ConfigurationHelper.getSocialIconColor(context, CandyBarApplication.getConfiguration().getSocialIconColor());
        switch (type) {
            case EMAIL:
                return getTintedDrawable(context, R.drawable.ic_toolbar_email, color);
            case BEHANCE:
                return getTintedDrawable(context, R.drawable.ic_toolbar_behance, color);
            case BLUESKY:
                return getTintedDrawable(context, R.drawable.ic_toolbar_bluesky, color);
            case DRIBBBLE:
                return getTintedDrawable(context, R.drawable.ic_toolbar_dribbble, color);
            case DISCORD:
                return getTintedDrawable(context, R.drawable.ic_toolbar_discord, color);                
            case FACEBOOK:
                return getTintedDrawable(context, R.drawable.ic_toolbar_facebook, color);
            case GITHUB:
                return getTintedDrawable(context, R.drawable.ic_toolbar_github, color);
            case GITLAB:
                return getTintedDrawable(context, R.drawable.ic_toolbar_gitlab, color);
            case GOOGLEPLAY:
                return getTintedDrawable(context, R.drawable.ic_toolbar_googleplay, color);
            case INSTAGRAM:
                return getTintedDrawable(context, R.drawable.ic_toolbar_instagram, color);
            case KOFI:
                return getTintedDrawable(context, R.drawable.ic_toolbar_kofi, color);
            case MASTODON:
                return getTintedDrawable(context, R.drawable.ic_toolbar_mastodon, color);
            case MATRIX:
                return getTintedDrawable(context, R.drawable.ic_toolbar_matrix, color);
            case PINTEREST:
                return getTintedDrawable(context, R.drawable.ic_toolbar_pinterest, color);
            case THREADS:
                return getTintedDrawable(context, R.drawable.ic_toolbar_threads, color);
            case TWITTER:
                return getTintedDrawable(context, R.drawable.ic_toolbar_x, color);
            case TELEGRAM:
                return getTintedDrawable(context, R.drawable.ic_toolbar_telegram, color);
            case TIKTOK:
                return getTintedDrawable(context, R.drawable.ic_toolbar_tiktok, color);
            default:
                return getTintedDrawable(context, R.drawable.ic_toolbar_website, color);
        }
    }

    public static Type getType(String url) {
        if (url == null) return Type.INVALID;
        if (!URLUtil.isValidUrl(url)) {
            if (Patterns.EMAIL_ADDRESS.matcher(url).matches()) {
                return Type.EMAIL;
            }
            return Type.INVALID;
        }

        if (url.contains("behance.")) {
            return Type.BEHANCE;
        } else if (url.contains("bsky.")) {
            return Type.BLUESKY;
        } else if (url.contains("dribbble.")) {
            return Type.DRIBBBLE;
        } else if (url.contains("discord.")) {
            return Type.DISCORD;
        } else if (url.contains("facebook.")) {
            return Type.FACEBOOK;
        } else if (url.contains("github.")) {
            return Type.GITHUB;
        } else if (url.contains("gitlab.")) {
            return Type.GITLAB;
        } else if (url.contains("play.google.")) {
            return Type.GOOGLEPLAY;
        } else if (url.contains("instagram.")) {
            return Type.INSTAGRAM;
        } else if (url.contains("ko-fi.")) {
            return Type.KOFI;
        } else if (url.contains("mastodon.")|| url.contains("mstdn.")|| url.contains("mas.")|| url.contains("todon.")|| url.contains("fosstodon.")|| url.contains("troet.")|| url.contains("chaos.")|| url.contains("floss.")) {
            return Type.MASTODON;
        } else if (url.contains("matrix.")) {
            return Type.MATRIX;
        } else if (url.contains("pinterest.")) {
            return Type.PINTEREST;
        } else if (url.contains("twitter.")|| url.contains("https://x.com/")) {
            return Type.TWITTER;
        } else if (url.contains("threads.")) {
            return Type.THREADS;
        } else if (url.contains("t.me/") || url.contains("telegram.me/")) {
            return Type.TELEGRAM;
        } else if (url.contains("tiktok.")) {
            return Type.TIKTOK;
        } else {
            return Type.UNKNOWN;
        }
    }

    public enum Type {
        EMAIL,
        BEHANCE,
        BLUESKY,
        DRIBBBLE,
        DISCORD,
        FACEBOOK,
        GITHUB,
        GITLAB,
        GOOGLEPLAY,
        INSTAGRAM,
        KOFI,
        MASTODON,
        MATRIX,
        PINTEREST,
        THREADS,
        TWITTER,
        TELEGRAM,
        TIKTOK,
        UNKNOWN,
        INVALID
    }
}
