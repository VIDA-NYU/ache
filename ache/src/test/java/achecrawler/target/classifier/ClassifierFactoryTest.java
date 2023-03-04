package achecrawler.target.classifier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ClassifierFactoryTest {

    @Test
    void shouldCreateUrlRegexClassifier() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("url_regex_classifier_config/").getPath();

        TargetClassifier classifier = TargetClassifierFactory.create(path);
        assertThat(classifier).isNotNull();
        assertThat(classifier).isInstanceOf(UrlRegexTargetClassifier.class);
    }

    @Test
    void shouldCreateTitleRegexClassifier() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("title_regex_classifier_config/").getPath();

        TargetClassifier classifier = TargetClassifierFactory.create(path);
        assertThat(classifier).isNotNull();
        assertThat(classifier).isInstanceOf(TitleRegexTargetClassifier.class);
    }

    @Test
    void shouldCreateBodyRegexClassifier() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("body_regex_classifier_config/").getPath();

        TargetClassifier classifier = TargetClassifierFactory.create(path);
        assertThat(classifier).isNotNull();
        assertThat(classifier).isInstanceOf(BodyRegexTargetClassifier.class);
    }

    @Test
    void shouldCreateSmileClassifier() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("smile_classifier_config/").getPath();
        System.out.println(path);
        TargetClassifier classifier = TargetClassifierFactory.create(path);
        assertThat(classifier).isNotNull();
        assertThat(classifier).isInstanceOf(SmileTargetClassifier.class);
    }

    @Test
    void shouldNotSupportClassifierConfigWithoutYmlFile() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            // given
            String path = ClassifierFactoryTest.class.getResource("old_classifier_config/").getPath();
            TargetClassifier classifier = TargetClassifierFactory.create(path);
        });
    }

}
