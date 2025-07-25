package org.scoula.transactions.service;

import lombok.RequiredArgsConstructor;
import org.scoula.transactions.domain.Transaction;
import org.scoula.transactions.dto.TransactionDTO;
import org.scoula.transactions.dto.TransactionDetailDTO;
import org.scoula.transactions.exception.TransactionNotFoundException;
import org.scoula.transactions.exception.UserNotFoundException;
import org.scoula.transactions.exception.AccountNotFoundException;
import org.scoula.transactions.mapper.TransactionMapper;
import org.scoula.transactions.util.RuleBasedAnalyzer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionMapper transactionMapper;

    @Override
    public List<TransactionDTO> getTransactionsByUserId(Long userId) {
        List<TransactionDTO> transactions = transactionMapper.findByUserId(userId);
        if (transactions == null || transactions.isEmpty()) {
            throw new UserNotFoundException(userId);
        }
        return transactions;
    }

    @Override
    public List<TransactionDTO> getTransactionsByAccountId(Long accountId) {
        List<TransactionDTO> transactions = transactionMapper.findByAccountId(accountId);
        if (transactions == null || transactions.isEmpty()) {
            throw new AccountNotFoundException(accountId);
        }
        return transactions;
    }

    @Override
    public TransactionDetailDTO getTransactionDetail(Long id) {
        TransactionDetailDTO dto = transactionMapper.findById(id);
        if (dto == null) throw new TransactionNotFoundException(id);

        if (dto.getAnalysisText() == null || dto.getAnalysisText().isEmpty()) {
            List<Transaction> recent = transactionMapper.findRecentTransactionsByUser(
                    dto.getUserId(), dto.getDate().minusDays(7)
            );
            String analysis = RuleBasedAnalyzer.analyze(dto, recent);
            transactionMapper.updateAnalysisText(id, analysis);
            dto.setAnalysisText(analysis);
        }

        return dto;
    }
}
