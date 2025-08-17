package org.scoula.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.account.domain.Account;

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
    void updateIsActive(@Param("id") Long id, @Param("isActive") boolean isActive);
    List<Account> findActiveByUserId(Long userId);
    List<Account> findByIdList(List<Long> ids);
    BigDecimal sumBalanceByUserId(Long userId);
    int countByUserAndType(@Param("userId") Long userId, @Param("type") String type);
    int countByUserAndProductName(Long userId, String productName);
    List<String> findProductNamesByUser(Long userId);
}