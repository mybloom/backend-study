아래대로 테스트해보면 “어떤 예외가 던져지면 언제 롤백되고, 전파(Propagation)에 따라 바깥/안쪽 트랜잭션이 어떻게 되는지”가 한 방에 들어온다.

1) 최소 실험 세팅
* 도메인(아주 단순): Wallet(id, owner, balance)
* Repository: WalletRepository extends JpaRepository<Wallet, Long>
* 서비스 2개
    * OuterService : 바깥 트랜잭션 시작점
    * InnerService : 전파 속성/예외를 바꿔붙여 호출 대상
* 로그 설정(매우 중요, 무조건 켜기) logging:
*   level:
*     org.springframework.transaction.interceptor: TRACE
*     org.hibernate.SQL: DEBUG
*     org.hibernate.type.descriptor.sql.BasicBinder: TRACE
* 콘솔에 트랜잭션 시작/커밋/롤백과 SQL/바인딩이 보여서 이해가 확 됩니다.
* JPA 주의점: JPA는 쓰기를 “지연”하므로, 중간에 DB 예외를 보고 싶으면 entityManager.flush() 또는 repository.flush()를 넣어 즉시 DB에 쏘고 확인하세요.

2) “종류별” 체크리스트 (예외 종류 × 처리 방식)
   멘토가 말한 “종류별”은 보통 아래 축을 말한다:
1. 예외 타입
* RuntimeException (언체크) → 기본 규칙: 롤백
* CheckedException (체크드) → 기본 규칙: 롤백 안 함
* 커스터마이징: @Transactional(rollbackFor = Exception.class) / noRollbackFor = ...
1. 예외 처리 방식
* 던지고 전파(rethrow) → 트랜잭션 종료 시점에 롤백/커밋 결정
* catch 후 삼킴 → 아무 일 없었던 것처럼 커밋될 수 있음
* catch 후 수동 롤백 표시: TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
  위 두 축만 바꿔도 행동이 확 달라져요.

3) “프로퍼게이션도 똑같습니다!”의 뜻
   같은 실험을 전파 속성을 바꿔가며 반복하라는 뜻이에요.
   핵심 3형제만 집중 실험:
* REQUIRED(기본): 바깥이 있으면 합승, 없으면 새로 시작
* REQUIRES_NEW: 항상 새 트랜잭션(바깥 일시 정지, 내부는 독립 커밋/롤백)
* NESTED: 저장점(savepoint) 기반 하위 트랜잭션 (DB/TxManager가 지원해야 함, MySQL+JpaTxManager 일반적으로 OK)
  보조:
* SUPPORTS(있으면 합승/없으면 비트랜잭션),
* NOT_SUPPORTED(항상 비트랜잭션; 바깥이 있어도 정지),
* MANDATORY(바깥 없으면 예외),
* NEVER(바깥 있으면 예외)

4) 실험 시나리오(테이블)
   아래 조합을 모두 코드로 만들어 결과를 눈으로 확인하면 감이 확 옵니다.
# 스프링 트랜잭션 전파 & 예외 처리 조합 정리

아래 조합을 모두 코드로 만들어 결과를 눈으로 확인하면 감이 확 옵니다.

| #  | 바깥 Tx   | 안쪽 Tx 전파   | 안쪽에서 발생         | 안쪽 처리                  | 기대 결과                               |
|----|-----------|----------------|-----------------------|----------------------------|-----------------------------------------|
| 1  | REQUIRED  | REQUIRED       | RuntimeException      | 그대로 던짐                | 바깥 전체 롤백                          |
| 2  | REQUIRED  | REQUIRED       | RuntimeException      | catch 후 삼킴              | 바깥 커밋 (**주의!**)                   |
| 3  | REQUIRED  | REQUIRED       | CheckedException      | 그대로 던짐                | 기본 규칙상 커밋 (체크드는 롤백 X)      |
| 4  | REQUIRED  | REQUIRED       | CheckedException      | 그대로 던짐 + rollbackFor=Exception | 롤백                                   |
| 5  | REQUIRED  | REQUIRES_NEW   | RuntimeException      | 그대로 던짐                | 안쪽만 롤백, 바깥은 기본 커밋            |
| 6  | REQUIRED  | REQUIRES_NEW   | RuntimeException      | catch 후 삼킴              | 안쪽 롤백, 바깥 커밋                     |
| 7  | REQUIRED  | NESTED         | RuntimeException      | 그대로 던짐                | 기본은 바깥까지 롤백 (저장점도 롤백)     |
| 8  | REQUIRED  | NESTED         | RuntimeException      | catch 후 setRollbackOnly   | 바깥은 커밋, 안쪽 저장점만 롤백          |
| 9  | (없음)    | REQUIRED       | RuntimeException      | -                          | 단일 트랜잭션 롤백                      |
| 10 | REQUIRED  | NOT_SUPPORTED  | RuntimeException      | -                          | 안쪽은 비트랜잭션 → 바깥은 예외 전파 시 롤백 |

| 포인트
* 예외를 어디서 던지고/잡는지가 진짜 중요합니다.
* REQUIRES_NEW는 독립 커밋/롤백이라 바깥 실패에도 안쪽 커밋 결과가 남을 수 있음(보상 트랜잭션/아웃박스에서 자주 쓰는 이유).
* NESTED는 저장점이라 부분 롤백을 만들 수 있음(잡고 setRollbackOnly()로 저장점만 롤백).

7) JPA와 섞일 때 꼭 보는 포인트
* 플러시 시점: 예외가 “커밋 시점”에야 터지면 테스트가 헷갈립니다. **중간 단계에서 flush()**로 빨리 실패를 앞당겨 보세요.
* **예외 타입 변환: 스프링이 JDBC/Hibernate 예외를 DataAccessException 계열로 번역합니다. 잡을 때는 상위 타입으로 잡는 습관.**
* self-invocation 함정: 같은 빈 내부에서 this.inner()로 호출하면 AOP 프록시를 안 타서 @Transactional이 적용 안 됩니다. → 메서드를 다른 빈으로 분리하거나, 구조를 바꾸세요.
* readOnly=true: 하이버네이트는 플러시를 늦추지만 “절대 쓰기 금지”가 되는 건 아닙니다. 실수 방지는 테스트/코드 규칙으로.
* 격리수준/락: 필요하면 isolation/락킹 테스트(낙관/비관)도 별 랩으로 추가.

7) 이렇게 하면 “몸에 배는” 학습 루틴
1. 각 케이스 메서드를 진짜로 만들어서 호출
**2. 로그로 트랜잭션 시작/커밋/롤백 확인**
3. DB 상태(balance)로 최종 결과 검증
4. 예외 타입/전파/flush 유무를 한 번에 바꿔보며 차이를 표로 기록
   멘토 말의 요지 = “예외/전파 ‘종류별’로 실험을 쪼개서, 실제로 던져보고(또는 삼켜보고) 결과를 눈으로 검증하라.” 이걸 반복하면, “왜 여기서 롤백되지?” “왜 이건 커밋됐지?” 같은 감각이 아주 빠르게 자리 잡습니다.
