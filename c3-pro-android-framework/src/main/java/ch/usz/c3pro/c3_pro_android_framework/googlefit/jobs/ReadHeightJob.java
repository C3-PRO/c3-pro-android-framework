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

import org.hl7.fhir.dstu3.model.Quantity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.LoadResultJob;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.Priority;
import ch.usz.c3pro.c3_pro_android_framework.googlefit.GoogleFitAgent;

/**
 * C3PRO
 *
 * Created by manny Weber on 07/04/2016.
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
 * This job can be used to read the latest entry of the user's height. The permissions to read
 * such data has to have been requested.
 * If no entry can be found, a quantity of zero is returned.
 */
public class ReadHeightJob extends LoadResultJob<Quantity> {
    private GoogleApiClient apiClient;

    public ReadHeightJob(GoogleApiClient googleApiClient, final String requestID, GoogleFitAgent.QuantityReceiver quantityReceiver) {
        super(new Params(Priority.HIGH).singleInstanceBy(requestID), requestID, quantityReceiver);
        apiClient = googleApiClient;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_HEIGHT)
                .setTimeRange(1, new Date().getTime(), TimeUnit.MILLISECONDS)
                .setLimit(1)
                .build();

        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(apiClient, readRequest).await(1, TimeUnit.MINUTES);


        if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                for (DataPoint dataPoint : dataSet.getDataPoints()) {
                    Quantity quantity = new Quantity();
                    quantity.setValue((double) dataPoint.getValue(Field.FIELD_HEIGHT).asFloat());
                    quantity.setUnit("m");
                    quantity.setSystem("http://loinc.org");
                    quantity.setCode("8302-2");
                    returnResult(quantity);
                }
            }
        } else if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dataPoint : dataSet.getDataPoints()) {
                        Quantity quantity = new Quantity();
                        quantity.setValue((double) dataPoint.getValue(Field.FIELD_HEIGHT).asFloat());
                        quantity.setUnit("m");
                        quantity.setSystem("http://loinc.org");
                        quantity.setCode("8302-2");
                        returnResult(quantity);
                    }
                }
            }
        } else {
            Quantity quantity = new Quantity();
            quantity.setValue(0d);
            quantity.setUnit("m");
            quantity.setSystem("http://loinc.org");
            quantity.setCode("8302-2");
            returnResult(quantity);
        }
    }
}