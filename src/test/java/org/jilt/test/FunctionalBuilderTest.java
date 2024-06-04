package org.jilt.test;

import org.jilt.test.data.functional.LargeLanguageModel;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jilt.test.data.functional.LargeLanguageModelBuilder.Optional.outputTokensLimit;
import static org.jilt.test.data.functional.LargeLanguageModelBuilder.Optional.temperature;
import static org.jilt.test.data.functional.LargeLanguageModelBuilder.largeLanguageModel;
import static org.jilt.test.data.functional.LargeLanguageModelBuilder.name;
import static org.jilt.test.data.functional.LargeLanguageModelBuilder.toBuilder;

public class FunctionalBuilderTest {
    @Test
    public void func_builder_for_only_required_properties_works() {
        LargeLanguageModel llm = largeLanguageModel(
                name("my-name")
        );

        assertThat(llm.name).isEqualTo("my-name");
        assertThat(llm.temperature).isEqualTo(0.3F);
        assertThat(llm.outputTokensLimit).isEqualTo(100);
    }

    @Test
    public void func_builder_for_optional_properties_works() {
        LargeLanguageModel llm = largeLanguageModel(
                name(null),
                temperature(41F),
                outputTokensLimit(50)
        );

        assertThat(llm.name).isNull();
        assertThat(llm.temperature).isEqualTo(41);
        assertThat(llm.outputTokensLimit).isEqualTo(50);
    }

    @Test
    public void func_builder_allows_skipping_middle_optional_property() {
        LargeLanguageModel llm = largeLanguageModel(
                name(""),
                outputTokensLimit(50)
        );

        assertThat(llm.name).isEmpty();
        assertThat(llm.temperature).isEqualTo(0.3F);
        assertThat(llm.outputTokensLimit).isEqualTo(50);
    }

    @Test
    public void func_builder_allows_skipping_last_optional_property() {
        LargeLanguageModel llm = largeLanguageModel(
                name(" "),
                temperature(41F)
        );

        assertThat(llm.name).isEqualTo(" ");
        assertThat(llm.temperature).isEqualTo(41F);
        assertThat(llm.outputTokensLimit).isEqualTo(100);
    }

    @Test
    public void func_builder_toBuilder_without_changes_works() {
        LargeLanguageModel llm = new LargeLanguageModel("my-name", null, null);
        LargeLanguageModel copy = toBuilder(llm);

        assertThat(copy.name).isEqualTo("my-name");
        assertThat(copy.temperature).isEqualTo(0.3F);
        assertThat(copy.outputTokensLimit).isEqualTo(100);
    }

    @Test
    public void func_builder_toBuilder_with_only_required_prop_works() {
        LargeLanguageModel llm = new LargeLanguageModel("my-name", null, null);
        LargeLanguageModel copy = toBuilder(llm, name("changed-name"));

        assertThat(copy.name).isEqualTo("changed-name");
        assertThat(copy.temperature).isEqualTo(0.3F);
        assertThat(copy.outputTokensLimit).isEqualTo(100);
    }

    @Test
    public void func_builder_toBuilder_with_only_optional_prop_works() {
        LargeLanguageModel llm = new LargeLanguageModel("my-name", null, null);
        LargeLanguageModel copy = toBuilder(llm, temperature(20F));

        assertThat(copy.name).isEqualTo("my-name");
        assertThat(copy.temperature).isEqualTo(20F);
        assertThat(copy.outputTokensLimit).isEqualTo(100);
    }

    @Test
    public void func_builder_toBuilder_with_all_props_works() {
        LargeLanguageModel llm = new LargeLanguageModel("my-name", null, null);
        LargeLanguageModel copy = toBuilder(llm,
                outputTokensLimit(50), temperature(99F), name("changed-name"), temperature(20F));

        assertThat(copy.name).isEqualTo("changed-name");
        assertThat(copy.temperature).isEqualTo(20F);
        assertThat(copy.outputTokensLimit).isEqualTo(50);
    }
}
