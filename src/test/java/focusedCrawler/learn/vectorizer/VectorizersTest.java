package focusedCrawler.learn.vectorizer;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
        SparseVector v1 = vectorizer.fitTransform(i1);
        SparseVector v2 = vectorizer.fitTransform(i2);

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
        vectorizer.partialFit(i1);
        SparseVector v2 = vectorizer.transform(i2);

        // then
        assertThat(vectorizer.numberOfFeatures(), is(2));

        assertThat(v2.get("asdf", vectorizer), is(1d));
        assertThat(v2.get("sdfg", vectorizer), is(0d)); // is not know by vectorizer
        assertThat(v2.get("zxcv", vectorizer), is(0d));
    }

    @Test
    public void shouldCreateQuadraticFeatures() {
        // given
        BinaryTextVectorizer vectorizer = new BinaryTextVectorizer(Tokenizers.alphaNumeric(), true);
        String i1 = "asdf qwer";
        String i2 = "asdf sdfg qwer";

        // when
        vectorizer.partialFit(i1);

        SparseVector v2 = vectorizer.transform(i2);

        // then
        assertThat(vectorizer.numberOfFeatures(), is(3));

        assertThat(v2.get("asdf", vectorizer), is(1d));
        assertThat(v2.get("asdfqwer", vectorizer), is(1d));
        assertThat(v2.get("sdfg", vectorizer), is(0d)); // is not know by vectorizer
        assertThat(v2.get("zxcv", vectorizer), is(0d));
    }

    @Test
    public void shouldStackVectors() {
        // given
        BinaryTextVectorizer vectorizer1 = new BinaryTextVectorizer();
        String i1 = "asdf";
        vectorizer1.partialFit(i1);

        BinaryTextVectorizer vectorizer2 = new BinaryTextVectorizer();
        String i2 = "qwer";
        vectorizer2.partialFit(i2);

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
