package it.jaschke.alexandria.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.services.BookService;

/**
 * Created by davidduque on 11/2/15.
 */
public class Utility {

    public static void setBookSearchStatus(Context context,
                                         @BookService.BookSearchStatus int bookSearchStatus) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(context.getString(R.string.pref_book_search_status_key), bookSearchStatus);
        editor.commit();
    }

    @SuppressWarnings("ResourceType")
    public static @BookService.BookSearchStatus int getBookSearchStatus(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(context.getString(R.string.pref_book_search_status_key),
                BookService.BOOK_SEARCH_STATUS_KO);
    }

    public static boolean checkNetworkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        return ((cm.getActiveNetworkInfo() != null) &&
                (cm.getActiveNetworkInfo().isConnectedOrConnecting()));
    }
}
