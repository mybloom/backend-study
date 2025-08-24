package com.example.demo.application;

import com.example.demo.infrastructure.WalletRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OuterService {
    private final WalletRepository repo;
    private final InnerService inner;
    @PersistenceContext
    EntityManager em;

    @Transactional // REQUIRED
    public void outer_calls_required_then_runtime(Long id) {
        repo.findById(id).orElseThrow().plus(10);
        inner.required_addAndThrowRuntime(id, 100); // 전체 롤백 기대
    }

    @Transactional
    public void outer_calls_requiresNew_and_catch(Long id) {
        repo.findById(id).orElseThrow().plus(10); // 바깥 누적
        try {
            inner.requiresNew_addAndThrowRuntime(id, 100); // 안쪽만 롤백
        } catch (RuntimeException ignore) { /* swallow */ }
        // 바깥은 계속 진행 → 커밋 예상
        em.flush();
    }

    @Transactional
    public void outer_calls_nested_markRollbackOnly(Long id) {
        repo.findById(id).orElseThrow().plus(10);
        inner.nested_add_thenMarkRollbackOnly(id, 100); // 저장점 롤백
        em.flush(); // 바깥은 커밋 가능
    }

    @Transactional
    public void outer_calls_required_checked(Long id) {
        repo.findById(id).orElseThrow().plus(10);
        try {
            inner.required_checkedException(id, 100); // rollbackFor로 롤백
        } catch (Exception e) {
            // 전파되면서 바깥도 롤백됨
            throw new RuntimeException(e);
        }
    }
}
