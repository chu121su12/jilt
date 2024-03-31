package org.jilt.test;

import org.jilt.test.data.functional.SomeModel;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jilt.test.data.functional.SomeModelBuilderStaged.maxOutputTokens;
import static org.jilt.test.data.functional.SomeModelBuilderStaged.modelName;
import static org.jilt.test.data.functional.SomeModelBuilderStaged.someModel;
import static org.jilt.test.data.functional.SomeModelBuilderStaged.temperature;

public class FunctionalBuilderStagedTest {
    @Test
    public void staged_functional_builder_without_optionals_works() {
            SomeModel someModel = someModel(
                    modelName("my model"),
                    temperature(36.6F),
                    maxOutputTokens(13)
            );

            assertThat(someModel.modelName).isEqualTo("my model");
            assertThat(someModel.temperature).isEqualTo(36.6F);
            assertThat(someModel.maxOutputTokens).isEqualTo(13);
    }
}
