package org.scoula.transactions.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.transactions.domain.Ledger;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LedgerMapper {
    List<Ledger> findLedgers(@Param("userId") Long userId,
                             @Param("from") String from,
                             @Param("to") String to,
                             @Param("category") String category);

    Ledger findLedgerDetail(@Param("userId") Long userId, @Param("ledgerId") Long ledgerId);
}

