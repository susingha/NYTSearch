package com.nytimes.nytsearch;

import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

/**
 * Created by supsingh on 10/21/2016.
 */

public class Article implements Serializable {

    String webUrl;
    String headline;
    String thumbnail;

    public String getWebUrl() {
        return webUrl;
    }
    public String getHeadline() {
        return headline;
    }
    public String getThumbnail() {
        return thumbnail;
    }

    public Article(JSONObject jsonObject) {
        try {
            this.webUrl = jsonObject.getString("web_url");
            this.headline = jsonObject.getJSONObject("headline").getString("main");
            JSONArray multimedia = jsonObject.getJSONArray("multimedia");

            if(multimedia.length() > 0) {
                JSONObject multimediaJson = multimedia.getJSONObject(0);
                this.thumbnail = prefixUrl + multimediaJson.getString("url");
            } else {
                this.thumbnail = "";
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // STATICS
    private static ArrayList<String> orderValues = new ArrayList<>(Arrays.asList("newest", "oldest"));
    private static final String apiUrl = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    private static final String apiKey = "cf6266fbb42e41b883b5ae6c8e1f2edb";
    private static final String prefixUrl = "http://www.nytimes.com/";
    private static ArrayList<Article> ArticleArrayList = new ArrayList<>();
    private static String lastQuery;
    private static String filterOrder = "newest";
    private static String filterDate = "";
    private static String filterCategory = "";


    public static ArrayList<Article> getArticleArrayList() {
        return ArticleArrayList;
    }

    public static void setFilterOrder(int pos) {
        filterOrder = orderValues.get(pos);
    }

    public static void setFilterDate(String date) {
        filterDate = date.toString();
    }

    public static void setFilterCategory(String category) {
        filterCategory = category.toString();
    }

    public static ArrayList<Article> fetchArticlePageFirst(boolean refresh, final SearchActivity context, final String query) {

        lastQuery = query;

        RequestParams params = new RequestParams();
        params.put("api-key", apiKey);
        params.put("q", lastQuery);
        params.put("sort", filterOrder);
        if(!filterDate.isEmpty())
            params.put("begin_date", filterDate);
        if(!filterCategory.isEmpty())
            params.put("fq", filterCategory);
        params.put("page", 0);

        Log.d("DEBUG", "sup: fetchArticlePageFirst Called");

        if (refresh == false && ArticleArrayList.isEmpty() == false) {
            Log.d("DEBUG", "sup:1 fetchArticleArrayList returning");
            return ArticleArrayList;
        }

        Toast.makeText(context, R.string.refreshing, Toast.LENGTH_SHORT).show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(apiUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", "sup:1 fetchArticlePageFirst onSuccess Called");
                // super.onSuccess(statusCode, headers, response);
                JSONArray articleJsonResults = null;
                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    appendFromJSONArray(articleJsonResults, context);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG", "sup:1 onFailure 1 FAIL FAIL FAIL FAIL FAIL FAIL FAIL FAIL FAIL");
                super.onFailure(statusCode, headers, throwable, errorResponse);
                handleFailure(0, context);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("DEBUG", "sup:1 onFailure 2 FAIL FAIL FAIL FAIL FAIL FAIL FAIL FAIL FAIL");
                super.onFailure(statusCode, headers, responseString, throwable);
                handleFailure(0, context);
            }
        });

        Log.d("DEBUG", "sup:2 fetchArticleArrayList returning");
        return ArticleArrayList;
    }


    public static ArrayList<Article> fetchArticlePageNext(boolean refresh, final SearchActivity context, int page) {

        String query = lastQuery;
        final int page_t = page;

        RequestParams params = new RequestParams();
        params.put("api-key", apiKey);
        params.put("q", query);
        params.put("sort", filterOrder);
        if(!filterDate.isEmpty())
            params.put("begin_date", filterDate);
        if(!filterCategory.isEmpty())
            params.put("fq", filterCategory);
        params.put("page", page);


        Log.d("DEBUG", "sup: fetchArticlePageNext Called page = " + page);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(apiUrl, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", "sup:2 fetchArticlePageNext onSuccess Called");
                // super.onSuccess(statusCode, headers, response);
                JSONArray articleJsonResults = null;
                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    appendFromJSONArray(articleJsonResults, context);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG", "sup:2 onFailure 1 FAIL FAIL FAIL FAIL FAIL FAIL FAIL FAIL FAIL");
                super.onFailure(statusCode, headers, throwable, errorResponse);
                handleFailure(page_t, context);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("DEBUG", "sup:2 onFailure 2 FAIL FAIL FAIL FAIL FAIL FAIL FAIL FAIL FAIL");
                super.onFailure(statusCode, headers, responseString, throwable);
                handleFailure(page_t, context);
            }
        });

        return ArticleArrayList;
    }

    private static void appendFromJSONArray (JSONArray array, final SearchActivity context) {
        int len = array.length();
        int insertPos = ArticleArrayList.size();
        for (int x = 0; x < len; x++) {
            try {
                ArticleArrayList.add(new Article(array.getJSONObject(x)));
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        context.notifyResponse(insertPos, len);
    }

    private static void handleFailure(int page, final SearchActivity context) {
        context.notifyFailure(page);
    }
}
