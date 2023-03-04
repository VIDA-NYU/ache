package achecrawler.seedfinder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class RelevanceModelTest {

    @Test
    void shouldExecuteQuery() throws Exception {
        // given
        RelevanceModel model = new RelevanceModel();
        
        String[] docTerm1 = {"asdf", "qwer", "asdf", "zxcv"};
        String[] docTerm2 = {"asdf", "qwer", "asdf"};
        String[] docTerm3 = {"asdf", "poiu"};
        
        Set<String> asdf = new HashSet<String>();
        asdf.add("asdf");
        
        Set<String> asdfqwer = new HashSet<String>();
        asdfqwer.add("asdf");
        asdfqwer.add("qwer");
        
        // when
        model.addPage(true,  docTerm1);
        model.addPage(true,  docTerm2);
        model.addPage(false, docTerm3);
        
        // then
        assertThat(model.getTermsWithBestScore().term, is("asdf"));
        assertThat(model.getTermWithBestScoreExcept(asdf).term, is("qwer"));
        assertThat(model.getTermWithBestScoreExcept(asdfqwer).term, is("zxcv"));
    }

}
