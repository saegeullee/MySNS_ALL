package com.example.saegeullee.applicationoneproject.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.saegeullee.applicationoneproject.Adapter.NewsAdapter;
import com.example.saegeullee.applicationoneproject.Models.News;
import com.example.saegeullee.applicationoneproject.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment {

    private static final String TAG = "NewsFragment";

    private View view;
    private ProgressBar mProgressBar;

    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private List<News> newsList;

    private String searchQuery;

    private boolean isFirstSearch;
    private int requestNumber;

    public static final String SHARED_PREF_NAME = "querySharedPref";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: in");
        Log.d(TAG, "onCreate: requestNumber : " + requestNumber);

        requestNumber = 1;
        isFirstSearch = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_news, container, false);

        setHasOptionsMenu(true);

        mProgressBar = view.findViewById(R.id.progressbar);

        newsList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new NewsAdapter(getActivity(), newsList);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount();

                Log.d(TAG, "onScrolled: lastVisibleItemPosition : " + lastVisibleItemPosition);
                Log.d(TAG, "onScrolled: itemTotalCount : " + itemTotalCount);

                if(lastVisibleItemPosition== (itemTotalCount - 1)) {

//                    showProgressbar();

                    //다음 데이터 가져옴
                    Log.d(TAG, "onScrolled: itemTotalCount : " + itemTotalCount);
                    isFirstSearch = false;
                    requestNumber += 1;
                    getNews(searchQuery);

//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//
//                        }
//                    }, 300);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
//        if(!sharedPreferences.getString("searchQuery", "").equals(""))
//            getNews(sharedPreferences.getString("searchQuery", ""));

        return view;
    }

    private void showSearchDialog() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.search_news_dialog);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        Button searchBtn = dialog.findViewById(R.id.submitBtn);
        final EditText editText = dialog.findViewById(R.id.searchInput);
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);

        dialog.show();
        dialog.getWindow().setAttributes(lp);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(editText.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
                } else {

                    searchQuery = editText.getText().toString();

                    isFirstSearch = true;
                    requestNumber = 1;

                    getNews(searchQuery);
                    editor.putString("searchQuery", searchQuery);
                    dialog.dismiss();
                }
            }
        });
    }

    private void getNews(final String query) {

        Log.d(TAG, "getNews: in");
        final StringBuilder[] sb = new StringBuilder[1];//

        final String clientId = "AQ7qUd272h2oViC__hIz";// 애플리케이션 클라이언트 아이디값";
        final String clientSecret = "xDi3VRgiZg";// 애플리케이션 클라이언트 시크릿값";\

        final int display = 15; // 검색결과갯수. 최대100개
        final int start_number =  ((requestNumber - 1)*15 + 1);

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String text = URLEncoder.encode(query, "utf-8");
                    String apiURL = "https://openapi.naver.com/v1/search/news?query=" + text + "&display=" + display + "&start=" + start_number;

                    URL url = new URL(apiURL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("X-Naver-Client-Id", clientId);
                    con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                    int responseCode = con.getResponseCode();

                    BufferedReader br;
                    if (responseCode == 200) {
                        br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    } else {
                        br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    }
                    sb[0] = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null) {
                        sb[0].append(line.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "") + "\n");
                    }

                    br.close();
                    con.disconnect();
                    Log.d(TAG, "getNews: sb : " + sb[0].toString());

                    if(isFirstSearch)
                        newsList.clear();

                    initRecyclerView(sb[0]);

                } catch (Exception e) {
                    Log.d(TAG, "getNews: exception : " + e.getMessage());
                }
            }
        }).start();
    }

    private void initRecyclerView(StringBuilder stringBuilder) {

        try {
            JSONObject jsonObject = new JSONObject(String.valueOf(stringBuilder));

            JSONArray jsonArray = jsonObject.getJSONArray("items");

            for(int i = 0; i < jsonArray.length(); i++) {

                JSONObject object = jsonArray.getJSONObject(i);
                Log.d(TAG, "initRecyclerView: object : " + object.toString());

                News news = new News();
                news.setTitle(object.getString("title"));
                news.setLink(object.getString("link"));
                news.setDesc(object.getString("description"));
                news.setDate(object.getString("pubDate"));

                newsList.add(news);
            }

            Log.d(TAG, "initRecyclerView: newsList size : " + newsList.size());

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hideProgressbar() {
        if(mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showProgressbar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_fragment_news, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.search_news:
                showSearchDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCategoryRecyclerView() {





    }
}
