# 테스트 단계 로그 (사용자 측 “Act” 표식)
2025-08-24T00:56:41.810+09:00  INFO 28710 --- [    Test worker] com.example.demo.application.TxLabTest   : >>> Act

# 바깥 트랜잭션 시작(@Transactional REQUIRED)
2025-08-24T00:56:41.811+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [com.example.demo.application.OuterService.outer_calls_requiresNew_and_catch]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
# 바깥 Tx용 EM 오픈
2025-08-24T00:56:41.811+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(932629072<open>)] for JPA transaction
# JDBC 핸들 노출
2025-08-24T00:56:41.812+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@55154761]
# AOP 인터셉터가 바깥 트랜잭션 경계 진입 감지
2025-08-24T00:56:41.812+09:00 TRACE 28710 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [com.example.demo.application.OuterService.outer_calls_requiresNew_and_catch]
# 동일 스레드에 바인딩된 EM 사용(바깥 Tx 계속)
2025-08-24T00:56:41.812+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(932629072<open>)] for JPA transaction
# 기존 트랜잭션에 참여(합승)
2025-08-24T00:56:41.812+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Participating in existing transaction

# 바깥 서비스 내부에서 지갑 조회(findById)
2025-08-24T00:56:41.812+09:00 TRACE 28710 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]
# SELECT 실행(id=1)
2025-08-24T00:56:41.814+09:00 DEBUG 28710 --- [    Test worker] org.hibernate.SQL                        :
select
w1_0.id,
w1_0.balance,
w1_0.owner
from
wallet w1_0
where
w1_0.id=?
# 파라미터 바인딩
2025-08-24T00:56:41.815+09:00 TRACE 28710 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
# 조회 트랜잭션 경계 종료(바깥 Tx는 계속 유지)
2025-08-24T00:56:41.816+09:00 TRACE 28710 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]

# === REQUIRES_NEW 진입: 바깥 Tx 일시정지 + 내부 독립 Tx 시작 ===
# 바깥 Tx에 바인딩된 EM 확인
2025-08-24T00:56:41.816+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(932629072<open>)] for JPA transaction
# "바깥 Tx를 suspend 하고", InnerService용 새 트랜잭션 생성(REQUIRES_NEW의 핵심 시그널) ######################### Suspending current transaction
2025-08-24T00:56:41.816+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Suspending current transaction, creating new transaction with name [com.example.demo.application.InnerService.requiresNew_addAndThrowRuntime]
# 내부 전용 EM 오픈(바깥과 다른 EM/커넥션)
2025-08-24T00:56:41.816+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(1956559929<open>)] for JPA transaction
# 내부 Tx JDBC 핸들 노출
2025-08-24T00:56:41.817+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@7dc6ce7d]
# 내부 트랜잭션 경계 진입 감지
2025-08-24T00:56:41.817+09:00 TRACE 28710 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [com.example.demo.application.InnerService.requiresNew_addAndThrowRuntime]
# 내부 Tx에 바인딩된 EM 확인
2025-08-24T00:56:41.818+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(1956559929<open>)] for JPA transaction
# 내부 Tx 진행
2025-08-24T00:56:41.818+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Participating in existing transaction

# 내부 로직에서 재조회(findById)
2025-08-24T00:56:41.818+09:00 TRACE 28710 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]
# SELECT 실행(id=1) — 내부 Tx 컨텍스트
2025-08-24T00:56:41.818+09:00 DEBUG 28710 --- [    Test worker] org.hibernate.SQL                        :
select
w1_0.id,
w1_0.balance,
w1_0.owner
from
wallet w1_0
where
w1_0.id=?
# 바인딩
2025-08-24T00:56:41.819+09:00 TRACE 28710 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
# 내부 조회 경계 종료
2025-08-24T00:56:41.819+09:00 TRACE 28710 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]

# 내부 Tx에서 DB 업데이트(+100) — 플러시
2025-08-24T00:56:41.821+09:00 DEBUG 28710 --- [    Test worker] org.hibernate.SQL                        :
update
wallet
set
balance=?,
owner=?
where
id=?
# 바인딩: 100, 'u', id=1
2025-08-24T00:56:41.822+09:00 TRACE 28710 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [100]
2025-08-24T00:56:41.822+09:00 TRACE 28710 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (2:VARCHAR) <- [u]
2025-08-24T00:56:41.822+09:00 TRACE 28710 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (3:BIGINT) <- [1]

# 내부에서 RuntimeException 발생(명시적 throw)
2025-08-24T00:56:41.823+09:00 TRACE 28710 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [com.example.demo.application.InnerService.requiresNew_addAndThrowRuntime] after exception: java.lang.RuntimeException: inner requires_new boom
# 내부 독립 Tx 롤백 시작
2025-08-24T00:56:41.823+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction rollback
# 내부 독립 Tx 롤백 완료(+100 되돌림) ######################### SessionImpl(1956559929<open>
2025-08-24T00:56:41.823+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Rolling back JPA transaction on EntityManager [SessionImpl(1956559929<open>)]
# 내부 Tx 리소스 정리
2025-08-24T00:56:41.825+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(1956559929<open>)] after transaction

# 바깥 Tx 재개(REQUIRES_NEW의 resume 단계) ######################### Resuming suspended transaction
2025-08-24T00:56:41.825+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Resuming suspended transaction after completion of inner transaction

# 바깥 Tx 로직 계속 진행 — DB 업데이트(+10) 후 커밋 예정
2025-08-24T00:56:41.825+09:00 DEBUG 28710 --- [    Test worker] org.hibernate.SQL                        :
update
wallet
set
balance=?,
owner=?
where
id=?
# 바인딩: 10, 'u', id=1
2025-08-24T00:56:41.825+09:00 TRACE 28710 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [10]
2025-08-24T00:56:41.825+09:00 TRACE 28710 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (2:VARCHAR) <- [u]
2025-08-24T00:56:41.826+09:00 TRACE 28710 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (3:BIGINT) <- [1]

# 바깥 서비스 경계 종료(예외는 내부에서 catch했으므로 정상 흐름)
2025-08-24T00:56:41.826+09:00 TRACE 28710 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [com.example.demo.application.OuterService.outer_calls_requiresNew_and_catch]
# 바깥 Tx 커밋 시작
2025-08-24T00:56:41.826+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
# 바깥 Tx 커밋 완료(+10 반영)
2025-08-24T00:56:41.826+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(932629072<open>)]
# 바깥 Tx 리소스 정리
2025-08-24T00:56:41.827+09:00 DEBUG 28710 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(932629072<open>)] after transaction

# 테스트 단계 로그 (사용자 측 “Act” 표식 종료)
2025-08-24T00:56:41.827+09:00  INFO 28710 --- [    Test worker] com.example.demo.application.TxLabTest   : >>> Act
