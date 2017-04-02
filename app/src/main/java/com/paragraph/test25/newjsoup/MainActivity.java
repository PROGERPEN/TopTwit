package com.paragraph.test25.newjsoup;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    RecyclerView recyclerView;
    LinearLayoutManager verticalLinearLayoutManager;
    RecyclerAdapter adapter;
    public Elements title;
    public ArrayList<String> titleList = new ArrayList<String>();
    public ArrayList<String> twitterList = new ArrayList<>();
    private SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Лучшие твиты дня");
        setContentView(R.layout.activity_main);

        NewThread nt = new NewThread();
        nt.execute();

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        verticalLinearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(verticalLinearLayoutManager);
        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(200);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        swipe = (SwipeRefreshLayout) findViewById(R.id.SwipeContainer);
        swipe.setOnRefreshListener(this);



    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NewThread nt = new NewThread();
                nt.execute();
                adapter.addAll(titleList, twitterList);
                adapter.notifyDataSetChanged();
                swipe.setRefreshing(false);
            }
        }, 500);

    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder>{
        ArrayList<String> linkList = new ArrayList<String>();
        ArrayList<String> twitList = new ArrayList<String>();

        public void addAll(ArrayList<String> titleList, ArrayList<String> twiterList) {
            int pos = 0;
            this.linkList.addAll(titleList);
            this.twitList.addAll(twiterList);
            notifyItemRangeInserted(pos, this.linkList.size());
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);

            return new RecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder holder, final int position) {
                Picasso.with(MainActivity.this).load(linkList.get(position)).into(holder.image3);
            {
                holder.buttonLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitList.get(position)));
                        startActivity(browserIntent);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return linkList.size();
        }
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder{
        public ImageView image3;
        public Button buttonLink;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            image3 = (ImageView) itemView.findViewById(R.id.image2);
            buttonLink = (Button) itemView.findViewById(R.id.buttonLink);

        }

        public void bind(String s) {

        }
    }

    public class NewThread extends AsyncTask<String, Void, String> {

        // Метод выполняющий запрос в фоне
        @Override
        protected String doInBackground(String... arg) {

            // класс который захватывает страницу
            Document doc;
            try {
                // определяем откуда будем воровать данные
                doc = Jsoup.connect("http://feed.exileed.com/vk/feed/toptwit/?owner=1&count=100").get();
                // задаем с какого места, я выбрал заголовке статей
                // чистим наш аррей лист для того что бы заполнить
                titleList.clear();
                twitterList.clear();
                // и в цикле захватываем все данные какие есть на странице
                Elements link3 = doc.getElementsByTag("description");

                for(Element el:link3){
                    String k = String.valueOf(el);
                    int oops = k.indexOf("src='")+5;
                    int oops2 = k.indexOf(".jpg")+4;
                    int twittag1 = k.indexOf("href='")+6;
                    int twittag2 = k.indexOf("'&", twittag1);
                    if (oops2>oops) {
                        String newlink = k.substring(oops, oops2);
                        if (!k.contains("https://pp.userapi.com/c627628/"))
                            titleList.add(newlink);
                    }
                    if (twittag2>twittag1){
                        String twitLink = k.substring(twittag1, twittag2);
                        twitterList.add(twitLink);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            adapter.addAll(titleList, twitterList);
            adapter.notifyDataSetChanged();
        }
    }
}
