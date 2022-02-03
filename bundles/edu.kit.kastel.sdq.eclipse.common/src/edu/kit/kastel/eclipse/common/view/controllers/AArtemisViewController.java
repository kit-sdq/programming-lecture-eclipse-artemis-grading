package edu.kit.kastel.eclipse.common.view.controllers;

import java.util.List;

import edu.kit.kastel.eclipse.common.view.observers.ViewAlertObserver;
import edu.kit.kastel.sdq.eclipse.grading.api.ArtemisClientException;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.ICourse;
import edu.kit.kastel.sdq.eclipse.grading.api.controller.IAlertObserver;
import edu.kit.kastel.sdq.eclipse.grading.api.controller.IArtemisController;
import edu.kit.kastel.sdq.eclipse.grading.api.controller.ISystemwideController;

/**
 * This abstract class is the base for controllers for a view for artemis. It holds all general controllers for the backend calls.
 */
public abstract class AArtemisViewController {
	private IArtemisController artemisGUIController;
	private ISystemwideController systemwideController;
	private IAlertObserver alertObserver;

	public AArtemisViewController() {
	}

	protected void initializeControllersAndObserver() {
		this.alertObserver = new ViewAlertObserver();
		this.artemisGUIController = this.systemwideController.getArtemisGUIController();
		this.systemwideController.addAlertObserver(this.alertObserver);
		this.artemisGUIController.addAlertObserver(this.alertObserver);
	}
	
	/**
	 * @return all courses available at artemis
	 */
	public List<ICourse> getCourses() {
		return this.artemisGUIController.getCourses();
	}

	/**
	 * @return the name of all courses
	 */
	public List<String> getCourseShortNames() {
		return this.artemisGUIController.getCourseShortNames();
	}
	
	/**
	 * @param courseTitle (of the selected course in the combo)
	 * @return all exams of the given course
	 */
	public List<String> getExamShortNames(String courseTitle) {
		return this.artemisGUIController.getExamTitles(courseTitle);
	}
	
	/**
	 * @param courseName (selected course in the combo)
	 * @return all exercises from the given course
	 */
	public List<String> getExerciseShortNames(String courseName) {
		try {
			return this.systemwideController.setCourseIdAndGetExerciseShortNames(courseName);
		} catch (ArtemisClientException e) {
			this.alertObserver.error(e.getMessage(), e);
			return List.of();
		}
	}

	/**
	 * @param examShortName (of the selected exam in the combo)
	 * @return all exercises of the given exam
	 */
	public List<String> getExercisesShortNamesForExam(String examShortName) {
		return this.artemisGUIController.getExerciseShortNamesFromExam(examShortName);
	}
	
	/**
	 * Sets the exercise ID of the selected exercise
	 *
	 * @param exerciseShortName (of the selected exercise in the combo)
	 */
	public void setExerciseID(String exerciseShortName) {
		try {
			this.systemwideController.setExerciseId(exerciseShortName);
		} catch (ArtemisClientException e) {
			this.alertObserver.error(e.getMessage(), e);
		}
	}
	
	protected IArtemisController getArtemisGUIController() {
		return this.artemisGUIController;
	}
	
	protected ISystemwideController getSystemwideController() {
		return this.systemwideController;
	}

	protected IAlertObserver getAlertObserver() {
		return alertObserver;
	}

	protected void setSystemwideController(ISystemwideController systemwideController) {
		this.systemwideController = systemwideController;
	}
	

}