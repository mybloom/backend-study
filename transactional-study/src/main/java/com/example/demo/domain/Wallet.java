package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String owner;
    private long balance;

    public Wallet(String owner, long balance) {
        this.owner = owner;
        this.balance = balance;
    }

    public void plus(long amount) { this.balance += amount; }
    public void minus(long amount) { this.balance -= amount; }
}
