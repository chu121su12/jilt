package org.jilt.test.data.functional;

public class SomeModelBuilderStaged {
    public static interface SetterModelName {
        void accept(SomeModelBuilderStaged builder);
    }
    public static SetterModelName modelName(final String modelName) {
        return new SetterModelName() {
            @Override
            public void accept(SomeModelBuilderStaged builder) {
                builder.modelName = modelName;
            }
        };
    }

    public static interface SetterTemperature {
        void accept(SomeModelBuilderStaged builder);
    }
    public static SetterTemperature temperature(final Float temperature) {
        return new SetterTemperature() {
            @Override
            public void accept(SomeModelBuilderStaged builder) {
                builder.temperature = temperature;
            }
        };
    }

    public static interface SetterMaxOutputTokens {
        void accept(SomeModelBuilderStaged builder);
    }
    public static SetterMaxOutputTokens maxOutputTokens(final Integer maxOutputTokens) {
        return new SetterMaxOutputTokens() {
            @Override
            public void accept(SomeModelBuilderStaged builder) {
                builder.maxOutputTokens = maxOutputTokens;
            }
        };
    }

    public static SomeModel someModel(
            SetterModelName setterModelName,
            SetterTemperature setterTemperature,
            SetterMaxOutputTokens setterMaxOutputTokens
    ) {
        SomeModelBuilderStaged builder = new SomeModelBuilderStaged();
        setterModelName.accept(builder);
        setterTemperature.accept(builder);
        setterMaxOutputTokens.accept(builder);
        return new SomeModel(
                builder.modelName,
                builder.temperature,
                builder.maxOutputTokens
        );
    }

    private String modelName;
    private Float temperature;
    private Integer maxOutputTokens;

    private SomeModelBuilderStaged() {
    }
}
