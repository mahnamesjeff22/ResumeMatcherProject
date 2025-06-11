package resumematch;

import java.util.Scanner;

public class ResumeMatcher {
    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter resume content (paste from PDF):");
        String resume = scanner.useDelimiter("\A").next();

        System.out.println("Enter job description content:");
        scanner = new Scanner(System.in);
        String jobDescription = scanner.useDelimiter("\A").next();

        double score = matchScore(resume, jobDescription);
        System.out.printf("Match score: %.2f%%\n", score * 100);
    }

    private double matchScore(String resume, String jobDescription) {
        String[] resumeWords = resume.toLowerCase().split("\W+");
        String[] jobWords = jobDescription.toLowerCase().split("\W+");

        int matchCount = 0;
        for (String word : jobWords) {
            for (String resWord : resumeWords) {
                if (word.equals(resWord)) {
                    matchCount++;
                    break;
                }
            }
        }

        return (double) matchCount / jobWords.length;
    }
}
