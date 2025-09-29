package model;

import java.io.Serializable;

/**
 * Represents a customer review showcased on the homepage.
 */
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String reviewerName;
    private final int rating;
    private final String comment;
    private final String productName;

    public Review(String reviewerName, int rating, String comment, String productName) {
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.comment = comment;
        this.productName = productName;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getProductName() {
        return productName;
    }
}
