package resumematch;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
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

        resultLabel = new Label("Match Score: --");
        resultLabel.setStyle("-fx-font-size: 16px;");

        resumeStatusLabel = new Label("No resume selected");
        jobStatusLabel = new Label("No job description selected");

        chooseResumeBtn.setOnAction(e -> {
            resumeFile = chooseFile(stage);
            if (resumeFile != null) {
                resumeStatusLabel.setText("✅ " + resumeFile.getName());
            }
        });

        chooseJobBtn.setOnAction(e -> {
            jobFile = chooseFile(stage);
            if (jobFile != null) {
                jobStatusLabel.setText("✅ " + jobFile.getName());
            }
        });

        compareBtn.setOnAction(e -> {
            if (resumeFile != null && jobFile != null) {
                String resumeText = extractTextFromPDF(resumeFile);
                String jobText = extractTextFromPDF(jobFile);
                double score = matchScore(resumeText, jobText);
                String scoreText = String.format("Match Score: %.2f%%", score * 100);
                resultLabel.setText(scoreText);

                if (score >= 0.7) {
                    resultLabel.setTextFill(Color.GREEN);
                } else if (score >= 0.4) {
                    resultLabel.setTextFill(Color.ORANGE);
                } else {
                    resultLabel.setTextFill(Color.RED);
                }
            } else {
                resultLabel.setText("Please select both PDF files.");
                resultLabel.setTextFill(Color.BLACK);
            }
        });

        VBox root = new VBox(10,
                chooseResumeBtn, resumeStatusLabel,
                chooseJobBtn, jobStatusLabel,
                compareBtn, resultLabel);
        root.setPadding(new Insets(20));
        stage.setScene(new Scene(root, 450, 280));
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
        String[] resumeTokens = resume.toLowerCase().split("\\W+");
        String[] jobTokens = jobDescription.toLowerCase().split("\\W+");

        Set<String> vocabSet = new HashSet<>();
        vocabSet.addAll(Arrays.asList(resumeTokens));
        vocabSet.addAll(Arrays.asList(jobTokens));
        List<String> vocab = new ArrayList<>(vocabSet);

        double[] resumeVector = toTFIDFVector(resumeTokens, jobTokens, vocab);
        double[] jobVector = toTFIDFVector(jobTokens, resumeTokens, vocab);


        return cosineSimilarity(resumeVector, jobVector);
    }

    private double[] toTFIDFVector(String[] tokens, String[] otherTokens, List<String> vocab) {
    double[] vector = new double[vocab.size()];
    Map<String, Integer> freq = new HashMap<>();

    for (String token : tokens) {
        freq.put(token, freq.getOrDefault(token, 0) + 1);
    }

    for (int i = 0; i < vocab.size(); i++) {
        String term = vocab.get(i);
        int tf = freq.getOrDefault(term, 0);

        int docCount = 0;
        if (termIn(tokens, term)) docCount++;
        if (termIn(otherTokens, term)) docCount++;

        double idf = Math.log(2.0 / (1.0 + docCount)); // Avoid division by 0
        vector[i] = tf * idf;
    }

    return vector;
}
    private boolean termIn(String[] tokens, String term) {
        for (String t : tokens) {
            if (t.equals(term)) return true;
        }
        return false;
    }

    private double cosineSimilarity(double[] vec1, double[] vec2) {
        double dot = 0, mag1 = 0, mag2 = 0;
        for (int i = 0; i < vec1.length; i++) {
            dot += vec1[i] * vec2[i];
            mag1 += vec1[i] * vec1[i];
            mag2 += vec2[i] * vec2[i];
        }
        return (mag1 == 0 || mag2 == 0) ? 0 : dot / (Math.sqrt(mag1) * Math.sqrt(mag2));
    }
}
