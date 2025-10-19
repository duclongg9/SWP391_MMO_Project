/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.time.Instant;

/**
 *
 * @author D E L L
 */
public class Wallets {
    private int id;
    private User userId;
    private double balance;
    private int status;
    private Instant createdAt;
    private Instant updatedAt;

    public Wallets() {
    }

    public Wallets(int id, double balance, int status, Instant created_at, Instant updated_at) {
        this.id = id;
        this.balance = balance;
        this.status = status;
        this.createdAt = created_at;
        this.updatedAt = updated_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }
    
    

    @Override
    public String toString() {
        return "Wallets{" + "id=" + id + ", balance=" + balance + ", status=" + status + ", created_at=" + createdAt + ", updated_at=" + updatedAt + '}';
    }
    
    
}
