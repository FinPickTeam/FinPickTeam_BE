package org.scoula.transactions.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.transactions.domain.AccountTransaction;

import java.util.List;

@Mapper
public interface AccountTransactionMapper {
    List<AccountTransaction> findByUserAndAccount(@Param("userId") Long userId, @Param("accountId") Long accountId);
    void insert(AccountTransaction tx);
}
