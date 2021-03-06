package com.example.guess_a_celeb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs = new ArrayList<>();
    ArrayList<String> celebNames = new ArrayList<>();
    int chosenCeleb;
    ImageView myImageView;
    String[] answers = new String[4];
    int locationOfCorrectAnswer = 0;
    Button btn0, btn1, btn2, btn3;

    public class ImageDownloader extends AsyncTask <String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try{
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public class DownloadTask extends AsyncTask <String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while(data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            }catch (Exception e){
                e.printStackTrace();

            }
            return null;
        }
    }

    public void newQuestion(){

        try {
            Random rand = new Random(celebURLs.size());
            chosenCeleb = rand.nextInt();

            ImageDownloader imageTask = new ImageDownloader();
            Bitmap celebImage = imageTask.execute(celebURLs.get(chosenCeleb)).get();
            myImageView.setImageBitmap(celebImage);

            locationOfCorrectAnswer = rand.nextInt(4);
            int incorrectAnswerLocation;

            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    incorrectAnswerLocation = rand.nextInt(celebURLs.size());
                    while (incorrectAnswerLocation == chosenCeleb) {
                        incorrectAnswerLocation = rand.nextInt();
                    }

                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }


            btn0.setText(answers[0]);
            btn1.setText(answers[1]);
            btn2.setText(answers[2]);
            btn3.setText(answers[3]);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void buttonClicked(View view){
        if(view.getTag().toString().equals(String.valueOf(locationOfCorrectAnswer))){
            Toast.makeText(getApplicationContext(), "Correct!!", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getApplicationContext(), "Incorrect!! It was " + celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }

        newQuestion();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn0 = (Button)findViewById(R.id.btn0);
        btn1 = (Button)findViewById(R.id.btn1);
        btn2 = (Button)findViewById(R.id.btn2);
        btn3 = (Button)findViewById(R.id.btn3);


        myImageView = (ImageView)findViewById(R.id.myImageView);

        DownloadTask task = new DownloadTask();
        String result = null;

        try{
            result = task.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"listedArticles\">");

            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while(m.find()){
                celebURLs.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while(m.find()){
               celebNames.add(m.group(1));
            }

            Log.i("Contents of URL", result);

            newQuestion();


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}