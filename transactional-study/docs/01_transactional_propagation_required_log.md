# 테스트 케이스 시작(사용자 로그)
2025-08-24T00:01:18.623+09:00  INFO 28285 --- [    Test worker] com.example.demo.application.TxLabTest   : >>> Test: required_required_runtime_rolls_back_all

# 리포지토리 findAll 호출을 위한 읽기 트랜잭션 시작
2025-08-24T00:01:18.623+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findAll]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT,readOnly
# 트랜잭션용 EntityManager 오픈
2025-08-24T00:01:18.623+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(1750744084<open>)] for JPA transaction
# JDBC 커넥션 핸들 노출
2025-08-24T00:01:18.625+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@368617cd]
# "@Transactional 인터셉터"가 트랜잭션 경계 진입 감지
2025-08-24T00:01:18.625+09:00 TRACE 28285 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findAll]
# 실제 SQL 실행(지갑 전체 조회): 테스트코드에서 findAll()
2025-08-24T00:01:18.626+09:00 DEBUG 28285 --- [    Test worker] org.hibernate.SQL                        : 
    select
        w1_0.id,
        w1_0.balance,
        w1_0.owner 
    from
        wallet w1_0
# 인터셉터 기준 트랜잭션 종료 타이밍
2025-08-24T00:01:18.628+09:00 TRACE 28285 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findAll]
# 커밋 시작(읽기라 변경 없음)
2025-08-24T00:01:18.628+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
# 커밋 완료
2025-08-24T00:01:18.628+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(1750744084<open>)]
# EntityManager 정리
2025-08-24T00:01:18.631+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(1750744084<open>)] after transaction


# sut 부분 시작 
########################################################################################################################################################################
# 바깥 트랜잭션 시작(@Transactional REQUIRED)
2025-08-24T00:01:18.647+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [com.example.demo.application.OuterService.outer_calls_required_then_runtime]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
# 바깥 트랜잭션용 EM 오픈
2025-08-24T00:01:18.647+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(125089688<open>)] for JPA transaction
# JDBC 바인딩
2025-08-24T00:01:18.647+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@b079fa2]
# AOP 인터셉터가 바깥 트랜잭션 진입 감지 : ################## Getting transaction for OuterService
2025-08-24T00:01:18.648+09:00 TRACE 28285 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [com.example.demo.application.OuterService.outer_calls_required_then_runtime]
# 같은 스레드에 바인딩된 EM 재사용 : ###### Found thread-bound EntityManager!
2025-08-24T00:01:18.648+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(125089688<open>)] for JPA transaction
# 기존 트랜잭션에 참여(합승)
2025-08-24T00:01:18.648+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Participating in existing transaction

# 바깥 서비스 내부의 조회(findById) ################## Getting transaction for SimpleJpaRepository.findById
2025-08-24T00:01:18.648+09:00 TRACE 28285 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]
# 실제 SQL 실행(지갑 id=1 조회)
2025-08-24T00:01:18.650+09:00 DEBUG 28285 --- [    Test worker] org.hibernate.SQL                        : 
    select
        w1_0.id,
        w1_0.balance,
        w1_0.owner 
    from
        wallet w1_0 
    where
        w1_0.id=?
# 파라미터 바인딩
2025-08-24T00:01:18.650+09:00 TRACE 28285 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
# 조회 종료(동일 트랜잭션 유지) ################## Completing transaction for SimpleJpaRepository.findById
2025-08-24T00:01:18.652+09:00 TRACE 28285 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]


########################################################################################################################################################################
# 내부 서비스 진입도 같은 트랜잭션에 참여(REQUIRED→REQUIRED) ###### "Found" thread-bound EntityManager!
2025-08-24T00:01:18.652+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(125089688<open>)] for JPA transaction
# 기존 트랜잭션 참여 유지 ###### Participating in existing transaction
2025-08-24T00:01:18.652+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Participating in existing transaction

# 내부 서비스 메서드 진입(@Transactional REQUIRED) ################## Getting transaction for InnerService
2025-08-24T00:01:18.652+09:00 TRACE 28285 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [com.example.demo.application.InnerService.required_addAndThrowRuntime]
# 같은 EM 재사용
2025-08-24T00:01:18.652+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(125089688<open>)] for JPA transaction
# 기존 트랜잭션 참여 ###### Participating in existing transaction
2025-08-24T00:01:18.652+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Participating in existing transaction

