package com.nytimes.nytsearch;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by supsingh on 10/22/2016.
 */

public class ArticleArrayAdapter extends RecyclerView.Adapter<ViewHolder> {


    // Store a member variable for the contacts
    private ArrayList<Article> mArticles;

    // Pass in the contact array into the constructor
    public ArticleArrayAdapter(ArrayList<Article> articles) {
        mArticles = articles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.item_article_result, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView, context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Article article = mArticles.get(position);

        ImageView articleImage = (ImageView) holder.articleImage;
        TextView articleTitle = (TextView) holder.articleTitle;
        Context articleContext = (Context) holder.articleContext;

        articleTitle.setText(article.getHeadline());

        // articleImage.setImageResource(0);
        String thumbnail = article.getThumbnail();
        if(!TextUtils.isEmpty(thumbnail)) {
            Picasso.with(articleContext).load(thumbnail).transform(new RoundedCornersTransformation(10, 10)).into(articleImage);
        }

        return;
    }

    @Override
    public int getItemCount() {
        return mArticles.size();
    }
}


class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    // Your holder should contain a member variable
    // for any view that will be set as you render a row
    public ImageView articleImage;
    public TextView articleTitle;
    public Context articleContext;

    // We also create a constructor that accepts the entire item row
    // and does the view lookups to find each subview
    public ViewHolder(View itemView, Context context) {
        // Stores the itemView in a public final member variable that can be used
        // to access the context from any ViewHolder instance.
        super(itemView);

        articleImage = (ImageView) itemView.findViewById(R.id.ivImage);
        articleTitle = (TextView) itemView.findViewById(R.id.tvTitle);
        articleContext = context;

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int position = getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
            // User user = users.get(position);
            // We can access the data within the views
            Log.d("DEBUG", "sup: loading page for article: " + position);
            Intent i = new Intent(articleContext, ArticleActivity.class);
            Article article = Article.getArticleArrayList().get(position);
            i.putExtra("article", article);
            articleContext.startActivity(i);
        }
    }
}

