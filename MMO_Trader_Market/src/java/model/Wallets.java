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
    private double balance;
    private int status;
    private Instant created_at;
    private Instant updated_at;

    public Wallets() {
    }

    public Wallets(int id, double balance, int status, Instant created_at, Instant updated_at) {
        this.id = id;
        this.balance = balance;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
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

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }

    public Instant getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Instant updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "Wallets{" + "id=" + id + ", balance=" + balance + ", status=" + status + ", created_at=" + created_at + ", updated_at=" + updated_at + '}';
    }
    
    
}
