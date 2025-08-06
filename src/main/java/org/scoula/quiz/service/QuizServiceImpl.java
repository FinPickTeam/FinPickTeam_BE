package org.scoula.quiz.service;

import lombok.RequiredArgsConstructor;
import org.scoula.coin.mapper.CoinMapper;
import org.scoula.quiz.domain.QuizHistoryDetailVO;
import org.scoula.quiz.domain.QuizHistoryVO;
import org.scoula.quiz.domain.QuizVO;
import org.scoula.quiz.dto.QuizDTO;
import org.scoula.quiz.dto.QuizHistoryDTO;
import org.scoula.quiz.dto.QuizHistoryDetailDTO;
import org.scoula.quiz.exception.QuizAlreadyTakenTodayException;
import org.scoula.quiz.exception.QuizNotFoundException;
import org.scoula.quiz.mapper.QuizMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    final private QuizMapper quizMapper;
    final private CoinMapper coinMapper;

    @Override
    public QuizDTO getQuiz(Long userId) {

        //금일 퀴즈응시한 데이터가 있는지 검사
        if(quizMapper.isQuizTakenToday(userId)>0){
            throw new QuizAlreadyTakenTodayException(userId);
        }
        QuizVO quizVO = quizMapper.getQuiz(userId);

        QuizDTO quizDTO = QuizDTO.of(quizVO);

        //응시가능한 데이터가 없을 경우, 예외처리
        if(quizDTO == null){
            throw new QuizNotFoundException(userId);
        }

        return quizDTO;
    }

    @Override
    public void submit(QuizHistoryDTO dto) {
        QuizHistoryVO vo = dto.toVO();
        coinMapper.addCoinAmount(dto.getUserId(),10);
        coinMapper.insertCoinHistory(dto.getUserId(), 10, "plus", "QUIZ");
        quizMapper.insertHistory(vo);
    }

    @Override
    public List<QuizHistoryDetailDTO> getHistoryList(Long userId) {
        List<QuizHistoryDetailVO> quizHistoryDetailVOList = quizMapper.getHistoryList(userId); //historyVO 리스트 불러오기
        List<QuizHistoryDetailDTO> quizHistoryDetailDTOList = new ArrayList<>(); //DTO리스트 선언

        for(QuizHistoryDetailVO quizHistoryDetailVO : quizHistoryDetailVOList){ //for문으로 VO를 DTO로 변환
            quizHistoryDetailDTOList.add(QuizHistoryDetailDTO.of(quizHistoryDetailVO));
        }
        return  quizHistoryDetailDTOList;
    }

    @Override
    public QuizHistoryDetailDTO getHistoryDetail(Long historyId) {
        QuizHistoryDetailVO quizHistoryDetailVO=quizMapper.getHistoryDetail(historyId);

        return QuizHistoryDetailDTO.of(quizHistoryDetailVO);
    }
}
