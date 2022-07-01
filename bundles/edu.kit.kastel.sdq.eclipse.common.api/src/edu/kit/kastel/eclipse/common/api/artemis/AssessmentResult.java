/* Licensed under EPL-2.0 2022. */
package edu.kit.kastel.eclipse.common.api.artemis;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import edu.kit.kastel.eclipse.common.api.artemis.mapping.Feedback;
import edu.kit.kastel.eclipse.common.api.artemis.mapping.User;

@JsonInclude(Include.NON_NULL)
public class AssessmentResult implements Serializable {
    private static final long serialVersionUID = -1703764424474018461L;

    private int id;
    private String assessmentType;
    private double score;
    private boolean rated;
    private boolean hasFeedback;
    private String completionDate;
    private User assessor;
    private List<Feedback> feedbacks;

    public AssessmentResult(int id, String assessmentType, double score, boolean rated, boolean hasFeedback,
            String completionDate, User assessor, List<Feedback> feedbacks) {
        this.id = id;
        this.assessmentType = assessmentType;
        this.score = score;
        this.rated = rated;
        this.hasFeedback = hasFeedback;
        this.completionDate = completionDate;
        this.assessor = assessor;
        this.feedbacks = feedbacks;
    }

    public String getAssessmentType() {
        return this.assessmentType;
    }

    public User getAssessor() {
        return this.assessor;
    }

    public String getCompletionDate() {
        return this.completionDate;
    }

    public List<Feedback> getFeedbacks() {
        return this.feedbacks;
    }

    public int getId() {
        return this.id;
    }

    public double getScore() {
        return this.score;
    }

    public boolean isHasFeedback() {
        return this.hasFeedback;
    }

    public boolean isRated() {
        return this.rated;
    }
}
