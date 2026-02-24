package com.luxzentao.javabar.core;

import java.io.Serializable;
import java.util.List;

public class LandlordPromptOutcome implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LandlordPromptResultType resultType;
    private final LandlordPromptEffectPackage effectPackage;
    private final List<String> textVariants; // 3 narrative text variants

    public LandlordPromptOutcome(LandlordPromptResultType resultType,
                                  LandlordPromptEffectPackage effectPackage,
                                  List<String> textVariants) {
        this.resultType = resultType;
        this.effectPackage = effectPackage;
        this.textVariants = textVariants;
    }

    public LandlordPromptResultType getResultType() { return resultType; }
    public LandlordPromptEffectPackage getEffectPackage() { return effectPackage; }
    public List<String> getTextVariants() { return textVariants; }
    public String getRandomText(java.util.Random random) {
        return textVariants.get(random.nextInt(textVariants.size()));
    }
}
