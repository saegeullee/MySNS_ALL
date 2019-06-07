package com.example.saegeullee.applicationoneproject.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.saegeullee.applicationoneproject.Models.News;
import com.example.saegeullee.applicationoneproject.NewsDetailActivity;
import com.example.saegeullee.applicationoneproject.R;

import java.util.List;
import java.util.StringTokenizer;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private Context context;
    private List<News> newsList;

    public NewsAdapter(Context context, List<News> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_news, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final News news = newsList.get(position);

        holder.title.setText(news.getTitle());
        holder.desc.setText(news.getDesc());

        StringTokenizer tokenizer = new StringTokenizer(news.getDate(), " ");

        String dayOfWeek = tokenizer.nextToken();
        String day = tokenizer.nextToken();
        String month = tokenizer.nextToken();
        String year = tokenizer.nextToken();

        String date = dayOfWeek + " " + day + " " + month + " " + year;

        holder.date.setText(date);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NewsDetailActivity.class);
                intent.putExtra(context.getString(R.string.intent_news_content), news.getLink());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title, desc, date;

        public ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.news_title);
            desc = itemView.findViewById(R.id.news_desc);
            date = itemView.findViewById(R.id.news_date);
        }
    }
}
