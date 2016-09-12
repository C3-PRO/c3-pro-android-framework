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

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Quantity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * C3-PRO
 * <p/>
 * Created by manny Weber on 08/02/2016.
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
 * In order to use this AsyncTask, the google API needs to be set up and permissions have to be granted.
 * The Task will return a FHIR {@link Observation} containing min, max, and average weight during the specified
 * time interval. Returns an empty Observation (weight = 0) if no data is available in Google Fit.
 * */
public class ReadWeightSummaryAsyncTask extends LoadResultAsyncTask<Observation> {
    private GoogleApiClient apiClient;
    private Date start;
    private Date end;

    public ReadWeightSummaryAsyncTask(GoogleApiClient googleApiClient, final String requestID, Date startTime, Date endTime, Callback.ObservationReceiver observationReceiver) {
        super(requestID, observationReceiver);
        apiClient = googleApiClient;
        start = startTime;
        end = endTime;
    }

    @Override
    protected Observation doInBackground(Void... params) {
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
        return observation;
    }
}
