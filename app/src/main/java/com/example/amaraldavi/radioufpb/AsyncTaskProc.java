package com.example.amaraldavi.radioufpb;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import net.moraleboost.streamscraper.ScrapeException;
import net.moraleboost.streamscraper.Scraper;
import net.moraleboost.streamscraper.Stream;
import net.moraleboost.streamscraper.scraper.IceCastScraper;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by amaraldavi on 10/10/16.
 */

public class AsyncTaskProc extends AsyncTask<Object, Object, Void> {

    private String radioName;
    private String radioDesc;
    private TextView lblRadioName;
    private TextView lblRadioDesc;
    private String url;

    WeakReference<Activity> mWeakActivity;

    public AsyncTaskProc(Activity activity, String radioUrl) {
        mWeakActivity = new WeakReference<Activity>(activity);

        url = new String(radioUrl);

    }

    @Override
    protected Void doInBackground(Object... unused) {

        List<Stream> streams = null;
        Scraper scraper = new IceCastScraper();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {

            streams = scraper.scrape(new URI(url));

            if(streams.size()>0) {
                radioName = streams.get(0).getTitle();
                radioDesc = streams.get(0).getDescription();
            }else {
                Log.i("---INFO---","Varredura de metadados Icecast retornou vazia.");
            }
        } catch (ScrapeException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            Log.e("ERRO DE CONEXÃO: ", "Impossível se conectar com Servidor ICECAST");
            //Toast.makeText(getApplicationContext(),e.toString(), Toast.LENGTH_LONG).show();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //Toast.makeText(getApplicationContext(),e.toString(), Toast.LENGTH_LONG).show();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        Activity activity = mWeakActivity.get();
        if (activity != null) {
            lblRadioName = (TextView) activity.findViewById(R.id.radio_title);
            lblRadioDesc = (TextView) activity.findViewById(R.id.radio_desc);

            lblRadioName.setText(radioName);
            lblRadioDesc.setText(radioDesc);

        }
    }

    }



