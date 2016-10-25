package com.nytimes.nytsearch;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private SwipeRefreshLayout swipeContainer;

    String query;
    String date;
    String dateVerbal;
    EditText etQuery;
    RecyclerView rvResults;
    Button btnSearch;
    Spinner spSorter;
    TextView tvDPLauncher;

    ArrayList<Article> articles = Article.getArticleArrayList();
    private EndlessRecyclerViewScrollListener scrollListener;
    ArticleArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupViews();
    }

    public void setupViews() {
        etQuery = (EditText) findViewById(R.id.etQuery);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        rvResults = (RecyclerView) findViewById(R.id.rvResults);
        spSorter = (Spinner) findViewById(R.id.spSorter);
        tvDPLauncher = (TextView) findViewById(R.id.tvDPLauncher);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rvResults.setLayoutManager(gridLayoutManager);
        adapter = new ArticleArrayAdapter(articles);
        rvResults.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.d("DEBUG", "sup: onLoadMore called with page = " + page + " totalItemsCount " + totalItemsCount);
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                customLoadMoreDataFromApi(page);
            }
        };
        rvResults.addOnScrollListener(scrollListener);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setEnabled(false);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });

        spSorter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("DEBUG", "sup: selected " + position);
                onArticleSearch(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onArticleSearch(View view) {

        query = etQuery.getText().toString();
        if (query.isEmpty()) {
            Toast.makeText(this, "Type Query", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide the Keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etQuery.getWindowToken(), 0);

        fetchTimelineAsync(0);
    }

    public void notifyResponse(int insertPos, int length) {
        adapter.notifyDataSetChanged();
        // adapter.notifyItemRangeInserted(insertPos, length);
        swipeContainer.setRefreshing(false);
        swipeContainer.setEnabled(true);
    }

    public void notifyFailure(int page) {
        fetchTimelineAsync(page + 1);
        swipeContainer.setEnabled(true);
    }

    public void fetchTimelineAsync(int page) {
        Log.d("INFO", "fetchTimelineAsync called");
        if (query == null)
            query = etQuery.getText().toString();


        int itemPos = spSorter.getSelectedItemPosition();
        Log.d("DEBUG", "spinner value = "+ itemPos);
        Article.setFilterOrder(itemPos);

        if (page == 0) {
            // 1. First, clear the array of data
            articles.clear();
            // 2. Notify the adapter of the update
            adapter.notifyDataSetChanged(); // or notifyItemRangeRemoved
            // 3. Reset endless scroll listener when performing a new search
            scrollListener.resetState();
            // 4. Fetch new data
            articles = Article.fetchArticlePageFirst(true, this, query);
        } else {
            if (page == 1)
                page++;
            articles = Article.fetchArticlePageNext(true, this, page);
        }
    }


    // Append more data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void customLoadMoreDataFromApi(int page) {
        // Send an API request to retrieve appropriate data using the offset value as a parameter.
        //  --> Deserialize API response and then construct new objects to append to the adapter
        //  --> Notify the adapter of the changes
        fetchTimelineAsync(page);
    }

    public void onClickDatePicker(View view) {
        Log.d("DEBUG", "sup: trying to pick date");  // attach to an onclick handler to show the date picker
        showDatePickerDialog(view);
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    // handle the date selected
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // store the values selected into a Calendar instance
        date = String.format("%4d%02d%02d", year, monthOfYear, dayOfMonth);
        dateVerbal = String.format("%4d-%02d-%02d", year, monthOfYear, dayOfMonth);
        Log.d("DEBUG", "sup: Date: " + dateVerbal);
        tvDPLauncher.setText(dateVerbal);
        Article.setFilterDate(date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
