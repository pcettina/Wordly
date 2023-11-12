package edu.fandm.pcettina.wordly;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph { //https://chat.openai.com/share/b21d698f-3e1f-404d-94d2-52c77dc69b99
    private Map<String, List<String>> graph;

    public Graph(String filePath) {
        this.graph = new HashMap<>();
        buildGraph(filePath);
    }

    private void buildGraph(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase();
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

    public static void main(String[] args) {
        String filePath = "path/to/your/dictionary.txt"; // Replace with the actual path
        Graph wordGraph = new Graph(filePath);

        // Access the graph
        Map<String, List<String>> graph = wordGraph.getGraph();

        // Print the graph (for demonstration purposes)
        for (Map.Entry<String, List<String>> entry : graph.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}

