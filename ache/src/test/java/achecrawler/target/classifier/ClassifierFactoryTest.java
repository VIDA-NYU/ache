package achecrawler.target.classifier;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ClassifierFactoryTest {

    @Test
    void shouldCreateUrlRegexClassifier() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("url_regex_classifier_config/").getPath();

        TargetClassifier classifier = TargetClassifierFactory.create(path);
        assertThat(classifier, is(notNullValue()));
        assertThat(classifier, is(instanceOf(UrlRegexTargetClassifier.class)));
    }

    @Test
    void shouldCreateTitleRegexClassifier() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("title_regex_classifier_config/").getPath();

        TargetClassifier classifier = TargetClassifierFactory.create(path);
        assertThat(classifier, is(notNullValue()));
        assertThat(classifier, is(instanceOf(TitleRegexTargetClassifier.class)));
    }

    @Test
    void shouldCreateBodyRegexClassifier() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("body_regex_classifier_config/").getPath();

        TargetClassifier classifier = TargetClassifierFactory.create(path);
        assertThat(classifier, is(notNullValue()));
        assertThat(classifier, is(instanceOf(BodyRegexTargetClassifier.class)));
    }

    @Test
    void shouldCreateSmileClassifier() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("smile_classifier_config/").getPath();
        System.out.println(path);
        TargetClassifier classifier = TargetClassifierFactory.create(path);
        assertThat(classifier, is(notNullValue()));
        assertThat(classifier, is(instanceOf(SmileTargetClassifier.class)));
    }

    @Test
    void shouldNotSupportClassifierConfigWithoutYmlFile() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            // given
            String path = ClassifierFactoryTest.class.getResource("old_classifier_config/").getPath();
            TargetClassifier classifier = TargetClassifierFactory.create(path);
        });
    }

}
