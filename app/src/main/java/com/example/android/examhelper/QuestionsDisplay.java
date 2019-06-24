package com.example.android.examhelper;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.examhelper.Utils.AlgorithmUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class QuestionsDisplay extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private ClipData mClipData;
    private List<Uri> mUriList;
    private  List<String> mQuestionsList;
    private  ListView mQuestionListView;
    private  HashMap<String, Integer> mQuestionsCount;
    private  QuestionsAdapter adapter;
    private  ProgressBar mQuestionsProgressBar;
    private  TextView mProgressBarLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions_display);

        mClipData = (ClipData) getIntent().getParcelableExtra("uri_list");
        mUriList = getUriList(mClipData);
        mQuestionListView = (ListView) findViewById(R.id.question_list_view);
        mQuestionsProgressBar = (ProgressBar) findViewById(R.id.questions_progress_bar);
        mProgressBarLabel = (TextView) findViewById(R.id.progress_bar_label);

        try {
            mQuestionsList = getQuestionsList(mUriList, getString(R.string.delimiter_default_value));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        mQuestionsCount = AlgorithmUtils.counter(mQuestionsList);
//        adapter = new QuestionsAdapter(mQuestionsCount);
//        mQuestionListView.setAdapter(adapter);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        new QuestionsAsyncTask().execute(mQuestionsList);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);


        mQuestionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap.Entry<String, Integer> h = adapter.getItem(position);
                String query = h.getKey();
                String escapedQuery = null;
                try {
                    escapedQuery = URLEncoder.encode(query, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Uri uri = Uri.parse("http://www.google.com/#q=" + query);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });


    }

    public class QuestionsAsyncTask extends AsyncTask<List<String>, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mQuestionsProgressBar.setVisibility(View.VISIBLE);
            mProgressBarLabel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(List<String>... lists) {
            mQuestionsCount = AlgorithmUtils.counter(lists[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new QuestionsAdapter(mQuestionsCount);
            mQuestionListView.setAdapter(adapter);
            mQuestionsProgressBar.setVisibility(View.GONE);
            mProgressBarLabel.setVisibility(View.GONE);
        }
    }

    private List<Uri> getUriList(ClipData data) {
        List<Uri> uriList = new ArrayList<>();
        for (int i = 0; i < data.getItemCount(); i++) {
            uriList.add(data.getItemAt(i).getUri());
        }
        return uriList;
    }

    private List<String> getQuestionsList(List<Uri> uriList, String delimiter) throws IOException {
        InputStream is = null;
        BufferedReader reader;
        String line;
        List<String> questionsList = new ArrayList<String>();
        StringBuilder info = new StringBuilder();
        for (Uri uri : uriList) {
            is = getContentResolver().openInputStream(uri);
            reader = new BufferedReader(new InputStreamReader(is));
            while ((line = reader.readLine()) != null) {
                info.append(line);
            }
        }
            if (delimiter.equals("alpha")) {
                questionsList.addAll(Arrays.asList(info.toString().split("[a-z][)]")));
            } else if(delimiter.equals("num")){
                questionsList.addAll(Arrays.asList(info.toString().split("[0-9]{1,2}[)]")));

            }
        for(int i=0;i<questionsList.size();i++)
        {   String test = questionsList.get(i);
            if(questionsList.get(i).equals("\\s+") || questionsList.get(i).split("").length==1 || questionsList.get(i).equals("\n+"))
            {
                questionsList.remove(i);
            }
        }
        return questionsList;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.list_key))) {
            try {
                mQuestionsList = getQuestionsList(mUriList, sharedPreferences.getString(getString(R.string.list_key), getString(R.string.delimiter_default_value)));
                String k = sharedPreferences.getString(getString(R.string.list_key), getString(R.string.delimiter_default_value));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new QuestionsAsyncTask().execute(mQuestionsList);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our visualizer_menu layout to this menu */
        inflater.inflate(R.menu.settings_menu, menu);
        /* Return true so that the visualizer_menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
