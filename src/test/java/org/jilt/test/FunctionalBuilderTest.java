package org.jilt.test;

import org.jilt.test.data.functional.SomeModel;
import org.jilt.test.data.functional.SomeModelBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jilt.test.data.functional.SomeModelBuilder.maxOutputTokens;
import static org.jilt.test.data.functional.SomeModelBuilder.modelName;
import static org.jilt.test.data.functional.SomeModelBuilder.temperature;

public class FunctionalBuilderTest {
    @Test
    public void classic_functional_builder_works() {
        SomeModel someModel = SomeModelBuilder.someModel(
                maxOutputTokens(13),
                temperature(36.6F),
                modelName("my model")
        );

        assertThat(someModel.modelName).isEqualTo("my model");
        assertThat(someModel.temperature).isEqualTo(36.6F);
        assertThat(someModel.maxOutputTokens).isEqualTo(13);
    }
}
