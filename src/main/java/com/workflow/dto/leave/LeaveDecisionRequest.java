package com.workflow.dto.leave;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LeaveDecisionRequest {

    @NotNull(message = "Reviewer ID is required")
    private Long reviewerId;

    @Size(max = 500, message = "Review comment cannot exceed 500 characters")
    private String reviewComment;

    public LeaveDecisionRequest() {}

    public LeaveDecisionRequest(Long reviewerId, String reviewComment) {
        this.reviewerId = reviewerId;
        this.reviewComment = reviewComment;
    }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
}
