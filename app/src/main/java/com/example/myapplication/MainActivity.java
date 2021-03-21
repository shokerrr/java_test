package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView contentView = (TextView) findViewById(R.id.content);
        //WebView browser = (WebView) findViewById(R.id.webBrowser);

        //browser.getSettings().setJavaScriptEnabled(true);

        new Thread(new Runnable(){
            public  void run() {
                try {
                    ArrayList<Valute> valG = new ArrayList<> (getContent("https://www.cbr-xml-daily.ru/daily_json.js"));


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


    }


    //создаем соединение с сайтом. сериализуем данные в объекты и вставляем в список валют
    private ArrayList<Valute> getContent(String path) throws Exception {
        BufferedReader reader = null;
        InputStream stream = null;
        HttpsURLConnection connection = null;
        ArrayList<Valute> valG = new ArrayList<Valute>();
        StringBuilder buf = new StringBuilder();
        int countLine = 0;
        String line;
        Valute tempVal;

        try {
            URL url = new URL(path);

            connection = (HttpsURLConnection)url.openConnection();

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
            connection.setReadTimeout(10000);
            connection.connect();

            stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            while ((line = reader.readLine()) != null) {
                if ((countLine >= 6) && (countLine <=311))
                {
                    if (line.indexOf("\"") > 0)  {

                        line = line.replace("\"",  "\'");
                    }

                    if (line.indexOf(" ") > 0)  {

                        line = line.replace(" ",  "");
                    }

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

