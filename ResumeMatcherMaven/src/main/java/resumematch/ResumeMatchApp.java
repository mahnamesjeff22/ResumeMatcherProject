package resumematch;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.geometry.Insets;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.nio.file.Files;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class ResumeMatchApp extends Application {

    private File resumeFile;
    private File jobFile;
    private Label resultLabel;
    private Label resumeStatusLabel;
    private Label jobStatusLabel;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Resume Matcher");

        Button chooseResumeBtn = new Button("Choose Resume PDF");
        Button chooseJobBtn = new Button("Choose Job Description PDF");
        Button compareBtn = new Button("Compare");
        
        resumeStatusLabel = new Label("No resume selected");
        jobStatusLabel = new Label("No job description selected");
        resultLabel = new Label("Match Score: --");

        chooseResumeBtn.setOnAction(e -> {
           resumeFile = chooseFile(stage);
            if (resumeFile != null) {
            resumeStatusLabel.setText("Resume selected ✅");
}
        });

        chooseJobBtn.setOnAction(e -> {
            jobFile = chooseFile(stage);
                if (jobFile != null) {
                jobStatusLabel.setText("Job description selected ✅");
}
        });

        compareBtn.setOnAction(e -> {
            if (resumeFile != null && jobFile != null) {
                String resumeText = extractTextFromPDF(resumeFile);
                String jobText = extractTextFromPDF(jobFile);
                double score = matchScore(resumeText, jobText);
                resultLabel.setText(String.format("Match Score: %.2f%%", score * 100));
            } else {
                resultLabel.setText("Please select both PDF files.");
            }
        });

        VBox root = new VBox(10,
        chooseResumeBtn, resumeStatusLabel,
        chooseJobBtn, jobStatusLabel,
        compareBtn, resultLabel);

        root.setPadding(new Insets(20));
        stage.setScene(new Scene(root, 400, 200));
        stage.show();
    }

    private File chooseFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        return fileChooser.showOpenDialog(stage);
    }

    private String extractTextFromPDF(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            return new PDFTextStripper().getText(document);
        } catch (IOException e) {
            return "";
        }
    }

    private double matchScore(String resume, String jobDescription) {
        String[] stopWords = { "the", "and", "a", "an", "of", "to", "for", "in", "on", "with", "is", "at", "by", "from" };
        Set<String> stopWordSet = new HashSet<>(Arrays.asList(stopWords));

        Set<String> resumeWords = new HashSet<>();
        for (String word : resume.toLowerCase().split("\\W+")) {
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
