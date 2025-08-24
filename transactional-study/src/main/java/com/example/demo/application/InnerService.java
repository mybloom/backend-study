package com.example.demo.application;

import com.example.demo.infrastructure.WalletRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
@RequiredArgsConstructor
public class InnerService {
    private final WalletRepository repo;
    @PersistenceContext
    EntityManager em;

    @Transactional(propagation = Propagation.REQUIRED)
    public void required_addAndThrowRuntime(Long id, long amt) {
        repo.findById(id).orElseThrow().plus(amt);
        em.flush(); // DB까지 즉시 반영 시도
        throw new RuntimeException("inner required boom");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNew_addAndThrowRuntime(Long id, long amt) {
        repo.findById(id).orElseThrow().plus(amt);
        em.flush();
        throw new RuntimeException("inner requires_new boom");
    }

    @Transactional(propagation = Propagation.NESTED)
    public void nested_add_thenMarkRollbackOnly(Long id, long amt) {
        repo.findById(id).orElseThrow().plus(amt);
        em.flush();
        // 부분 롤백만 원하면 예외 삼키고 저장점만 롤백
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void required_checkedException(Long id, long amt) throws Exception {
        repo.findById(id).orElseThrow().plus(amt);
        em.flush();
        throw new Exception("checked but rollbackFor 지정");
    }
}
