package resumematch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ResumeMatcher {
    public void run() {
    Scanner scanner = new Scanner(System.in);

    System.out.println("Paste your resume (type ':end' on a new line when done):");
    String resume = readMultilineInput(scanner);

    System.out.println("Paste job description (type ':end' on a new line when done):");
    String jobDescription = readMultilineInput(scanner);

    double score = matchScore(resume, jobDescription);
    System.out.printf("Match score: %.2f%%\n", score * 100);
}

private String readMultilineInput(Scanner scanner) {
    StringBuilder input = new StringBuilder();
    while (true) {
        String line = scanner.nextLine();
        if (line.equalsIgnoreCase(":end")) break;
        input.append(line).append(" ");
    }
    return input.toString();
}


   private double matchScore(String resume, String jobDescription) {
    String[] stopWords = { "the", "and", "a", "an", "of", "to", "for", "in", "on", "with", "is", "at", "by", "from" };

    Set<String> stopWordSet = new HashSet<>(Arrays.asList(stopWords));

    Set<String> resumeWords = new HashSet<>();
    for (String word : resume.toLowerCase().split("\\W+")) { // W+ makes the sentence into words and passes digits and letters.
        if (!stopWordSet.contains(word) && word.length() > 1) {
            resumeWords.add(word);
        }
    }

    String[] jobWords = jobDescription.toLowerCase().split("\\W+");
    int matchCount = 0;

    for (String word : jobWords) {
        if (!stopWordSet.contains(word) && word.length() > 1 && resumeWords.contains(word)) {
            matchCount++;
        }
    }

    return (double) matchCount / jobWords.length;
}

}
