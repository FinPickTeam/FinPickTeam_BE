package org.scoula.account.util;

import java.util.*;

public final class BankCatalog {
    public static final class Bank {
        public final String code, name;
        public final String[] demand, saving, cardCredit, cardDebit, bins;
        public Bank(String code, String name, String[] demand, String[] saving,
                    String[] cardCredit, String[] cardDebit, String[] bins) {
            this.code = code; this.name = name; this.demand = demand; this.saving = saving;
            this.cardCredit = cardCredit; this.cardDebit = cardDebit; this.bins = bins;
        }
    }

    public static final List<Bank> BANKS = List.of(
            new Bank("004","KB국민은행",
                    new String[]{"KB 주거래우대통장","KB 마이핏 통장","KB Star 통장","KB 모임통장"},
                    new String[]{"KB 자유적금","KB 1년 정기적금","KB 청년도약적금"},
                    new String[]{"국민 톡톡","리브메이트"},
                    new String[]{"국민 노리체크","리브 체크"},
                    new String[]{"7018","5311"}),
            new Bank("088","신한은행",
                    new String[]{"신한 주거래 통장","신한 Deep 통장","신한 플러스 통장"},
                    new String[]{"신한 자유적금","신한 정기적금","신한 청년우대형 적금"},
                    new String[]{"신한 Deep","Mr.Life"},
                    new String[]{"신한 체크","S20 체크"},
                    new String[]{"9410","4305"}),
            new Bank("020","우리은행",
                    new String[]{"우리 WON 통장","우리 주거래 통장"},
                    new String[]{"우리 자유적금","우리 정기적금","우리 청년 적금"},
                    new String[]{"카드의정석","WON 카드"},
                    new String[]{"우리 체크","WON 체크"},
                    new String[]{"4573","4048"}),
            new Bank("081","하나은행",
                    new String[]{"하나 1Q 통장","하나 주거래 통장"},
                    new String[]{"하나 자유적금","하나 정기적금","하나 청년 적금"},
                    new String[]{"하나 1Q","멤버스"},
                    new String[]{"하나 체크","1Q 체크"},
                    new String[]{"5540","4386"}),
            new Bank("011","NH농협은행",
                    new String[]{"NH 올원 입출금","NH 주거래 통장"},
                    new String[]{"NH 올원 적금","NH 자유적금","NH 청년희망 적금"},
                    new String[]{"올바른 카드"},
                    new String[]{"올원 체크"},
                    new String[]{"3560","5399"}),
            new Bank("090","카카오뱅크",
                    new String[]{"카카오 입출금","세이프박스"},
                    new String[]{"카카오 자유적금","카카오 정기예금"},
                    new String[]{"카카오뱅크 카드"},
                    new String[]{"카카오뱅크 체크"},
                    new String[]{"4704","5559"}),
            new Bank("092","토스뱅크",
                    new String[]{"토스통장","먼슬리통장"},
                    new String[]{"먼슬리적금","토스 자유적금"},
                    new String[]{"토스카드"},
                    new String[]{"토스 체크"},
                    new String[]{"5355","5390"})
    );

    public static Random rng(long userId, String ns){
        return new Random(Objects.hash(userId, ns, 2025, 8)); // 유저별 고정 랜덤
    }
}
