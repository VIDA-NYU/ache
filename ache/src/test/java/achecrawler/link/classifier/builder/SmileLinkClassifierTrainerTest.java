package achecrawler.link.classifier.builder;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import achecrawler.link.classifier.LNClassifier;
import achecrawler.util.Sampler;
import achecrawler.util.Tokenizers;
import achecrawler.util.parser.LinkNeighborhood;
import achecrawler.util.string.Stopwords;

import org.junit.jupiter.api.Test;

class SmileLinkClassifierTrainerTest {

    @Test
    void shouldTrainAndClassifyLinkNeighborhoods() throws Exception {

        LinkClassifierTrainer classifierTrainer = new LinkClassifierTrainer(Stopwords.DEFAULT);

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

        assertThat(classifier).isNotNull();

        for (LinkNeighborhood ln : relevantSample.getSamples()) {
            double[] probs = classifier.classify(ln);
            assertThat(probs[0]).as(ln.getLink().toString()).isGreaterThan(0.5d);
            assertThat(probs[1]).as(ln.getLink().toString()).isLessThan(0.5d);
        }

        for (LinkNeighborhood ln : irrelevantSample.getSamples()) {
            double[] probs = classifier.classify(ln);
            assertThat(probs[0]).as(ln.getLink().toString()).isLessThan(0.5d);
            assertThat(probs[1]).as(ln.getLink().toString()).isGreaterThan(0.5d);
        }

    }

    @Test
    void shouldWorkWithMultipleClasses() throws Exception {

        LinkClassifierTrainer classifierTrainer = new LinkClassifierTrainer(Stopwords.DEFAULT);

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

        assertThat(classifier).isNotNull();

        for (LinkNeighborhood ln : sample1.getSamples()) {
            double[] probs = classifier.classify(ln);
            assertThat(probs[0]).as(ln.getLink().toString()).isGreaterThan(0.5d);
            assertThat(probs[1]).as(ln.getLink().toString()).isLessThan(0.5d);
            assertThat(probs[2]).as(ln.getLink().toString()).isLessThan(0.5d);
        }

        for (LinkNeighborhood ln : sample2.getSamples()) {
            double[] probs = classifier.classify(ln);
            assertThat(probs[0]).as(ln.getLink().toString()).isLessThan(0.5d);
            assertThat(probs[1]).as(ln.getLink().toString()).isGreaterThan(0.5d);
            assertThat(probs[2]).as(ln.getLink().toString()).isLessThan(0.5d);
        }

        for (LinkNeighborhood ln : sample3.getSamples()) {
            double[] probs = classifier.classify(ln);
            assertThat(probs[0]).as(ln.getLink().toString()).isLessThan(0.5d);
            assertThat(probs[1]).as(ln.getLink().toString()).isLessThan(0.5d);
            assertThat(probs[2]).as(ln.getLink().toString()).isGreaterThan(0.5d);
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
