package edu.fandm.pcettina.wordly;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MainActivity extends AppCompatActivity { //https://chat.openai.com/share/d3116b7e-75ff-4901-b2da-4367f289aea4

    private Graph wordGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Replace this with the actual path to your dictionary file

        wordGraph = new Graph(this, "words_test.txt");

        final EditText startWordEditText = findViewById(R.id.editTextStart);
        final EditText endWordEditText = findViewById(R.id.editTextEnd);
        final LinearLayout wordContainer = findViewById(R.id.wordContainer);
        final TextView resultTextView = findViewById(R.id.textViewResult);

        Button findPathButton = findViewById(R.id.buttonFindPath);
        findPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String startWord = startWordEditText.getText().toString().trim().toLowerCase();
                String endWord = endWordEditText.getText().toString().trim().toLowerCase();

                List<String> path = findPath(wordGraph.getGraph(), startWord, endWord);

                if (path != null && !path.get(0).equals("wrong")) {
                    createWordGuessUI(wordContainer, path);
                    resultTextView.setText("Try to guess the words in the path!");
//                    StringBuilder pathString = new StringBuilder("Valid Path: ");
//                    for (String word : path) {
//                        pathString.append(word).append(" -> ");
//                    }
//                    pathString.delete(pathString.length() - 4, pathString.length());
//                    resultTextView.setText(pathString.toString());
                }
                else if(path.get(0).equals("wrong")){
                    resultTextView.setText("Words not in graph.");
                }
                else {
                    resultTextView.setText("No valid path exists.");
                }
            }
        });
    }

    private void createWordGuessUI(LinearLayout wordContainer, List<String> path) {
        wordContainer.removeAllViews(); // Clear existing UI components

        for (int i = 0; i < path.size(); i++) {
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

            // Add views to the container
            wordContainer.addView(wordTextView);
            wordContainer.addView(guessEditText);
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
}
