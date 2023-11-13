package edu.fandm.pcettina.wordly;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class Game_Activity extends AppCompatActivity {
    private Graph wordGraph;
    private List<String> path;
    private List<EditText> guessEditTexts;
    private int currentGuessIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        View rootView = findViewById(R.id.root);

        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }
        wordGraph = new Graph(this, "words_simple.txt");

        final EditText startWordEditText = findViewById(R.id.editTextStart);
        final EditText endWordEditText = findViewById(R.id.editTextEnd);
        final LinearLayout wordContainer = findViewById(R.id.wordContainer);
        final TextView resultTextView = findViewById(R.id.textViewResult);

        guessEditTexts = new ArrayList<>();
        currentGuessIndex = 0;

        Button findPathButton = findViewById(R.id.buttonFindPath);
        findPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String startWord = startWordEditText.getText().toString().trim().toLowerCase();
                String endWord = endWordEditText.getText().toString().trim().toLowerCase();

                path = findPath(wordGraph.getGraph(), startWord, endWord);

                if (path != null && !path.get(0).equals("wrong")) {
                    createWordGuessUI(wordContainer, path);
                    resultTextView.setText("Try to guess the words in the path!");

                }
                else if(path.get(0).equals("wrong")){
                    resultTextView.setText("Words not in graph.");
                }
                else {
                    resultTextView.setText("No valid path exists.");
                }
            }
        });

        Button checkResponse = (Button) findViewById(R.id.buttonSubmit);
        checkResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUserGuess();
            }
        });
    }

    private void createWordGuessUI(LinearLayout wordContainer, List<String> path) {
        wordContainer.removeAllViews(); // Clear existing UI components
        guessEditTexts.clear();
        for (int i = 1; i < path.size()-1; i++) {
            String word = path.get(i);

            // Create a TextView for each word
            TextView wordTextView = new TextView(this);
            wordTextView.setText("?");
            wordTextView.setTextSize(18);
            wordTextView.setGravity(Gravity.CENTER);

            // Create an EditText for user input
            EditText guessEditText = new EditText(this);
            guessEditText.setHint("Enter word");
            guessEditText.setTag(word); // Tag each EditText with the corresponding word
            guessEditText.setTextSize(14);
            guessEditText.setGravity(Gravity.CENTER);

            guessEditTexts.add(guessEditText);

            // Add views to the container
            wordContainer.addView(wordTextView);
            wordContainer.addView(guessEditText);
        }
        Game_Activity.WordImgExecutor rie = new Game_Activity.WordImgExecutor();
        rie.fetch(ric,path.get(1));
    }
    private void checkUserGuess() {
        if (path == null) {
            return; // No valid path or all guesses completed
        }

        String userGuess = guessEditTexts.get(currentGuessIndex).getText().toString().trim().toLowerCase();
        String correctWord = path.get(currentGuessIndex+1);

        if (userGuess.equals(correctWord)) {

            currentGuessIndex++;

            if (currentGuessIndex < guessEditTexts.size()) {
                // Move to the next guess
                EditText nextGuessEditText = guessEditTexts.get(currentGuessIndex);
                nextGuessEditText.requestFocus();
                Game_Activity.WordImgExecutor rie = new Game_Activity.WordImgExecutor();
                rie.fetch(ric, path.get(currentGuessIndex + 1));// Load photo for the next word
                showCorrectGuessAnimation();
            } else {
                // User completed all guesses, show congratulations animation
                showCongratulationsAnimation();

                // Reset for a new round of guesses
                currentGuessIndex = 0;
            }
        } else {
            final TextView resultTextView = findViewById(R.id.textViewResult);
            resultTextView.setText("Incorrect. Try again");
        }
    }
    private List<String> findPath(Map<String, List<String>> graph, String startWord, String endWord) {
        if (!graph.containsKey(startWord) || !graph.containsKey(endWord)) {
            List<String> list = Collections.singletonList("wrong");
            return list;// Either start or end word is not in the graph
        }

        Queue<String> queue = new LinkedList<>();
        Map<String, String> parentMap = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(startWord);
        visited.add(startWord);

        while (!queue.isEmpty()) {
            String currentWord = queue.poll();

            for (String neighbor : graph.get(currentWord)) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                    parentMap.put(neighbor, currentWord);

                    if (neighbor.equals(endWord)) {
                        // Found the end word, reconstruct the path
                        return reconstructPath(parentMap, startWord, endWord);
                    }
                }
            }
        }

        return null; // No path found
    }

    private List<String> reconstructPath(Map<String, String> parentMap, String startWord, String endWord) {
        List<String> path = new ArrayList<>();
        String currentWord = endWord;

        while (currentWord != null) {
            path.add(currentWord);
            currentWord = parentMap.get(currentWord);
        }

        Collections.reverse(path);
        return path;
    }

    private void showCorrectGuessAnimation() {
        final TextView resultTextView = findViewById(R.id.textViewResult);
        resultTextView.setText("Correct Guess!");

        AlphaAnimation blinkAnimation = new AlphaAnimation(0.0f, 1.0f);
        blinkAnimation.setDuration(500);
        blinkAnimation.setRepeatMode(AlphaAnimation.REVERSE);
        blinkAnimation.setRepeatCount(5);

        resultTextView.startAnimation(blinkAnimation);

        // Reset the animation and text after it finishes
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                resultTextView.clearAnimation();
                resultTextView.setText("");
            }
        }, 5000); // Adjust the duration as needed
    }



    private void showCongratulationsAnimation() {
        final TextView resultTextView = findViewById(R.id.textViewResult);
        resultTextView.setText("Congratulations!");

        AlphaAnimation blinkAnimation = new AlphaAnimation(0.0f, 1.0f);
        blinkAnimation.setDuration(500);
        blinkAnimation.setRepeatMode(AlphaAnimation.REVERSE);
        blinkAnimation.setRepeatCount(5);

        resultTextView.startAnimation(blinkAnimation);

        // Reset the animation and text after it finishes
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                resultTextView.clearAnimation();
                resultTextView.setText("");
            }
        }, 5000); // Adjust the duration as needed
    }



    Game_Activity.WordImgCallback ric = new Game_Activity.WordImgCallback(){
        public void onComplete(Bitmap img){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(img != null) {
                        ImageView tv = (ImageView) findViewById(R.id.imageView);
                        tv.setImageBitmap(img);
                    } else{
                        Toast.makeText(getApplicationContext(), "Failed to download IMG :(", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };
    interface WordImgCallback{
        void onComplete(Bitmap img);
    }


    public class WordImgExecutor{
        public void fetch(Game_Activity.WordImgCallback ric, String word){
            ExecutorService es = Executors.newFixedThreadPool(1);

            es.execute(new Runnable() {
                @Override
                public void run() {
                    String url_image = null;
                    Bitmap img = null;

                    try{

                        URL url = new URL("https://pixabay.com/api/?key=40639320-8a391c5a237693deea3abfccb&q=" + word+"&image_type=photo");
                        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                        con.setRequestMethod("GET");
                        con.connect();

                        BufferedReader url_in = new BufferedReader(new InputStreamReader(url.openStream()));
                        StringBuffer data = new StringBuffer();
                        String curLine;
                        while((curLine = url_in.readLine()) != null){
                            Log.println(Log.WARN, "JSON", "line: " + curLine);
                            data.append(curLine);
                        }


                        JSONObject jsonResponse = new JSONObject(data.toString());
                        JSONArray hitsArray = jsonResponse.getJSONArray("hits");

                        if (hitsArray.length() > 0) {  //https://chat.openai.com/share/c9bb4010-e8f0-48e1-bc1b-44fd575b3822
                            Random r_idx = new Random();
                            int idx = r_idx.nextInt(hitsArray.length());
                            JSONObject firstHit = hitsArray.getJSONObject(idx); // Assuming you want the first hit
                            url_image = firstHit.getString("webformatURL");
                        }

                        url = new URL(url_image);
                        con = (HttpsURLConnection) url.openConnection();
                        con.setRequestMethod("GET");
                        con.connect();

                        InputStream photo_in = new BufferedInputStream(url.openStream());
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int n = 0;

                        while(-1!=(n=photo_in.read(buf))){
                            out.write(buf,0,n);
                        }

                        out.close();
                        url_in.close();
                        photo_in.close();

                        byte[] response = out.toByteArray();
                        img = BitmapFactory.decodeByteArray(response, 0, response.length);

                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                    ric.onComplete(img);
                }
            });
        }
    }
}