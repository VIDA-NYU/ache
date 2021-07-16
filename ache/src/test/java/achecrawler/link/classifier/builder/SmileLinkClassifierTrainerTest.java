package achecrawler.link.classifier.builder;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Test;

import achecrawler.link.classifier.LNClassifier;
import achecrawler.util.Sampler;
import achecrawler.util.Tokenizers;
import achecrawler.util.parser.LinkNeighborhood;
import achecrawler.util.string.StopListFile;

public class SmileLinkClassifierTrainerTest {

    @Test
    public void shouldTrainAndClassifyLinkNeighborhoods() throws Exception {

        LinkClassifierTrainer classifierTrainer = new LinkClassifierTrainer(StopListFile.DEFAULT);

        LinkNeighborhood ln1 = createLinkNeighborhood("asdf");
        LinkNeighborhood ln2 = createLinkNeighborhood("asdf");
        LinkNeighborhood ln3 = createLinkNeighborhood("qwer asdf");
        LinkNeighborhood ln4 = createLinkNeighborhood("zxcv");
        LinkNeighborhood ln5 = createLinkNeighborhood("xcvb");
        LinkNeighborhood ln6 = createLinkNeighborhood("cbvn");

        Sampler<LinkNeighborhood> relevantSample = new Sampler<>(asList(ln1, ln2, ln3));
        Sampler<LinkNeighborhood> irrelevantSample = new Sampler<>(asList(ln4, ln5, ln6));

        List<Sampler<LinkNeighborhood>> instances = asList(relevantSample, irrelevantSample);
        List<String> classValues = asList("RELEVANT", "IRRELEVANT");

        LNClassifier classifier = classifierTrainer.trainLNClassifier(instances, classValues);

        assertThat(classifier, notNullValue());

        for (LinkNeighborhood ln : relevantSample.getSamples()) {
            double[] probs = classifier.classify(ln);
            assertThat(ln.getLink().toString(), probs[0], is(greaterThan(0.5d)));
            assertThat(ln.getLink().toString(), probs[1], is(lessThan(0.5d)));
        }

        for (LinkNeighborhood ln : irrelevantSample.getSamples()) {
            double[] probs = classifier.classify(ln);
            assertThat(ln.getLink().toString(), probs[0], is(lessThan(0.5d)));
            assertThat(ln.getLink().toString(), probs[1], is(greaterThan(0.5d)));
        }

    }

    @Test
    public void shouldWorkWithMultipleClasses() throws Exception {

        LinkClassifierTrainer classifierTrainer = new LinkClassifierTrainer(StopListFile.DEFAULT);

        LinkNeighborhood ln1 = createLinkNeighborhood("asdf");
        LinkNeighborhood ln2 = createLinkNeighborhood("asdf");
        LinkNeighborhood ln3 = createLinkNeighborhood("qwer qwer");
        LinkNeighborhood ln4 = createLinkNeighborhood("qwer");
        LinkNeighborhood ln5 = createLinkNeighborhood("zxcv");
        LinkNeighborhood ln6 = createLinkNeighborhood("zxcv");

        Sampler<LinkNeighborhood> sample1 = new Sampler<>(asList(ln1, ln2));
        Sampler<LinkNeighborhood> sample2 = new Sampler<>(asList(ln3, ln4));
        Sampler<LinkNeighborhood> sample3 = new Sampler<>(asList(ln5, ln6));

        List<Sampler<LinkNeighborhood>> instances = asList(sample1, sample2, sample3);
        List<String> classValues = asList("level0", "level1", "level2");

        LNClassifier classifier = classifierTrainer.trainLNClassifier(instances, classValues);

        assertThat(classifier, notNullValue());

        for (LinkNeighborhood ln : sample1.getSamples()) {
            double[] probs = classifier.classify(ln);
            assertThat(ln.getLink().toString(), probs[0], is(greaterThan(0.5d)));
            assertThat(ln.getLink().toString(), probs[1], is(lessThan(0.5d)));
            assertThat(ln.getLink().toString(), probs[2], is(lessThan(0.5d)));
        }

        for (LinkNeighborhood ln : sample2.getSamples()) {
            double[] probs = classifier.classify(ln);
            assertThat(ln.getLink().toString(), probs[0], is(lessThan(0.5d)));
            assertThat(ln.getLink().toString(), probs[1], is(greaterThan(0.5d)));
            assertThat(ln.getLink().toString(), probs[2], is(lessThan(0.5d)));
        }

        for (LinkNeighborhood ln : sample3.getSamples()) {
            double[] probs = classifier.classify(ln);
            assertThat(ln.getLink().toString(), probs[0], is(lessThan(0.5d)));
            assertThat(ln.getLink().toString(), probs[1], is(lessThan(0.5d)));
            assertThat(ln.getLink().toString(), probs[2], is(greaterThan(0.5d)));
        }

    }

    private LinkNeighborhood createLinkNeighborhood(String term) throws MalformedURLException {
        String url = "http://example.com/" + term + "/";
        LinkNeighborhood ln = new LinkNeighborhood(new URL(url));
        ln.setAnchor(Tokenizers.whitespace().tokenizeToArray("anchor text " + term));
        ln.setAround(Tokenizers.whitespace().tokenizeToArray("around " + term));
        return ln;
    }

}
