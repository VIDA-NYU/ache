package focusedCrawler.learn.vectorizer;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import focusedCrawler.tokenizers.Tokenizers;

public class VectorizersTest {

    @Test
    public void shouldCreateSparseVectors() {
        // given
        BinaryTextVectorizer vectorizer = new BinaryTextVectorizer();
        String i1 = "asdf qwer";
        String i2 = "asdf sdfg";

        // when
        vectorizer.fit(Arrays.asList(i1, i2));
        SparseVector v1 = vectorizer.transform(i1);
        SparseVector v2 = vectorizer.transform(i2);

        // then
        assertThat(v1.get("asdf", vectorizer), is(1d));
        assertThat(v1.get("qwer", vectorizer), is(1d));
        assertThat(v1.get("zxcv", vectorizer), is(0d));

        assertThat(v2.get("asdf", vectorizer), is(1d));
        assertThat(v2.get("sdfg", vectorizer), is(1d));
        assertThat(v2.get("zxcv", vectorizer), is(0d));

        assertThat(vectorizer.numberOfFeatures(), is(3));
    }

    @Test
    public void shouldRememberVocabulary() {
        // given
        BinaryTextVectorizer vectorizer = new BinaryTextVectorizer();
        String i1 = "asdf qwer";
        String i2 = "asdf sdfg";

        // when
        vectorizer.fit(Arrays.asList(i1));
        SparseVector v2 = vectorizer.transform(i2);

        // then
        assertThat(vectorizer.numberOfFeatures(), is(2));

        assertThat(v2.get("asdf", vectorizer), is(1d));
        assertThat(v2.get("sdfg", vectorizer), is(0d)); // is not know by vectorizer
        assertThat(v2.get("zxcv", vectorizer), is(0d));
    }

    @Test
    public void shouldGenerateNgrams() {
        // given
        BinaryTextVectorizer vectorizer = new BinaryTextVectorizer.Builder()
                .withNgramSize(4)
                .withMinDocFrequency(0)
                .build();
        String i1 = "asdf zxcv qwer sdfg";

        // when
        vectorizer.fit(Arrays.asList(i1));

        SparseVector v = vectorizer.transform(i1);

        // then
        assertThat(vectorizer.numberOfFeatures(), is(10));

        assertThat(v.get("asdf", vectorizer), is(1d));
        assertThat(v.get("zxcv", vectorizer), is(1d));
        assertThat(v.get("qwer", vectorizer), is(1d));
        assertThat(v.get("sdfg", vectorizer), is(1d));

        assertThat(v.get("asdf_zxcv", vectorizer), is(1d));
        assertThat(v.get("zxcv_qwer", vectorizer), is(1d));
        assertThat(v.get("qwer_sdfg", vectorizer), is(1d));

        assertThat(v.get("asdf_zxcv_qwer", vectorizer), is(1d));
        assertThat(v.get("zxcv_qwer_sdfg", vectorizer), is(1d));

        assertThat(v.get("asdf_zxcv_qwer_sdfg", vectorizer), is(1d));

        assertThat(v.get("zzzz", vectorizer), is(0d)); // is not know by vectorizer
    }

    @Test
    public void shouldFilterFeaturesByMinDocumentFrequency() {
        // given
        BinaryTextVectorizer vectorizer = new BinaryTextVectorizer.Builder()
                .withMinDocFrequency(2)
                .build();
        String i1 = "asdf qwer uiop";
        String i2 = "asdf qwer sdfg";
        String i3 = "asdf tyui";

        // when
        vectorizer.fit(Arrays.asList(i1, i2, i3));
        SparseVector v2 = vectorizer.transform(i2);

        // then: assert that there only 2 features are used
        assertThat(vectorizer.numberOfFeatures(), is(2));

        assertThat(v2.get("asdf", vectorizer), is(1d));
        assertThat(v2.get("qwer", vectorizer), is(1d));
        assertThat(v2.get("sdfg", vectorizer), is(0d)); // should be ignored by vectorizer
        assertThat(v2.get("tyui", vectorizer), is(0d));
    }

    @Test
    public void shouldFilterFeaturesByMaximumSize() {
        // given
        int maxFeatures = 2;
        BinaryTextVectorizer vectorizer = new BinaryTextVectorizer.Builder()
                .withMaxFeatures(maxFeatures)
                .build();
        String i1 = "asdf uiop gggg";
        String i2 = "asdf qwer sdfg";
        String i3 = "asdf qwer tyui";
        String i4 = "asdf qwer tyui";

        // when
        vectorizer.fit(Arrays.asList(i1, i2, i3, i4));
        SparseVector v2 = vectorizer.transform(i2);

        // then: assert that there only 2 features are used
        assertThat(vectorizer.numberOfFeatures(), is(maxFeatures));

        assertThat(v2.get("asdf", vectorizer), is(1d));
        assertThat(v2.get("qwer", vectorizer), is(1d));
        assertThat(v2.get("sdfg", vectorizer), is(0d)); // should be ignored by vectorizer
        assertThat(v2.get("tyui", vectorizer), is(0d));
        assertThat(v2.get("uiop", vectorizer), is(0d));
        assertThat(v2.get("gggd", vectorizer), is(0d));
    }

