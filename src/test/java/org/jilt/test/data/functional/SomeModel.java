package org.jilt.test.data.functional;

public final class SomeModel {
    public final String modelName;
    public final Float temperature;
    public final Integer maxOutputTokens;

    public SomeModel(String modelName, Float temperature, Integer maxOutputTokens) {
        this.modelName = modelName;
        this.temperature =  temperature == null ? 0.3F : temperature;
        this.maxOutputTokens = maxOutputTokens == null ? 100 : maxOutputTokens;
    }
}
