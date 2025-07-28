package org.scoula.nhapi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.nhapi.domain.Account;
import java.math.BigDecimal;

@Mapper
public interface NhAccountMapper {
    void insertAccount(Account account);
    void updateBalance(@Param("pinAccountNumber") String finAccount, @Param("balance") BigDecimal balance);
    Long findIdByFinAccount(String pinAccountNumber);
    Long findUserIdByFinAccount(String pinAccountNumber);
    Account findByAccountNumber(@Param("accountNumber") String accountNumber);

}