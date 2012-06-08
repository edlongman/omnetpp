/*--------------------------------------------------------------*
  Copyright (C) 2006-2008 OpenSim Ltd.

  This file is distributed WITHOUT ANY WARRANTY. See the file
  'License' for details on this and other legal matters.
*--------------------------------------------------------------*/

package org.omnetpp.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A job that can execute several simulation in serial or concurrent manner
 *
 * @author rhornig
 */
public class BatchedSimulationLauncherJob extends Job implements IJobChangeListener {
    protected ILaunchConfiguration configuration;
    protected ILaunch launch;
    protected int runs[];
    protected List<Job> runJobs = new ArrayList<Job>();
    protected int runIndex = 0;
    protected int maxParalelJobs;
    protected int finishedJobs = 0;
    protected IProgressMonitor groupMonitor;
    // dummy job to fix grouped progress monitor bug
    protected Job finishJob = new Job ("Finishing batch") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            monitor.beginTask("Finishing batch", 1);
            monitor.subTask("Refreshing workspace");
            try {
                // refresh the workspace so all generated files will be displayed
                ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
            } catch (CoreException e) {}
            monitor.worked(1);
            monitor.done();
            return Status.OK_STATUS;
        }
    };

    /**
     * @param configuration The configuration to be started
     * @param launch The launch object
     * @param runs An integer array specifying which runs to execute
     * @param parallelism How many simulations should be able to run concurrently
     */
    public BatchedSimulationLauncherJob(ILaunchConfiguration configuration, ILaunch launch, int[] runs, int parallelism) {
        super("Running "+runs.length+" simulations");
        this.configuration = configuration;
        this.launch = launch;
        this.runs = runs;
        this.maxParalelJobs = parallelism;
        // we don't want to display the batch job to the user
        setSystem(true);
    }

    @Override
    protected IStatus run(IProgressMonitor mon) {

        groupMonitor = Job.getJobManager().createProgressGroup();
        groupMonitor.beginTask("Running simulations ("+runs.length+" runs)", runs.length);
        runIndex = finishedJobs = 0;

        // create a list of jobs that later can be scheduled
        for (int run : runs) {
            SimulationLauncherJob launchJob = new SimulationLauncherJob(configuration, launch, run, true, -1);
            launchJob.addJobChangeListener(this);
            launchJob.setPriority(Job.LONG);
            runJobs.add(launchJob);
        }

        // schedule some of them
        try {
            while (!isFinished() && isSchedulable()) {
                scheduleJobs();
                Job.getJobManager().join(launch, null);
            }
            finishJob.setProgressGroup(groupMonitor, 0);
            finishJob.schedule();
        } catch (OperationCanceledException e) {
            Job.getJobManager().cancel(launch);
            return Status.CANCEL_STATUS;
        } catch (InterruptedException e) {
            return Status.CANCEL_STATUS;
        } finally {
            groupMonitor.done();
        }
        return Status.OK_STATUS;
    }

    /**
     * Schedules as many jobs for execution as possible
     */
    protected synchronized void scheduleJobs() {
        while (isSchedulable()) {
            Job job = runJobs.get(runIndex++);
            job.setProgressGroup(groupMonitor, 1); // assign it to the group monitor so it will be displayed under it
            job.schedule();
        }
    }

    /**
     * Returns whether we can schedule this job (the number scheduled and
     * unfinished jobs are < maxParalellJobs)
     */
    protected synchronized boolean isSchedulable() {
        return runIndex - finishedJobs < maxParalelJobs && runIndex < runJobs.size();
    }

    /**
     * Returns whether all jobs have been finished
     */
    protected synchronized boolean isFinished() {
        return finishedJobs >= runJobs.size();
    }

    // job state change listeners to keep track of the number of running jobs
    public synchronized void done(IJobChangeEvent event) {
        finishedJobs++;
        // if the a job is done, we no longer listen to its state changes
        event.getJob().removeJobChangeListener(this);
        // schedule some more jobs (if any)
        scheduleJobs();
    }

    public void aboutToRun(IJobChangeEvent event) {
    }

    public void running(IJobChangeEvent event) {
    }

    public void awake(IJobChangeEvent event) {
    }

    public void scheduled(IJobChangeEvent event) {
    }

    public void sleeping(IJobChangeEvent event) {
    }
}