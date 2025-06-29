package com.example.formalemail.service;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransformationService {

    private final JLanguageTool langTool;

    public TransformationService() {
        // It's good practice to initialize language tool once, as it can be resource-intensive.
        // Using AmericanEnglish, but others can be configured.
        langTool = new JLanguageTool(new AmericanEnglish());
    }

    public String makeFormal(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        try {
            List<RuleMatch> matches = langTool.check(text);

            // This is a simplistic approach to applying suggestions.
            // A more sophisticated approach would involve analyzing the type of rule match
            // and applying transformations more intelligently.
            // For now, we'll try to replace text based on the first suggestion if available.

            StringBuilder correctedText = new StringBuilder(text);
            int offset = 0; // Keep track of offset changes due to replacements

            for (RuleMatch match : matches) {
                if (!match.getSuggestedReplacements().isEmpty()) {
                    String replacement = match.getSuggestedReplacements().get(0);
                    // Only apply if the replacement is not too disruptive (e.g. very short or very long)
                    // This is a heuristic and might need refinement.
                    if (replacement.length() > 0 && replacement.length() < text.length() * 2) {
                         correctedText.replace(match.getFromPos() + offset, match.getToPos() + offset, replacement);
                         offset += replacement.length() - (match.getToPos() - match.getFromPos());
                    }
                }
            }
            // As a simple "formalization" step, ensure proper capitalization of the first letter.
            String result = correctedText.toString().trim();
            if (!result.isEmpty()) {
                result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
                // Ensure sentences end with a period if they don't have other punctuation.
                if (!result.endsWith(".") && !result.endsWith("!") && !result.endsWith("?")) {
                    result += ".";
                }
            }
            return result;

        } catch (IOException e) {
            System.err.println("Error during text transformation: " + e.getMessage());
            // Fallback to original text in case of error
            return text;
        }
    }
}
