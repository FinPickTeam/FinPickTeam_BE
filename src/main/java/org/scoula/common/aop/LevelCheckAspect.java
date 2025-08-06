package org.scoula.common.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.scoula.coin.mapper.CoinMapper;
import org.scoula.user.service.UserService;
import org.springframework.stereotype.Component;


@Log4j2
@Aspect
@Component
@RequiredArgsConstructor
public class LevelCheckAspect {
    private final UserService userService;
    private final CoinMapper coinMapper;

    @Pointcut("execution(* org.scoula.coin.mapper.CoinMapper.addCoinAmount(..))")
    private void coinMapperMethods() {}

    @AfterReturning("coinMapperMethods()")
    public void afterReturningCoinMapperMethods(JoinPoint joinPoint) {
        log.info("포인트 누적감지, 누적포인트 체크 실행");

        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        userService.checkAndLevelUp(userId);
    }
}
