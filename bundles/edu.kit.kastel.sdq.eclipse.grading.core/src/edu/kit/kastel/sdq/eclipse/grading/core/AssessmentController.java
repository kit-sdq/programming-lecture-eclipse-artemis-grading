package edu.kit.kastel.sdq.eclipse.grading.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.kit.kastel.sdq.eclipse.grading.api.IAnnotation;
import edu.kit.kastel.sdq.eclipse.grading.api.IAssessmentController;
import edu.kit.kastel.sdq.eclipse.grading.api.IMistakeType;
import edu.kit.kastel.sdq.eclipse.grading.api.IRatingGroup;
import edu.kit.kastel.sdq.eclipse.grading.core.annotation.AnnotationDao;
import edu.kit.kastel.sdq.eclipse.grading.core.annotation.JsonFileAnnotationDao;
import edu.kit.kastel.sdq.eclipse.grading.core.artemis.AnnotationDeserializer;
import edu.kit.kastel.sdq.eclipse.grading.core.config.ConfigDao;
import edu.kit.kastel.sdq.eclipse.grading.core.config.ExerciseConfig;
import edu.kit.kastel.sdq.eclipse.grading.core.config.JsonFileConfigDao;


public class AssessmentController implements IAssessmentController {

	private SystemwideController systemWideController;
	private int submissionID;
	private JsonFileConfigDao configDao;
	private AnnotationDao annotationDao;

	private final int courseID;
	private final int exerciseID;

	private String exerciseConfigShortName;
	//TODO global List of ASsessmentController in SystemSpecificController
	//
	//TODO pull config file up to "global state".

	/**
	 * Protected, because the way to get a specific assessment controller should be over a SystemwideController.
	 *
	 * @param configFile path to the config file
	 * @param exerciseName the shortName of the exercise (must be same in the config file).
	 */
	protected AssessmentController(SystemwideController systemWideController, int courseID, int exerciseID, int submissionID,String exerciseConfigName) {
		this.systemWideController = systemWideController;
		this.submissionID = submissionID;
		this.annotationDao = new JsonFileAnnotationDao();
		this.exerciseID = exerciseID;
		this.courseID = courseID;

		this.exerciseConfigShortName = exerciseConfigName;

		try {
			this.initializeWithDeserializedAnnotations();
		} catch (Exception e) {
			System.err.println("Warning: Deserializing Annotations from Artemis failed (most likely none were present)!");
		}
	}

	@Override
	public void addAnnotation(int annotationID, IMistakeType mistakeType, int startLine, int endLine, String fullyClassifiedClassName,
			String customMessage, Double customPenalty) throws Exception {
		this.annotationDao.addAnnotation(annotationID, mistakeType, startLine, endLine, fullyClassifiedClassName, customMessage, customPenalty);

	}

	@Override
	public double calculateCurrentPenaltyForMistakeType(IMistakeType mistakeType) throws IOException {
		return new DefaultPenaltyCalculationStrategy(this.getAnnotations(), this.getMistakes())
				.calculatePenaltyForMistakeType(mistakeType);
	}

	@Override
	public double calculateCurrentPenaltyForRatingGroup(IRatingGroup ratingGroup) throws IOException {
		return new DefaultPenaltyCalculationStrategy(this.getAnnotations(), this.getMistakes())
				.calcultatePenaltyForRatingGroup(ratingGroup);
	}

	@Override
	public Collection<IAnnotation> getAnnotations() {
		return this.annotationDao.getAnnotations().stream()
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public Collection<IAnnotation> getAnnotations(String className) {
		return this.annotationDao.getAnnotations().stream()
				.filter(annotation -> annotation.getClassFilePath().equals(className))
				.collect(Collectors.toUnmodifiableList());
	}

	private ConfigDao getConfigDao() {
		return this.systemWideController.getConfigDao();
	}

	@Override
	public int getCourseID() {
		return this.courseID;
	}

	/**
	 *
	 * @return the shortName (identifier) used to retrieve the corresponding exercise config from the ConfigDao.
	 */
	public String getExerciseConfigShortName() {
		return this.exerciseConfigShortName;
	}

	@Override
	public int getExerciseID() {
		return this.exerciseID;
	}

	@Override
	public Collection<IMistakeType> getMistakes() throws IOException {
		final Optional<ExerciseConfig> exerciseConfigOptional = this.getConfigDao().getExerciseConfigs().stream()
				.filter(exerciseConfig -> exerciseConfig.getShortName().equals(this.exerciseConfigShortName))
				.findFirst();
		if (exerciseConfigOptional.isPresent()) {
			return exerciseConfigOptional.get().getIMistakeTypes();
		}
		throw new IOException("Exercise not found in config!");
	}

	@Override
	public Collection<IRatingGroup> getRatingGroups() throws IOException {

		final Optional<ExerciseConfig> exerciseConfigOptional = this.getConfigDao().getExerciseConfigs().stream()
				.filter(exerciseConfig -> exerciseConfig.getShortName().equals(this.exerciseConfigShortName))
				.findFirst();
		if (exerciseConfigOptional.isPresent()) {
			return exerciseConfigOptional.get().getIRatingGroups();
		}
		throw new IOException("Exercise not found in config!");
	}

	public int getSubmissionID() {
		return this.submissionID;
	}

	@Override
	public String getTooltipForMistakeType(IMistakeType mistakeType) {
		// TODO Auto-generated method stub
		return "TEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEST TODO penalty type specific tooltip ";
	}

	private void initializeWithDeserializedAnnotations() throws Exception {
		AnnotationDeserializer annotationDeserializer = new AnnotationDeserializer(this.getMistakes());

		for (IAnnotation annotation : annotationDeserializer.deserialize(this.systemWideController.getArtemisGUIController().getAllFeedbacksGottenFromLocking(this.submissionID))) {
			this.addAnnotation(
					annotation.getId(),
					annotation.getMistakeType(),
					annotation.getStartLine(),
					annotation.getEndLine(),
					annotation.getClassFilePath(),
					annotation.getCustomMessage().orElse(null),
					annotation.getCustomPenalty().orElse(null)
			);
		}
	}

	/**
	 * TODO null statt optional
	 */
	@Override
	public void modifyAnnotation(int annatationId, String customMessage, Double customPenalty) {
		this.annotationDao.modifyAnnotation(annatationId, customMessage, customPenalty);
	}

	@Override
	public void removeAnnotation(int annotationId) {
		this.annotationDao.removeAnnotation(annotationId);
	}
}
