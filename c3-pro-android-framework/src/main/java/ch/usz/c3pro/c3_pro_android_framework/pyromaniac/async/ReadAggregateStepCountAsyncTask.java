package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import org.hl7.fhir.dstu3.model.Quantity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * C3PRO
 *
 * Created by manny Weber on 08/02/2016.
 * Copyright © 2016 University Hospital Zurich. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * In order to use this AsyncTask, the google API needs to be set up and permissions have to be granted.
 * The Task will return a FHIR {@link Quantity} containing the step count for the specified
 * time interval. Returns an empty Quantity (steps = 0) if no data is available in Google Fit.
 * */
/**
 * This Task can be used to read the aggregate step count between two dates from Google Fit in the background.
 * */
public class ReadAggregateStepCountAsyncTask extends LoadResultAsyncTask<Quantity> {
    private GoogleApiClient apiClient;
    private Date start;
    private Date end;

    public ReadAggregateStepCountAsyncTask(GoogleApiClient googleApiClient, final String requestID, Date startTime, Date endTime, Callback.QuantityReceiver quantityCallback){
        super(requestID, quantityCallback);
        apiClient = googleApiClient;
        start = startTime;
        end = endTime;
    }


    @Override
    protected Quantity doInBackground(Void... params) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        long startTime = cal.getTimeInMillis();
        cal.setTime(end);
        long endTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(20, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(apiClient, readRequest).await(1, TimeUnit.MINUTES);

        int stepCount = 0;
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dataPoint : dataSet.getDataPoints()) {
                        int newSteps = dataPoint.getValue(Field.FIELD_STEPS).asInt();
                        stepCount += newSteps;
                    }
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                for (DataPoint dataPoint : dataSet.getDataPoints()) {
                    stepCount += (int) dataPoint.getValue(Field.FIELD_STEPS).asFloat();
                }
            }
        }

        Quantity quantity = new Quantity();
        quantity.setValue(stepCount);
        quantity.setUnit("steps");

        return quantity;
    }
}
