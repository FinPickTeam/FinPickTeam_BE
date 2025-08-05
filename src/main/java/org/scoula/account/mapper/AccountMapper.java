package org.scoula.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.account.domain.Account;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AccountMapper {

    void insert(Account account);
    void updateBalanceByUser(
            @Param("userId") Long userId,
            @Param("pinAccountNumber") String pinAccountNumber,
            @Param("balance") BigDecimal balance
    );
    Account findById(Long accountId);// 핀어카운트로 계좌 조회
    List<Account> findByUserId(Long userId);
    void updateIsActive(@Param("id") Long id, @Param("isActive") boolean isActive);
    List<Account> findActiveByUserId(Long userId);
}