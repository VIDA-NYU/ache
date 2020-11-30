package achecrawler.target.classifier;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ClassifierFactoryTest {

    @Test
    public void shouldCreateUrlRegexClassifier() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("url_regex_classifier_config/").getPath();

        TargetClassifier classifier = TargetClassifierFactory.create(path);
        assertThat(classifier, is(notNullValue()));
        assertThat(classifier, is(instanceOf(UrlRegexTargetClassifier.class)));
    }
    
    @Test
    public void shouldCreateTitleRegexClassifier() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("title_regex_classifier_config/").getPath();

        TargetClassifier classifier = TargetClassifierFactory.create(path);
        assertThat(classifier, is(notNullValue()));
        assertThat(classifier, is(instanceOf(TitleRegexTargetClassifier.class)));
    }
    
    @Test
    public void shouldCreateBodyRegexClassifier() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("body_regex_classifier_config/").getPath();

        TargetClassifier classifier = TargetClassifierFactory.create(path);
        assertThat(classifier, is(notNullValue()));
        assertThat(classifier, is(instanceOf(BodyRegexTargetClassifier.class)));
    }
    
    @Test
    public void shouldCreateSmileClassifier() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("smile_classifier_config/").getPath();
        System.out.println(path);
        TargetClassifier classifier = TargetClassifierFactory.create(path);
        assertThat(classifier, is(notNullValue()));
        assertThat(classifier, is(instanceOf(SmileTargetClassifier.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSupportClassifierConfigWithoutYmlFile() throws Exception {
        // given
        String path = ClassifierFactoryTest.class.getResource("old_classifier_config/").getPath();
        TargetClassifier classifier = TargetClassifierFactory.create(path);
    }

}
