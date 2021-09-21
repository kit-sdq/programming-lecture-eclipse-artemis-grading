package edu.kit.kastel.sdq.eclipse.grading.client.mappings.lock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is only used in the json deserialization process. Much like {@link ParticipationDummy}
 */
public class ExerciseDummy {

	private double maxPoints;

	@JsonCreator
	public ExerciseDummy(@JsonProperty("maxPoints") double maxPoints) {
		this.maxPoints = maxPoints;
	}

	public double getMaxPoints() {
		return this.maxPoints;
	}
}
