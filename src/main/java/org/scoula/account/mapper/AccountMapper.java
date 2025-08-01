package org.scoula.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.account.domain.Account;

@Mapper
public interface AccountMapper {

    void insert(Account account); // useGeneratedKeys="true" 또는 XML 필요
    void updateBalance(String finAccountNumber, java.math.BigDecimal balance);
    Long findIdByFinAccount(String finAccountNumber); // 필요한 경우만
}
