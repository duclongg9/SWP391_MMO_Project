package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a demo customer profile that appears on the homepage.
 */
public class CustomerProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String displayName;
    private final String membershipLevel;
    private final LocalDate joinDate;
    private final int successfulOrders;
    private final double satisfactionScore;

    public CustomerProfile(String displayName, String membershipLevel, LocalDate joinDate,
                           int successfulOrders, double satisfactionScore) {
        this.displayName = displayName;
        this.membershipLevel = membershipLevel;
        this.joinDate = joinDate;
        this.successfulOrders = successfulOrders;
        this.satisfactionScore = satisfactionScore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMembershipLevel() {
        return membershipLevel;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public int getSuccessfulOrders() {
        return successfulOrders;
    }

    public double getSatisfactionScore() {
        return satisfactionScore;
    }
}
