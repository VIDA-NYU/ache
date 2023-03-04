package achecrawler.util.string;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StopwordsTest {

    @Test
    void shouldCheckIfWordIsIrrelevant() {
      Stopwords stopwords = Stopwords.DEFAULT;
      assertTrue(stopwords.isIrrelevant("the"));
      assertTrue(stopwords.isIrrelevant("of"));
      assertFalse(stopwords.isIrrelevant("animal"));
    }

}