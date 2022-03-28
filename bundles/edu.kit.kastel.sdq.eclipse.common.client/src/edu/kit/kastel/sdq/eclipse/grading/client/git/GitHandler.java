/* Licensed under EPL-2.0 2022. */
package edu.kit.kastel.sdq.eclipse.grading.client.git;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FileUtils;

public final class GitHandler {

	private static final String REMOTE_NAME = "origin";

	public static void cloneRepo(final File destination, String repoURL, final String branch, CredentialsProvider credentials) throws GitException {
		Repository repository = null;
		try {
			CloneCommand cloneRepository = Git.cloneRepository();
			cloneRepository.setBranch(branch);
			cloneRepository.setDirectory(destination);
			cloneRepository.setRemote(REMOTE_NAME);
			cloneRepository.setURI(String.valueOf(new URIish(repoURL)));
			cloneRepository.setCredentialsProvider(credentials);
			cloneRepository.setCloneAllBranches(true);
			cloneRepository.setCloneSubmodules(false);
			Git git = cloneRepository.call();
			repository = git.getRepository();
		} catch (final Exception e) {
			try {
				FileUtils.delete(destination, FileUtils.RECURSIVE);
			} catch (IOException ioe) {
				// NOP
			}
			throw new GitException("Git clone failed with exception" + e.getMessage(), e);
		} finally {
			if (repository != null) {
				repository.close();
			}
		}
	}

	public static void commitExercise(String authorName, String email, String commitMsg, File exerciseRepo) throws GitException {
		Git git;
		try {
			git = Git.open(exerciseRepo);
			git.add().addFilepattern(".").call();
			git.commit().setCommitter(authorName, email).setMessage(commitMsg).setSign(false).call();
			git.close();
		} catch (GitAPIException | IOException e) {
			throw new GitException("ERROR, can not commit new changes " + exerciseRepo.getPath(), e);
		}
	}

	public static void pushExercise(File exerciseRepo, CredentialsProvider credentials) throws GitException {
		Git git;
		try {
			git = Git.open(exerciseRepo);
		} catch (IOException e) {
			throw new GitException("ERROR, can not open git repo for exercise " + exerciseRepo.getPath(), e);
		}

		PushCommand pushCommand = git.push();
		pushCommand.setCredentialsProvider(credentials);
		// you can add more settings here if needed
		try {
			pushCommand.call().iterator().next();
		} catch (GitAPIException e) {
			throw new GitException("ERROR, can not push to origin git repo for exercise " + exerciseRepo.getPath(), e);
		} finally {
			git.close();
		}

	}

	public static void pullExercise(String gitUsername, String gitPassword, File exerciseRepo) throws GitException {
		Git git;
		try {
			git = Git.open(exerciseRepo);
		} catch (IOException e) {
			throw new GitException("ERROR, can not open git repo for exercise " + exerciseRepo.getPath(), e);
		}

		PullCommand pullCommand = git.pull();
		pullCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPassword));
		// you can add more settings here if needed
		try {
			PullResult result = pullCommand.call();
			if (!result.isSuccessful()) {
				throw new GitException("ERROR, can not push to origin git repo for exercise " + exerciseRepo.getPath());
			}
		} catch (GitAPIException e) {
			throw new GitException("ERROR, can not push to origin git repo for exercise " + exerciseRepo.getPath(), e);
		} finally {
			git.close();
		}

	}

	public static Set<String> cleanRepo(File exerciseRepo) throws GitException {
		Git git;
		try {
			git = Git.open(exerciseRepo);
		} catch (IOException e) {
			throw new GitException("ERROR, can not open git repo for exercise " + exerciseRepo.getPath(), e);
		}
		try {
			git.add().addFilepattern(".");
			Status status = git.status().call();
			Set<String> untrackedChanges = status.getUncommittedChanges();
			git.reset().setMode(ResetType.HARD).call();
			return untrackedChanges;
		} catch (NoWorkTreeException | GitAPIException e) {
			throw new GitException("ERROR, can not clean repository", e);
		} finally {
			git.close();
		}
	}

	private GitHandler() {
		throw new IllegalAccessError();
	}

}
