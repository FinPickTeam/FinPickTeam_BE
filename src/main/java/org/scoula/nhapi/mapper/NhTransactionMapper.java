package org.scoula.nhapi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.nhapi.domain.Transaction;

@Mapper
public interface NhTransactionMapper {
    int insertTransaction(Transaction tx);
}
