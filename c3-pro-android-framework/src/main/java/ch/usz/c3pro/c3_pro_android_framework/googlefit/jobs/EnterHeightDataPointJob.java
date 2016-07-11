package ch.usz.c3pro.c3_pro_android_framework.googlefit.jobs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.Priority;

/**
 * C3PRO
 * <p/>
 * Created by manny Weber on 07/05/2016.
 * Copyright © 2016 University Hospital Zurich. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This job can be used to enter a height measurement into Google Fit. The permissions to write
 * such data has to have been requested.
 * */
public class EnterHeightDataPointJob extends Job {

    private GoogleApiClient apiClient;
    private DataSet dataSet;

    public EnterHeightDataPointJob(GoogleApiClient googleApiClient, Context context, Date startTime, Date endTime, float height){
        super(new Params(Priority.HIGH).singleInstanceBy(Long.toString(startTime.getTime())+Long.toString(endTime.getTime())));
        apiClient = googleApiClient;
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(context)
                .setDataType(DataType.TYPE_HEIGHT)
                .setType(DataSource.TYPE_RAW)
                .build();

        dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint().setTimeInterval(startTime.getTime(), endTime.getTime(), TimeUnit.MILLISECONDS);
        dataPoint = dataPoint.setFloatValues(new Float(height));
        dataSet.add(dataPoint);
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        Status insertStatus =
                Fitness.HistoryApi.insertData(apiClient, dataSet)
                        .await(1, TimeUnit.MINUTES);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }
}
