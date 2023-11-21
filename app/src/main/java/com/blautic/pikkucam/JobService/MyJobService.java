package com.blautic.pikkucam.JobService;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.widget.Toast;

import com.blautic.pikkucam.PikkuCamApplication;
import com.blautic.pikkucam.service.FullService;

public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {


        FullService.enqueueWork(getApplicationContext(),new Intent(MyJobService.this, FullService.class));
        // perform work here, i.e. network calls asynchronously

        // returning false means the work has been done, return true if the job is being run asynchronously
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        // if the job is prematurely cancelled, do cleanup work here

        // return true to restart the job
        return false;
    }

}
