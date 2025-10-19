package model.view;

import java.time.LocalDate;

public class CustomerProfileView {

    private final String displayName;
    private final String email;
    private final LocalDate joinDate;
    private final long totalOrders;
    private final long completedOrders;
    private final long disputedOrders;
    private final double satisfactionScore;

    public CustomerProfileView(String displayName, String email, LocalDate joinDate,
            long totalOrders, long completedOrders, long disputedOrders, double satisfactionScore) {
        this.displayName = displayName;
        this.email = email;
        this.joinDate = joinDate;
        this.totalOrders = totalOrders;
        this.completedOrders = completedOrders;
        this.disputedOrders = disputedOrders;
        this.satisfactionScore = satisfactionScore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public long getCompletedOrders() {
        return completedOrders;
    }

    public long getDisputedOrders() {
        return disputedOrders;
    }

    public double getSatisfactionScore() {
        return satisfactionScore;
    }
}

