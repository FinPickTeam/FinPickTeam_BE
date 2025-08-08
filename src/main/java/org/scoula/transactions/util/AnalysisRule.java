package org.scoula.transactions.util;

import org.scoula.transactions.domain.Ledger;
import org.scoula.transactions.domain.analysis.AnalysisCode;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BiFunction;

public class AnalysisRule {
    private final AnalysisCode code;
    private final BiPredicate<Ledger, List<Ledger>> condition;
    private final BiFunction<Ledger, List<Ledger>, String> messageFunc;

    public AnalysisRule(
            AnalysisCode code,
            BiPredicate<Ledger, List<Ledger>> condition,
            BiFunction<Ledger, List<Ledger>, String> messageFunc
    ) {
        this.code = code;
        this.condition = condition;
        this.messageFunc = messageFunc;
    }

    public AnalysisCode getCode() { return code; }
    public BiPredicate<Ledger, List<Ledger>> getCondition() { return condition; }
    public BiFunction<Ledger, List<Ledger>, String> getMessageFunc() { return messageFunc; }
}
