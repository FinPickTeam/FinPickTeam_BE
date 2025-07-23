package org.scoula.transactions.service;

import lombok.RequiredArgsConstructor;
import org.scoula.transactions.domain.Transaction;
import org.scoula.transactions.dto.TransactionDTO;
import org.scoula.transactions.dto.TransactionDetailDTO;
import org.scoula.transactions.exception.TransactionNotFoundException;
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
        return transactionMapper.findByUserId(userId);
    }

    @Override
    public List<TransactionDTO> getTransactionsByAccountId(Long accountId) {
        return transactionMapper.findByAccountId(accountId); // 추가
    }

    @Override
    public TransactionDetailDTO getTransactionDetail(Long id) {
        TransactionDetailDTO dto = transactionMapper.findById(id);
        if (dto == null) throw new TransactionNotFoundException("거래를 찾을 수 없습니다.");

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