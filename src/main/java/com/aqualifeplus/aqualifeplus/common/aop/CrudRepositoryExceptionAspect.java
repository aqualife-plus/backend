package com.aqualifeplus.aqualifeplus.common.aop;

import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CrudRepositoryExceptionAspect {
    @Around("execution(* org.springframework.data.repository.CrudRepository.*(..))")
    public Object handleCrudRepositoryExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // CRUD 작업 실행
            return joinPoint.proceed();
        } catch (DataIntegrityViolationException e) {
            // 데이터 무결성 위반 예외 처리
            throw new CustomException(ErrorCode.DATA_INTEGRITY_VIOLATION_IN_JPA);
        } catch (DataAccessException e) {
            // 데이터베이스 접근 실패 예외 처리
            throw new CustomException(ErrorCode.DATA_ACCESS_ERROR_IN_JPA);
        } catch (Exception e) {
            // 기타 모든 예외 처리
            throw new CustomException(ErrorCode.UNEXPECTED_ERROR_IN_JPA);
        }
    }
}
