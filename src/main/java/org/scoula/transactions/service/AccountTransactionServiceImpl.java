package org.scoula.transactions.service;

import lombok.RequiredArgsConstructor;
import org.scoula.transactions.domain.AccountTransaction;
import org.scoula.transactions.dto.AccountTransactionDto;
import org.scoula.transactions.exception.AccountTransactionNotFoundException;
import org.scoula.transactions.mapper.AccountTransactionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountTransactionServiceImpl implements AccountTransactionService {

    private final AccountTransactionMapper mapper;

    @Override
    public List<AccountTransactionDto> getTransactions(Long userId, Long accountId, String from, String to) {
        List<AccountTransaction> txList = mapper.findAccountTransactions(userId, accountId, from, to);

        if (txList == null || txList.isEmpty()) {
            throw new AccountTransactionNotFoundException();
        }

        return txList.stream().map(this::toDto).toList();
    }

    private AccountTransactionDto toDto(AccountTransaction tx) {
        AccountTransactionDto dto = new AccountTransactionDto();
        dto.setId(tx.getId());
        dto.setUserId(tx.getUserId());
        dto.setAccountId(tx.getAccountId());
        dto.setDate(tx.getDate());
        dto.setType(tx.getType());
        dto.setAmount(tx.getAmount());
        dto.setBalance(tx.getBalance());
        dto.setPlace(tx.getPlace());
        return dto;
    }
}
