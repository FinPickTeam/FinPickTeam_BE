package org.scoula.nhapi.util;

import java.util.*;

public final class CardBrandingUtil {
    public record CardBranding(String bankName, String cardName, String masked, String cardType) {}

    public static CardBranding pickForUser(long userId, boolean credit) {
        Random r = BankCatalog.rng(userId, "cardBrand:" + (credit?"C":"D"));
        var bank = BankCatalog.BANKS.get(r.nextInt(BankCatalog.BANKS.size()));
        String name = (credit ? bank.cardCredit : bank.cardDebit)[r.nextInt(credit ? bank.cardCredit.length : bank.cardDebit.length)];
        String bin  = bank.bins[r.nextInt(bank.bins.length)];
        String masked = String.format("%s-****-****-%04d", bin, 1000 + r.nextInt(9000));
        return new CardBranding(bank.name, name, masked, credit ? "CREDIT" : "DEBIT");
    }
}