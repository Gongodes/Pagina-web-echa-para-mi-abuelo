package com.formalizer.jarapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransformationServiceTest {

    private TransformationService transformationService;

    @BeforeEach
    void setUp() {
        transformationService = new TransformationService();
        transformationService.init(); // Manually call init for testing since @PostConstruct might not be triggered by direct instantiation
    }

    @Test
    void makeFormal_nullInput_returnsEmptyString() {
        assertEquals("", transformationService.makeFormal(null));
    }

    @Test
    void makeFormal_emptyInput_returnsEmptyString() {
        assertEquals("", transformationService.makeFormal(""));
    }

    @Test
    void makeFormal_blankInput_returnsEmptyString() {
        assertEquals("", transformationService.makeFormal("   "));
    }

    @Test
    void makeFormal_simpleSentence_capitalizeAndAddPeriod() {
        assertEquals("This is a test.", transformationService.makeFormal("this is a test"));
    }

    @Test
    void makeFormal_alreadyFormal_noChange() {
        String formalText = "This is a formal sentence.";
        assertEquals(formalText, transformationService.makeFormal(formalText));
    }

    @Test
    void makeFormal_correctsSimpleMistake() {
        // Example: "teh" -> "the" (depends on LanguageTool's default rules)
        String inputText = "this is teh best.";
        String expectedText = "This is the best."; // LanguageTool should correct 'teh'
        String actualText = transformationService.makeFormal(inputText);
        System.out.println("Original (Teh): " + inputText + " -> Transformed: " + actualText);
        assertEquals(expectedText, actualText);
    }

    @Test
    void makeFormal_handlesSentenceWithoutProperEnd() {
        assertEquals("Hello world.", transformationService.makeFormal("Hello world"));
    }

    @Test
    void makeFormal_keepsExistingQuestionMark() {
        assertEquals("Is this a test?", transformationService.makeFormal("is this a test?"));
    }

    @Test
    void makeFormal_keepsExistingExclamationMark() {
        assertEquals("Wow!", transformationService.makeFormal("wow!"));
    }

    @Test
    void makeFormal_complexInformalText() {
        String informal = "u wanna go to store l8r, cuz i need stuff";
        String formal = transformationService.makeFormal(informal);
        System.out.println("Original (Complex): " + informal + " -> Transformed: " + formal);
        // Assertions here are tricky as LanguageTool's output can be complex.
        // We check for general improvements: capitalization, period, and absence of obvious slang.
        assertTrue(formal.startsWith("Y") || formal.startsWith("D")); // "You want to go..." or "Do you want to go..."
        assertTrue(formal.endsWith("."));
        assertFalse(formal.contains(" u "));
        assertFalse(formal.contains("wanna"));
        assertFalse(formal.contains("l8r"));
        assertFalse(formal.contains("cuz"));
        assertTrue(formal.toLowerCase().contains("you want to go") || formal.toLowerCase().contains("do you want to go"));
    }
}