    @Test
    public void shouldComputeNaiveBayesLogRatioFeatures() {
        // given
        BinaryTextVectorizer vectorizer = new BinaryTextVectorizer.Builder()
                .withMinDocFrequency(1)
                .withWeightType(BinaryTextVectorizer.WeightType.NB_LOG_RATIO)
                .build();

        String i1 = "asdf uiop";
        String i2 = "asdf qwer sdfg";
        String i3 = "zxcv qwer cvbn";
        String i4 = "zxcv hjkl";

        String i5 = "asdf zxcv qwer uiop hjkl";

        // when
        vectorizer.fit(
                Arrays.asList(i1, i2, i3, i4),
                Arrays.asList(0, 0, 1, 1));

        SparseVector v = vectorizer.transform(i5);

        // then
        assertThat(vectorizer.numberOfFeatures(), is(7));

        double asdf = v.get("asdf", vectorizer);
        double zxcv = v.get("zxcv", vectorizer);
        double qwer = v.get("qwer", vectorizer);
        double uiop = v.get("uiop", vectorizer);
        double hjkl = v.get("hjkl", vectorizer);

        // "asdf" only happens on positive class,
        // so it should be the most positive feature
        assertTrue(asdf > zxcv);
        assertTrue(asdf > qwer);
        assertTrue(asdf > uiop);
        assertTrue(asdf > hjkl);

        // "zxcv" only happens on negative class,
        // so it should be the most negative feature
        assertTrue(zxcv < asdf);
        assertTrue(zxcv < hjkl);
        assertTrue(zxcv < qwer);
        assertTrue(zxcv < hjkl);

        // "qwer" appears equally in both classes,
        // "uiop" is more positive, and "hjkl" is more negative
        assertTrue(qwer == 0d);
        assertTrue(uiop > qwer);
        assertTrue(hjkl < qwer);
    }

    @Test
    public void shouldCreateQuadraticFeatures() {
        // given
        BinaryTextVectorizer vectorizer = new BinaryTextVectorizer(Tokenizers.alphaNumeric(), true);
        String i1 = "asdf qwer";
        String i2 = "asdf sdfg qwer";

        // when
        vectorizer.fit(Arrays.asList(i1));

        SparseVector v2 = vectorizer.transform(i2);

        // then
        assertThat(vectorizer.numberOfFeatures(), is(3));

        assertThat(v2.get("asdf", vectorizer), is(1d));
        assertThat(v2.get("asdf_qwer", vectorizer), is(1d));
        assertThat(v2.get("sdfg", vectorizer), is(0d)); // is not know by vectorizer
        assertThat(v2.get("zxcv", vectorizer), is(0d));
    }

    @Test
    public void shouldStackVectors() {
        // given
        BinaryTextVectorizer vectorizer1 = new BinaryTextVectorizer();
        String i1 = "asdf";
        vectorizer1.fit(Arrays.asList(i1));

        BinaryTextVectorizer vectorizer2 = new BinaryTextVectorizer();
        String i2 = "qwer";
        vectorizer2.fit(Arrays.asList(i2));

        String i3 = "asdf qwer";

        // when
        FeatureStackVectorizer stackVectorizer =
                new FeatureStackVectorizer(vectorizer1, vectorizer2);
        SparseVector v1 = vectorizer1.transform(i3);
        SparseVector v2 = vectorizer2.transform(i3);
        SparseVector v3 = stackVectorizer.transform(i3);


        // then
        assertThat(vectorizer1.numberOfFeatures(), is(1));
        assertThat(vectorizer2.numberOfFeatures(), is(1));
        assertThat(stackVectorizer.numberOfFeatures(), is(2));

        assertThat(v1.get("asdf", vectorizer1), is(1d));
        assertThat(v1.get("qwer", vectorizer1), is(0d));

        assertThat(v2.get("asdf", vectorizer2), is(0d));
        assertThat(v2.get("qwer", vectorizer2), is(1d));

        assertThat(v3.get("asdf", stackVectorizer), is(1d));
        assertThat(v3.get("qwer", stackVectorizer), is(1d));
        assertThat(v3.get("zxcv", stackVectorizer), is(0d));
    }

    @Test
    public void shouldHashVectorize() {
        HashingVectorizer hv = new HashingVectorizer(Tokenizers.alphaNumeric(), 4, false);
        String i1 = "asdf qwer sdfg";
        SparseVector v1 = hv.transform(i1);

        String i2 = "asdf zxcv wert";
        hv.transform(i2);
        SparseVector v2 = hv.transform(i2);

        List<String> features = hv.getFeatures();
        for (int i = 0; i < hv.numberOfFeatures(); i++) {
            System.out.println(features.get(i) + " " + v1.get(i) + " " + v2.get(i));
        }

    }

}
