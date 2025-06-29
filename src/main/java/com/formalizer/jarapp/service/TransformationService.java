package com.formalizer.jarapp.service;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct; // Changed from javax.annotation
import java.io.IOException;
import java.util.List;
// import java.util.stream.Collectors; // Not strictly needed for current simple replacement

@Service
public class TransformationService {

    private JLanguageTool langTool;

    // Initialize LanguageTool after constructing the service
    // Using @PostConstruct is a good practice for resource-intensive initializations.
    @PostConstruct
    public void init() {
        // Using AmericanEnglish, but others can be configured.
        // Consider making the language configurable if needed in the future.
        langTool = new JLanguageTool(new AmericanEnglish());
        // You could load more rules or customize it here if necessary
        // For example, to disable certain rules:
        // langTool.disableRule("UPPERCASE_SENTENCE_START");
    }

    public String makeFormal(String text) {
        if (text == null || text.trim().isEmpty()) {
            return ""; // Return empty for null or effectively empty input
        }

        try {
            List<RuleMatch> matches = langTool.check(text);

            StringBuilder correctedText = new StringBuilder(text);
            int offset = 0; // Keep track of offset changes due to replacements

            for (RuleMatch match : matches) {
                if (!match.getSuggestedReplacements().isEmpty()) {
                    String replacement = match.getSuggestedReplacements().get(0);
                    // Basic heuristic: apply if replacement is not empty and seems reasonable
                    if (replacement != null && !replacement.isEmpty()) {
                         correctedText.replace(match.getFromPos() + offset, match.getToPos() + offset, replacement);
                         offset += replacement.length() - (match.getToPos() - match.getFromPos());
                    }
                }
            }

            String result = correctedText.toString().trim(); // Trim again after corrections

            // Ensure proper capitalization of the first letter if not empty.
            if (!result.isEmpty()) {
                result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
                // Ensure sentences end with appropriate punctuation if not already present.
                // This is a very basic check. More sophisticated sentence boundary detection might be needed for complex texts.
                char lastChar = result.charAt(result.length() - 1);
                if (Character.isLetterOrDigit(lastChar)) {
                    result += ".";
                }
            }
            return result;

        } catch (IOException e) {
            System.err.println("Error during text transformation with LanguageTool: " + e.getMessage());
            // Fallback to a simpler formalization (capitalize first, add period) or original text
            String fallbackResult = text.trim();
            if (!fallbackResult.isEmpty()) {
                fallbackResult = Character.toUpperCase(fallbackResult.charAt(0)) + fallbackResult.substring(1);
                 char lastChar = fallbackResult.charAt(fallbackResult.length() - 1);
                if (Character.isLetterOrDigit(lastChar)) {
                    fallbackResult += ".";
                }
                return fallbackResult;
            }
            return text; // Absolute fallback
        }
    }
}
