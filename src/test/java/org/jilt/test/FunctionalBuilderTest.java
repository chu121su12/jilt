package org.jilt.test;

import org.jilt.test.data.functional.LargeLanguageModel;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jilt.test.data.functional.LargeLanguageModelBuilder.largeLanguageModel;
import static org.jilt.test.data.functional.LargeLanguageModelBuilder.name;

public class FunctionalBuilderTest {
    @Test
    public void func_builder_for_required_properties_works() {
        LargeLanguageModel largeLanguageModel = largeLanguageModel(
                name("my-name")
        );

        assertThat(largeLanguageModel.name).isEqualTo("my-name");
        assertThat(largeLanguageModel.temperature).isEqualTo(0.3F);
        assertThat(largeLanguageModel.outputTokensLimit).isEqualTo(100);
    }
}
