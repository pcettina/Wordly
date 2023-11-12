package edu.fandm.pcettina.wordly;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private Map<String, List<String>> graph;

    public Graph(Context context, String fileName) {
        this.graph = new HashMap<>();
        buildGraph(context, fileName);
    }

    private void buildGraph(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();

        try (InputStream inputStream = assetManager.open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase();
                Log.println(Log.WARN, "MSG", "word: " + word);

                if (isValidWord(word)) {
                    addWordToGraph(word);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidWord(String word) {
        return word.length() == 4 && word.matches("[a-zA-Z]+");
    }

    private void addWordToGraph(String word) {
        graph.putIfAbsent(word, new ArrayList<>());
        for (String neighbor : findNeighbors(word)) {
            graph.get(word).add(neighbor);

            // Make the relationship bidirectional
            graph.putIfAbsent(neighbor, new ArrayList<>());
            graph.get(neighbor).add(word);
        }
    }

    private List<String> findNeighbors(String word) {
        List<String> neighbors = new ArrayList<>();
        char[] chars = word.toCharArray();

        for (int i = 0; i < word.length(); i++) {
            char originalChar = chars[i];
            for (char c = 'a'; c <= 'z'; c++) {
                if (c != originalChar) {
                    chars[i] = c;
                    String neighbor = new String(chars);
                    if (graph.containsKey(neighbor)) {
                        neighbors.add(neighbor);
                    }
                }
            }
            chars[i] = originalChar; // Restore the original character
        }

        return neighbors;
    }

    public Map<String, List<String>> getGraph() {
        return graph;
    }
}


