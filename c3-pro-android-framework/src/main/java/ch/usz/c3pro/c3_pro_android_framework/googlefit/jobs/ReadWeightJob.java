package ch.usz.c3pro.c3_pro_android_framework.googlefit.jobs;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
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

import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.Priority;
import ch.usz.c3pro.c3_pro_android_framework.googlefit.GoogleFitAgent;

/**
 * C3PRO
 * <p/>
 * Created by manny Weber on 07/04/2016.
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
 * This job can be used to read the latest entry of the user's weight. The permissions to read
 * such data has to have been requested.
 * If no entry can be found, a quantity of zero is returned.
 * */
public class ReadWeightJob extends Job {
    public static String LTAG = "LC3P";
    private static int HANDLER_MESSAGE_WEIGHT = 4;

    private GoogleApiClient apiClient;
    private GoogleFitAgent.QuantityReceiver receiver;
    private Handler dataHandler;

    public ReadWeightJob(GoogleApiClient googleApiClient, final String requestID, GoogleFitAgent.QuantityReceiver quantityReceiver) {
        super(new Params(Priority.HIGH).singleInstanceBy(requestID));
        apiClient = googleApiClient;
        receiver = quantityReceiver;


        dataHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == HANDLER_MESSAGE_WEIGHT) {
                    Quantity quantity = (Quantity) msg.obj;
                    receiver.receiveQuantity(requestID, quantity);
                } else {
                    //TODO error handling
                }
            }
        };
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .setTimeRange(1, new Date().getTime(), TimeUnit.MILLISECONDS)
                .setLimit(1)
                .build();

        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(apiClient, readRequest).await(1, TimeUnit.MINUTES);

        Message msg = new Message();
        msg.what = HANDLER_MESSAGE_WEIGHT;

        if (dataReadResult.getDataSets().size() > 0) {
            Log.d(LTAG, "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                for (DataPoint dataPoint : dataSet.getDataPoints()) {
                    Quantity quantity = new Quantity();
                    quantity.setValue((double) dataPoint.getValue(Field.FIELD_WEIGHT).asFloat());
                    quantity.setUnit("kg");
                    quantity.setSystem("http://loinc.org");
                    quantity.setCode("3141-9");
                    msg.obj = quantity;
                }
            }
        } else if (dataReadResult.getBuckets().size() > 0) {
            Log.d(LTAG, "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (DataPoint dataPoint : dataSet.getDataPoints()) {
                        Quantity quantity = new Quantity();
                        quantity.setValue((double) dataPoint.getValue(Field.FIELD_WEIGHT).asFloat());
                        quantity.setUnit("kg");
                        quantity.setSystem("http://loinc.org");
                        quantity.setCode("3141-9");
                        msg.obj = quantity;
                    }
                }
            }
        } else{
            Quantity quantity = new Quantity();
            quantity.setValue(0d);
            quantity.setUnit("kg");
            quantity.setSystem("http://loinc.org");
            quantity.setCode("3141-9");
            msg.obj = quantity;
        }

        dataHandler.sendMessage(msg);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }
}
