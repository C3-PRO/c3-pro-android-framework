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

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * C3-PRO
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
 * The Task will return a FHIR {@link Quantity} containing the last entry of the user's weight.
 * Returns an empty Quantity (weight = 0) if no data is available in Google Fit.
 * */
public class ReadWeightAsyncTask extends LoadResultAsyncTask<Quantity> {
    private GoogleApiClient apiClient;

    public ReadWeightAsyncTask(GoogleApiClient googleApiClient, final String requestID, Callback.QuantityReceiver quantityReceiver) {
        super(requestID, quantityReceiver);
        apiClient = googleApiClient;
    }

    @Override
    protected Quantity doInBackground(Void... params) {
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .setTimeRange(1, new Date().getTime(), TimeUnit.MILLISECONDS)
                .setLimit(1)
                .build();

        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(apiClient, readRequest).await(1, TimeUnit.MINUTES);

        if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                for (DataPoint dataPoint : dataSet.getDataPoints()) {
                    Quantity quantity = new Quantity();
                    quantity.setValue((double) dataPoint.getValue(Field.FIELD_WEIGHT).asFloat());
                    quantity.setUnit("kg");
                    quantity.setSystem("http://loinc.org");
                    quantity.setCode("3141-9");

                    return quantity;
                }
            }
        } else if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dataPoint : dataSet.getDataPoints()) {
                        Quantity quantity = new Quantity();
                        quantity.setValue((double) dataPoint.getValue(Field.FIELD_WEIGHT).asFloat());
                        quantity.setUnit("kg");
                        quantity.setSystem("http://loinc.org");
                        quantity.setCode("3141-9");

                        return quantity;
                    }
                }
            }
        } else {
            Quantity quantity = new Quantity();
            quantity.setValue(0d);
            quantity.setUnit("kg");
            quantity.setSystem("http://loinc.org");
            quantity.setCode("3141-9");

            return quantity;
        }
        return null;
    }
}
