package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

//TODO настроить отображение списка валют
public class MainActivity extends AppCompatActivity {
    private final String pathResource = "https://www.cbr-xml-daily.ru/daily_json.js";
    @SuppressLint("SdCardPath")
    private final String pathCacheValute = "/data/data/com.example.myapplication/curr.txt";
    private ScrollView scrollView;
    private TextView contentView;
    private  ArrayList<Valute> valG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        scrollView = (ScrollView) findViewById(R.id.scrollView);
        contentView = (TextView) findViewById(R.id.content);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);


        Thread th = new Thread(() -> {
            try {
                File pCaVa = new File(pathCacheValute);

                if(pCaVa.exists() && pCaVa.length() > 0){
                    valG = new ArrayList<>();
                    FileReader fr = new FileReader(pCaVa);
                    //создаем BufferedReader с существующего FileReader для построчного считывания
                    BufferedReader reader = new BufferedReader(fr);
                    // считаем сначала первую строку
                    String line = reader.readLine();
                    while (line != null) {
                        valG.add(new Gson().fromJson(line ,Valute.class));

                        line = reader.readLine();
                    }

                    if(reader != null)
                        reader.close();
                    if(fr != null)
                        reader.close();
                    if(pCaVa != null)
                        reader.close();

                    Log.d("File","File log is open");
                }
                else {

                    valG = new ArrayList<> (getContent(pathResource));

                    Log.d("Connection","Get connection after Create Activity");
                }



                scrollView.post(() -> {
                    StringBuilder s = new StringBuilder();

                    for(Valute v: valG) s.append(v.toStringLight());
                      contentView.setText(s.toString());
                });
                scrollView.addView(contentView);


            } catch (Exception ex) {
                 Log.d("Exception", ex.toString());
            }
        });

        th.start();



        //TODO нужна перезагрузка/перезапуск потока для обновления информации по валютам
        swipeRefreshLayout.setOnRefreshListener(() -> {

            th.interrupt();

            if(th.isInterrupted())
                th.start();
            Log.d("Timer", "RefreshList");

            swipeRefreshLayout.setRefreshing(false);
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Log.d("Activity","OnStart");

        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {public void run()
            {
                try {
                    ArrayList<Valute> valG = new ArrayList<> (getContent(pathResource));


                    scrollView.post(() -> {
                        StringBuilder s = new StringBuilder();

                        for(Valute v: valG) s.append(v.toStringLight());
                        //s.append(" times update");
                        contentView.setText(s.toString());
                    });

                    Log.d("Timer", "Update data");

                } catch (Exception ex) {
                    Log.d("Exception", ex.toString() + "   Resume");
                }
            }
        }, 5000, 100000); //100 Секунд
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileOutputStream writer = null;
        try{
            writer = new FileOutputStream(new File(pathCacheValute));

            StringBuilder s = new StringBuilder();

            for(Valute v: valG) s.append(new Gson().toJson(v)).append("\n");

            writer.write(s.toString().getBytes());

            Log.d("File", "File open");
        }
        catch(Exception ex){
            Log.d("File", ex.toString());
        }
        finally {
            try {
                if(writer != null)
                    writer.close();
            } catch (IOException e) {
                Log.d("File", e.toString());
            }
        }
    }

    /*@Override
    protected void onRestart() {

        TextView contentView = (TextView) findViewById(R.id.content);


        new Thread(new Runnable(){
            public  void run() {
                try {
                    ArrayList<Valute> valG = new ArrayList<> (getContent(pathResource));


                    contentView.post(new Runnable() {
                        public void run() {

                            StringBuilder s = new StringBuilder();

                            for(Valute v: valG) s.append(v.toString());


                            contentView.setText(s.toString());
                        }
                    });


                } catch (Exception ex) {
                    contentView.post(new Runnable() {
                        public void run() {
                            contentView.setText(ex.toString());}
                    });
                }
            }
        }).start();
        super.onRestart();
    }
*/

    //создаем соединение с сайтом. сериализуем данные в объекты и вставляем в список валют
    private ArrayList<Valute> getContent(String path) throws Exception {
        BufferedReader reader = null;
        InputStream stream = null;
        HttpsURLConnection connection = null;
        ArrayList<Valute> valG = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        int countLine = 0;
        String line;
        Valute tempVal;

        try {
            URL url = new URL(path);

            connection = (HttpsURLConnection)url.openConnection();

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
            //connection.setReadTimeout(10000);
            connection.connect();

            stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            while ((line = reader.readLine()) != null) {
                //TODO изменить этот участок кода. не гибкий!!!
                if ((countLine >= 6) && (countLine <= 311))
                {
                    if (line.indexOf("{") > 0)  {
                        buf.delete(0, buf.length());
                        line = "{";
                    }

                    if (line.indexOf("}") > 0) {
                        line = "}";
                        buf.append(line);

                        tempVal = new Gson().fromJson(buf.toString(), Valute.class);

                        valG.add(tempVal);
                    }

                    buf.append(line).append("\n");
                }
                countLine++;
            }


            return valG;
        }
        finally {
            if (reader != null) {
                reader.close();
            }
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}