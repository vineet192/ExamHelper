package com.example.android.examhelper.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AlgorithmUtils {
    public static double Standardsimilarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }
    /* // If you have Apache Commons Text, you can use it to calculate the edit distance:
    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    public static String getUniqueWords(List<String> input) {
        List<String> uniquewords = new ArrayList<String>();
        for (String s : input) {
            String[] words = s.replaceAll("[^A-Za-z\\s]", "").toLowerCase().split(" ");
            for (String word : words) {
                if (!uniquewords.contains(word)) {
                    uniquewords.add(word);
                }
            }
        }
        Collections.sort(uniquewords);
        String retS = uniquewords.toString();
        return retS;
    }

    public static HashMap<String, Integer> counter(List<String> questions) {
        HashMap<String, Integer> h = new HashMap<String, Integer>();
        List<String> list1, list2;

        for (int i = 0; i < questions.size(); i++) {

            for (int j = i + 1; j < questions.size(); j++) {
                if (i == questions.size() - 1) break;
                list1 = Arrays.asList(questions.get(i).split("\\s"));
                list2 = Arrays.asList(questions.get(j).split("\\s"));
                String s1 = questions.get(i);
                String s2 = questions.get(j);
                double similarityIndex = Standardsimilarity(getUniqueWords(list1), getUniqueWords(list2));
                if (Standardsimilarity(getUniqueWords(list1), getUniqueWords(list2)) >= 1.0) {

                    if (h.containsKey(questions.get(i))) {
                        h.put(questions.get(i), h.get(questions.get(i)) + 1);
                        Integer count = h.get(questions.get(i));
                        questions.remove(j);
                        j--;
                    } else {
                        h.put(questions.get(i), 2);
                        questions.remove(j);
                        j--;

                    }

                }

            }

        }

        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(h.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;

    }


}
