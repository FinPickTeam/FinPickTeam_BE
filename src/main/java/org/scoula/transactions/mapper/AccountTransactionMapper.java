package org.scoula.transactions.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.transactions.domain.AccountTransaction;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AccountTransactionMapper {

    void insert(AccountTransaction tx);

    List<AccountTransaction> findAccountTransactions(@Param("userId") Long userId,
                                                     @Param("accountId") Long accountId,
                                                     @Param("from") String from,
                                                     @Param("to") String to);

    boolean existsByUserIdAndTuNo(@Param("userId") Long userId,
                                  @Param("tuNo") Long tuNo);

    LocalDateTime findLastTransactionDate(@Param("accountId") Long accountId);
}
