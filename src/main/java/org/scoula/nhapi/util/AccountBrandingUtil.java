package org.scoula.nhapi.util;

import java.util.*;

public final class AccountBrandingUtil {
    public record AccountBranding(
            String bankCode, String bankName, String accountType,
            String productName, String accountNumber) {}

    // 입출금(=DEPOSIT)
    public static AccountBranding pickDeposit(long userId) {
        Random r = BankCatalog.rng(userId, "acct:deposit");
        var bank = BankCatalog.BANKS.get(r.nextInt(BankCatalog.BANKS.size()));
        String prod = bank.demand[r.nextInt(bank.demand.length)];
        String acct = String.format("%s-%04d-%06d", bank.code, r.nextInt(10_000), r.nextInt(1_000_000));
        return new AccountBranding(bank.code, bank.name, "DEPOSIT", prod, acct);
    }

    // 저축(=SAVING)
    public static AccountBranding pickSaving(long userId) {
        Random r = BankCatalog.rng(userId, "acct:saving");
        var bank = BankCatalog.BANKS.get(r.nextInt(BankCatalog.BANKS.size()));
        String prod = bank.saving[r.nextInt(bank.saving.length)];
        String acct = String.format("%s-%04d-%06d", bank.code, r.nextInt(10_000), r.nextInt(1_000_000));
        return new AccountBranding(bank.code, bank.name, "SAVING", prod, acct);
    }
}
