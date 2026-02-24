package com.luxzentao.javabar.core;

import java.io.Serializable;
import java.util.Map;

public class LandlordPromptEventDef implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LandlordPromptEventId id;
    private final String promptText;
    private final Map<LandlordPromptOption, String> optionTexts;
    private final Map<LandlordPromptOption, Map<LandlordPromptResultType, LandlordPromptOutcome>> outcomes;

    public LandlordPromptEventDef(LandlordPromptEventId id,
                                   String promptText,
                                   Map<LandlordPromptOption, String> optionTexts,
                                   Map<LandlordPromptOption, Map<LandlordPromptResultType, LandlordPromptOutcome>> outcomes) {
        this.id = id;
        this.promptText = promptText;
        this.optionTexts = optionTexts;
        this.outcomes = outcomes;
    }

    public LandlordPromptEventId getId() { return id; }
    public String getPromptText() { return promptText; }
    public String getOptionText(LandlordPromptOption option) { return optionTexts.get(option); }
    public LandlordPromptOutcome getOutcome(LandlordPromptOption option, LandlordPromptResultType resultType) {
        return outcomes.get(option).get(resultType);
    }
    public Map<LandlordPromptOption, String> getOptionTexts() { return optionTexts; }
}
