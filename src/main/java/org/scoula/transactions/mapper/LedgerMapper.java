package org.scoula.transactions.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.transactions.domain.Ledger;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LedgerMapper {
    List<Ledger> findLedgerByUserId(Long userId);
    Ledger findLedgerDetail(@Param("userId") Long userId, @Param("ledgerId") Long ledgerId);
    void insert(Ledger ledger);
}
