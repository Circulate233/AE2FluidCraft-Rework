package com.glodblock.github.integration.jei;

import javax.annotation.Nullable;

public class WrappedIngredient<T> {

    @Nullable
    private final T ingredient;
    private final boolean isInput;

    public WrappedIngredient(@Nullable final T ingredient, final boolean isInput) {
        this.ingredient = ingredient;
        this.isInput = isInput;
    }

    @Nullable
    public T getIngredient() {
        return ingredient;
    }

    public boolean isInput() {
        return isInput;
    }

}
