package org.jilt.test.data.functional;

import org.jilt.Builder;
import org.jilt.BuilderStyle;
import org.jilt.Opt;

@Builder(style = BuilderStyle.FUNCTIONAL, toBuilder = "toBuilder", buildMethod = "create")
public final class LargeLanguageModel {
    public final String name;
    @Opt public final Float temperature;
    @Opt public final Integer outputTokensLimit;

    public LargeLanguageModel(String name, Float temperature, Integer outputTokensLimit) {
        this.name = name;
        this.temperature =  temperature == null ? 0.3F : temperature;
        this.outputTokensLimit = outputTokensLimit == null ? 100 : outputTokensLimit;
    }
}
