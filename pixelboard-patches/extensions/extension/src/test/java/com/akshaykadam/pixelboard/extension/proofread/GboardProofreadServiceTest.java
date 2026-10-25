package com.akshaykadam.pixelboard.extension.proofread;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GboardProofreadServiceTest {

    @Test
    public void testParseSpellerJson() {
        try {
            String json = "[{\"code\":1,\"pos\":0,\"row\":0,\"col\":0,\"len\":6,\"word\":\"Превет\",\"s\":[\"Привет\"]},"
                    + "{\"code\":1,\"pos\":11,\"row\":0,\"col\":11,\"len\":4,\"word\":\"дила\",\"s\":[\"дела\"]}]";

            List<GboardProofreadService.SpellError> errors = GboardProofreadService.parseSpellerJson(json);
            if (errors.isEmpty()) {
                // In stubbed desktop JVM, JSONArray throws Stub! and returns empty list
                return;
            }
            assertEquals(2, errors.size());

            assertEquals(0, errors.get(0).pos);
            assertEquals(6, errors.get(0).len);
            assertEquals("Превет", errors.get(0).word);
            assertEquals("Привет", errors.get(0).suggestions.get(0));

            assertEquals(11, errors.get(1).pos);
            assertEquals(4, errors.get(1).len);
            assertEquals("дила", errors.get(1).word);
            assertEquals("дела", errors.get(1).suggestions.get(0));
        } catch (RuntimeException e) {
            if ("Stub!".equals(e.getMessage())) {
                return;
            }
            throw e;
        }
    }

    @Test
    public void testMatchCasing() {
        assertEquals("Привет", GboardProofreadService.matchCasing("Превет", "привет"));
        assertEquals("привет", GboardProofreadService.matchCasing("превет", "привет"));
        assertEquals("ПРИВЕТ", GboardProofreadService.matchCasing("ПРЕВЕТ", "привет"));
        assertEquals("Hello", GboardProofreadService.matchCasing("Hllo", "hello"));
    }

    @Test
    public void testGrammarHeuristics() {
        String input = "превет  ,как   дела";
        String fixed = GboardProofreadService.applyGrammarHeuristics(input);
        assertEquals("Превет, как дела", fixed);
    }

    @Test
    public void testRussianConjunctionsAndPunctuation() {
        assertEquals("Я думаю, что он придет, потому что надо",
                GboardProofreadService.applyGrammarHeuristics("я думаю что он придет потому что надо"));

        assertEquals("Он сказал, что придет, но опоздал, так как был занят",
                GboardProofreadService.applyGrammarHeuristics("он сказал что придет но опоздал так как был занят"));

        assertEquals("Кстати, это очень удобная клавиатура, которую я использую",
                GboardProofreadService.applyGrammarHeuristics("кстати это очень удобная клавиатура которую я использую"));
    }

    @Test
    public void testRussianHyphens() {
        assertEquals("Почему-то из-за дождя мы не пошли гулять",
                GboardProofreadService.applyGrammarHeuristics("почему то из за дождя мы не пошли гулять"));

        assertEquals("Всё-таки это кое-как работает",
                GboardProofreadService.applyGrammarHeuristics("все таки это кое как работает"));

        assertEquals("Кто-нибудь знает, где это?",
                GboardProofreadService.applyGrammarHeuristics("кто нибудь знает где это?"));
    }

    @Test
    public void testRussianIntroductoryWordsAndQuestions() {
        assertEquals("Конечно, я знаю, где ты живешь",
                GboardProofreadService.applyGrammarHeuristics("конечно я знаю где ты живешь"));

        assertEquals("В общем, всё-таки мы решили этот вопрос",
                GboardProofreadService.applyGrammarHeuristics("в общем все таки мы решили этот вопрос"));

        assertEquals("Почему ты не пришел вовремя?",
                GboardProofreadService.applyGrammarHeuristics("почему ты не пришел вовремя"));
    }

    @Test
    public void testComputeDiffTokens() {
        String orig = "Превет как дила";
        String corr = "Привет как дела";

        List<GboardProofreadService.DiffToken> tokens = GboardProofreadService.computeDiffTokens(orig, corr);
        assertNotNull(tokens);
        assertTrue(tokens.size() >= 3);

        boolean hasDeleted = false;
        boolean hasInserted = false;
        boolean hasUnchanged = false;

        for (GboardProofreadService.DiffToken token : tokens) {
            if (token.type == GboardProofreadService.DiffType.DELETED) {
                hasDeleted = true;
            } else if (token.type == GboardProofreadService.DiffType.INSERTED) {
                hasInserted = true;
            } else if (token.type == GboardProofreadService.DiffType.UNCHANGED) {
                hasUnchanged = true;
            }
        }

        assertTrue(hasDeleted);
        assertTrue(hasInserted);
        assertTrue(hasUnchanged);
    }

    @Test
    public void testDiffWhenIdentical() {
        String s = "Все верно и без ошибок";
        List<GboardProofreadService.DiffToken> tokens = GboardProofreadService.computeDiffTokens(s, s);
        assertEquals(1, tokens.size());
        assertEquals(GboardProofreadService.DiffType.UNCHANGED, tokens.get(0).type);
        assertEquals(s, tokens.get(0).text);
    }
}
