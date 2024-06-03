package org.jilt.test;

import org.jilt.test.data.functional.LargeLanguageModel;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jilt.test.data.functional.LargeLanguageModelBuilder.largeLanguageModel;
import static org.jilt.test.data.functional.LargeLanguageModelBuilder.name;
import static org.jilt.test.data.functional.LargeLanguageModelBuilder.outputTokensLimit;
import static org.jilt.test.data.functional.LargeLanguageModelBuilder.temperature;

public class FunctionalBuilderTest {
    @Test
    public void func_builder_for_only_required_properties_works() {
        LargeLanguageModel largeLanguageModel = largeLanguageModel(
                name("my-name")
        );

        assertThat(largeLanguageModel.name).isEqualTo("my-name");
        assertThat(largeLanguageModel.temperature).isEqualTo(0.3F);
        assertThat(largeLanguageModel.outputTokensLimit).isEqualTo(100);
    }

    @Test
    public void func_builder_for_optional_properties_works() {
        LargeLanguageModel largeLanguageModel = largeLanguageModel(
                name("my-name"),
                temperature(41F),
                outputTokensLimit(50)
        );

        assertThat(largeLanguageModel.name).isEqualTo("my-name");
        assertThat(largeLanguageModel.temperature).isEqualTo(41);
        assertThat(largeLanguageModel.outputTokensLimit).isEqualTo(50);
    }

    @Test
    public void func_builder_allows_skipping_middle_optional_property() {
        LargeLanguageModel largeLanguageModel = largeLanguageModel(
                name("my-name"),
                outputTokensLimit(50)
        );

        assertThat(largeLanguageModel.name).isEqualTo("my-name");
        assertThat(largeLanguageModel.temperature).isEqualTo(0.3F);
        assertThat(largeLanguageModel.outputTokensLimit).isEqualTo(50);
    }

    @Test
    public void func_builder_allows_skipping_last_optional_property() {
        LargeLanguageModel largeLanguageModel = largeLanguageModel(
                name("my-name"),
                temperature(41F)
        );

        assertThat(largeLanguageModel.name).isEqualTo("my-name");
        assertThat(largeLanguageModel.temperature).isEqualTo(41F);
        assertThat(largeLanguageModel.outputTokensLimit).isEqualTo(100);
    }
}
