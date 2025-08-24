package com.example.demo.application;

import com.example.demo.domain.Wallet;
import com.example.demo.infrastructure.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.NestedTransactionNotSupportedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class TxLabTest {

    @Autowired
    private OuterService outerService;
    @Autowired
    private WalletRepository walletRepository;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        wallet = walletRepository.save(new Wallet("u", 0));
    }

    @Test
    @DisplayName("REQUIRED→REQUIRED에서 RuntimeException 전파시 전체 롤백")
    void required_required_runtime_rolls_back_all() {
        log.info(">>> Test: required_required_runtime_rolls_back_all");
        // Arrange
        Long id = wallet.getId();
        assertThat(wallet.getBalance()).isEqualTo(0);

        // Act
        log.info(">>> Act");
        assertThrows(RuntimeException.class, () -> outerService.outer_calls_required_then_runtime(id));
        log.info(">>> Act");

        // Assert
        Wallet w = walletRepository.findById(id).orElseThrow();
        assertThat(w.getBalance()).isEqualTo(0);// +10, +100 모두 롤백
    }

    @Test
    @DisplayName("REQUIRED→REQUIRES_NEW 예외를 잡으면 안쪽만 롤백, 바깥 커밋")
    void required_requiresNew_catch_inner_only_rolls_back() {
        log.info(">>> Test: required_requiresNew_catch_inner_only_rolls_back");
        // Arrange
        Long id = wallet.getId();
        assertThat(wallet.getBalance()).isEqualTo(0);

        // Act
        log.info(">>> Act");
        outerService.outer_calls_requiresNew_and_catch(id);
        log.info(">>> Act");

        // Assert
        Wallet w = walletRepository.findById(id).orElseThrow();
        assertThat(w.getBalance()).isEqualTo(10);// 안쪽 +100 롤백, 바깥 +10 커밋
    }

    @Test
    @DisplayName("REQUIRED→NESTED 는 JPA에서는 지원하지 않아 예외 발생")
    void required_nested_savepoint_not_supported() {
        // Arrange
        Long id = wallet.getId();

        // Act
        ThrowableAssert.ThrowingCallable act = () -> outerService.outer_calls_nested_markRollbackOnly(id);

        // Assert
        assertThatThrownBy(act)
                .isInstanceOf(NestedTransactionNotSupportedException.class)
                .hasMessageContaining("does not support savepoints");
    }

}