# 내부 로직 중 재조회(findById) ################## Getting transaction for SimpleJpaRepository.findById
2025-08-24T00:01:18.652+09:00 TRACE 28285 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]
# 재조회 종료 ################## Completing transaction for SimpleJpaRepository.findById
2025-08-24T00:01:18.653+09:00 TRACE 28285 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]

# 내부 로직에서 DB 업데이트(플러시 발생) — balance=110 시도
2025-08-24T00:01:18.655+09:00 DEBUG 28285 --- [    Test worker] org.hibernate.SQL                        : 
    update
        wallet 
    set
        balance=?,
        owner=? 
    where
        id=?
# 바인딩: balance, owner, id
2025-08-24T00:01:18.655+09:00 TRACE 28285 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [110]
2025-08-24T00:01:18.655+09:00 TRACE 28285 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (2:VARCHAR) <- [u]
2025-08-24T00:01:18.655+09:00 TRACE 28285 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (3:BIGINT) <- [1]

# (1) 내부 메서드에서 RuntimeException 발생(명시적 throw) ################## Completing transaction for InnerService after exception
2025-08-24T00:01:18.656+09:00 TRACE 28285 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [com.example.demo.application.InnerService.required_addAndThrowRuntime] after exception: java.lang.RuntimeException: inner required boom
# (2) 현재(바깥) 트랜잭션을 rollback-only로 마킹 ###### Marking existing transaction as rollback-only
2025-08-24T00:01:18.656+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Participating transaction failed - marking existing transaction as rollback-only
# EntityManager에도 롤백 전용 표시
2025-08-24T00:01:18.657+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Setting JPA transaction on EntityManager [SessionImpl(125089688<open>)] rollback-only
# (3) 내부 예외가 바깥으로 전파되어 바깥 트랜잭션 종료(예외 포함) ################## Completing transaction for OuterService after exception
2025-08-24T00:01:18.657+09:00 TRACE 28285 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [com.example.demo.application.OuterService.outer_calls_required_then_runtime] after exception: java.lang.RuntimeException: inner required boom

# 실제 롤백 시작 ###### Initiating transaction rollback
2025-08-24T00:01:18.657+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction rollback
# 전체 트랜잭션 롤백 수행(앞선 update는 되돌려짐)
2025-08-24T00:01:18.657+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Rolling back JPA transaction on EntityManager [SessionImpl(125089688<open>)]
# EM 정리
2025-08-24T00:01:18.659+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(125089688<open>)] after transaction
# sut 부분 끝
########################################################################################################################################################################



########################################################################################################################################################################
# 검증용 조회 트랜잭션 시작(롤백 확인) : 테스트코드의 Assert 부분     
2025-08-24T00:01:18.659+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT,readOnly
# EM 오픈
2025-08-24T00:01:18.660+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(532687492<open>)] for JPA transaction
# JDBC 노출
2025-08-24T00:01:18.661+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@4c04188]
# 트랜잭션 진입 감지
2025-08-24T00:01:18.661+09:00 TRACE 28285 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]
# SQL 실행(롤백 후 상태 조회)
2025-08-24T00:01:18.661+09:00 DEBUG 28285 --- [    Test worker] org.hibernate.SQL                        : 
    select
        w1_0.id,
        w1_0.balance,
        w1_0.owner 
    from
        wallet w1_0 
    where
        w1_0.id=?
# 파라미터 바인딩
2025-08-24T00:01:18.662+09:00 TRACE 28285 --- [    Test worker] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
# 트랜잭션 종료
2025-08-24T00:01:18.663+09:00 TRACE 28285 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]
# 커밋 시작(읽기)
2025-08-24T00:01:18.663+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
# 커밋 완료
2025-08-24T00:01:18.663+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(532687492<open>)]
# EM 정리
2025-08-24T00:01:18.664+09:00 DEBUG 28285 --- [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(532687492<open>)] after transaction

# (JVM 경고: Mockito 에이전트 관련 공지성 메시지, 기능에 영향 없음)
Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended

# 테스트 종료 훅: EMF 종료
2025-08-24T00:01:18.673+09:00  INFO 28285 --- [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
# test 프로필의 ddl-auto=create-drop로 테이블 정리
2025-08-24T00:01:18.673+09:00 DEBUG 28285 --- [ionShutdownHook] org.hibernate.SQL                        : 
    drop table if exists wallet
# 커넥션 풀 종료 시작/완료
2025-08-24T00:01:18.679+09:00  INFO 28285 --- [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2025-08-24T00:01:18.894+09:00  INFO 28285 --- [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
