package edu.fandm.pcettina.wordly;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivity extends AppCompatActivity {

    private Graph wordGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Replace this with the actual path to your dictionary file
        String filePath = "path/to/your/dictionary.txt";
        wordGraph = new Graph(filePath);

        final EditText startWordEditText = findViewById(R.id.editTextStart);
        final EditText endWordEditText = findViewById(R.id.editTextEnd);
        final TextView resultTextView = findViewById(R.id.textViewResult);

        Button findPathButton = findViewById(R.id.buttonFindPath);
        findPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String startWord = startWordEditText.getText().toString().trim().toLowerCase();
                String endWord = endWordEditText.getText().toString().trim().toLowerCase();

                List<String> path = findPath(wordGraph.getGraph(), startWord, endWord);

                if (path != null) {
                    StringBuilder pathString = new StringBuilder("Valid Path: ");
                    for (String word : path) {
                        pathString.append(word).append(" -> ");
                    }
                    pathString.delete(pathString.length() - 4, pathString.length());
                    resultTextView.setText(pathString.toString());
                } else {
                    resultTextView.setText("No valid path found between the two words.");
                }
            }
        });
    }

    private List<String> findPath(Map<String, List<String>> graph, String startWord, String endWord) {
        if (!graph.containsKey(startWord) || !graph.containsKey(endWord)) {
            return null; // Either start or end word is not in the graph
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
