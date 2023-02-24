package achecrawler.util.string;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StopwordsTest {

    @Test
    public void shouldCheckIfWordIsIrrelevant() {
      Stopwords stopwords = Stopwords.DEFAULT;
      assertTrue(stopwords.isIrrelevant("the"));
      assertTrue(stopwords.isIrrelevant("of"));
      assertFalse(stopwords.isIrrelevant("animal"));
    }

}