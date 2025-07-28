package org.scoula.bubble.service;

import lombok.RequiredArgsConstructor;
import org.scoula.bubble.dto.BubbleDTO;
import org.scoula.bubble.mapper.BubbleMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BubbleServiceImpl implements BubbleService {

    final private BubbleMapper bubbleMapper;

    @Override
    public BubbleDTO getBubble() {
        return BubbleDTO.of(bubbleMapper.get());
    }
}
