    package org.jilt.test.data.functional;

public class SomeModelBuilder {
    public static interface Setter {
        void accept(SomeModelBuilder builder);
    }

    public static Setter modelName(final String modelName) {
        return new Setter() {
            @Override
            public void accept(SomeModelBuilder builder) {
                builder.modelName = modelName;
            }
        };
    }

    public static Setter temperature(final Float temperature) {
        return new Setter() {
            @Override
            public void accept(SomeModelBuilder builder) {
                builder.temperature = temperature;
            }
        };
    }

    public static Setter maxOutputTokens(final Integer maxOutputTokens) {
        return new Setter() {
            @Override
            public void accept(SomeModelBuilder builder) {
                builder.maxOutputTokens = maxOutputTokens;
            }
        };
    }

    public static SomeModel someModel(Setter... setters) {
        SomeModelBuilder builder = new SomeModelBuilder();
        for (Setter setter : setters) {
            setter.accept(builder);
        }
        return new SomeModel(
                builder.modelName,
                builder.temperature,
                builder.maxOutputTokens
        );
    }

    private String modelName;
    private Float temperature;
    private Integer maxOutputTokens;

    private SomeModelBuilder() {
    }
}
