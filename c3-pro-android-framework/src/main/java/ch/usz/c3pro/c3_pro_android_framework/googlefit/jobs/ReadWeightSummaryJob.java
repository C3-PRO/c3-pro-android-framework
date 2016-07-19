package ch.usz.c3pro.c3_pro_android_framework.googlefit.jobs;

import com.birbit.android.jobqueue.Params;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Quantity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.LoadResultJob;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.Priority;
import ch.usz.c3pro.c3_pro_android_framework.googlefit.GoogleFitAgent;

/**
 * C3PRO
 *
 * Created by manny Weber on 07/05/2016.
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
 * This job can be used to read a weight summary from Google Fit in the background. It will call back
 * with a FHIR Observation with maximum, average, and minimum weight within the specified time range.
 */
public class ReadWeightSummaryJob extends LoadResultJob<Observation> {
    private GoogleApiClient apiClient;
    private Date start;
    private Date end;

    public ReadWeightSummaryJob(GoogleApiClient googleApiClient, final String requestID, Date startTime, Date endTime, GoogleFitAgent.ObservationReceiver observationReceiver) {
        super(new Params(Priority.HIGH).singleInstanceBy(requestID), requestID, observationReceiver);
        apiClient = googleApiClient;
        start = startTime;
        end = endTime;
    }


    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_WEIGHT, DataType.AGGREGATE_WEIGHT_SUMMARY)
                .bucketByTime(100, TimeUnit.DAYS)
                .setTimeRange(start.getTime(), end.getTime(), TimeUnit.MILLISECONDS)
                .build();

        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(apiClient, readRequest).await(1, TimeUnit.MINUTES);

        float min = 0;
        float avg = 0;
        float max = 0;
        int sampleCount = 0;
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dataPoint : dataSet.getDataPoints()) {

                        sampleCount++;

                        float newMin = dataPoint.getValue(Field.FIELD_MIN).asFloat();
                        min += newMin;

                        float newAvg = dataPoint.getValue(Field.FIELD_AVERAGE).asFloat();
                        avg += newAvg;

                        float newMax = dataPoint.getValue(Field.FIELD_MAX).asFloat();
                        max += newMax;
                    }
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                for (DataPoint dataPoint : dataSet.getDataPoints()) {
                    sampleCount++;

                    float newMin = dataPoint.getValue(Field.FIELD_MIN).asFloat();
                    min += newMin;

                    float newAvg = dataPoint.getValue(Field.FIELD_AVERAGE).asFloat();
                    avg += newAvg;

                    float newMax = dataPoint.getValue(Field.FIELD_MAX).asFloat();
                    max += newMax;
                }
            }
        }

        Observation observation = new Observation();
        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
        String startString = format.format(start);
        String endString = format.format(end);
        String id = "Weight Summary between " + startString + " and " + endString;
        observation.setId(id);
        observation.setStatus(Observation.ObservationStatus.FINAL);


        if (sampleCount > 0) {
            Quantity minQuantity = new Quantity();
            minQuantity.setValue((double) (min / sampleCount));
            minQuantity.setUnit("kg");
            minQuantity.setSystem("http://loinc.org");
            minQuantity.setCode("3141-9");
            Observation.ObservationComponentComponent minComp = new Observation.ObservationComponentComponent();
            minComp.setId("minimum Weight");
            minComp.setValue(minQuantity);
            observation.addComponent(minComp);

            Quantity avgQuantity = new Quantity();
            avgQuantity.setValue((double) (avg / sampleCount));
            avgQuantity.setUnit("kg");
            avgQuantity.setSystem("http://loinc.org");
            avgQuantity.setCode("3141-9");
            Observation.ObservationComponentComponent avgComp = new Observation.ObservationComponentComponent();
            avgComp.setId("average Weight");
            avgComp.setValue(avgQuantity);
            observation.addComponent(avgComp);

            Quantity maxQuantity = new Quantity();
            maxQuantity.setValue((double) (max / sampleCount));
            maxQuantity.setUnit("kg");
            maxQuantity.setSystem("http://loinc.org");
            maxQuantity.setCode("3141-9");
            Observation.ObservationComponentComponent maxComp = new Observation.ObservationComponentComponent();
            maxComp.setId("maximum Weight");
            maxComp.setValue(maxQuantity);
            observation.addComponent(maxComp);
        }
        returnResult(observation);
    }
}