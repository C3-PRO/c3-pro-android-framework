package ch.usz.c3pro.c3_pro_android_framework.googlefit;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ch.usz.c3pro.c3_pro_android_framework.errors.Logging;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.Callback;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.ReadAggregateStepCountAsyncTask;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.ReadHeightAsyncTask;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.ReadWeightAsyncTask;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.ReadWeightSummaryAsyncTask;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.WriteToGoogleFitAsyncTask;

/**
 * C3-PRO
 *
 * Created by manny Weber on 06/29/2016.
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
 * This agent will help to request information from and write data to the Google Fit api. Before
 * using its methods, the permissions for the operations have to be requested from the user and data
 * may have to be subscribed to in order to get the information wanted.
 * */
public class GoogleFitAgent {
    private static GoogleApiClient apiClient;

    private GoogleFitAgent() {
    }

    public static void init(GoogleApiClient googleApiClient) {
        apiClient = googleApiClient;
    }

    /**
     * Calls back with the total number of steps taken between two dates.
     * Remember to subscribe to the step count. Add permission to the AndroidManifest.xml
     * and add the scope to the GoogleApiClient Builder .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
     */
    public static void getAggregateStepCountBetween(Date start, Date end, String requestID, Callback.QuantityReceiver quantityReveiver) {
        new ReadAggregateStepCountAsyncTask(apiClient, requestID, start, end, quantityReveiver).execute();
    }

    /**
     * Calls back with the latest entry of the user's height. If no entry is found, a Quantity of zero
     * is returned.
     * Remember to add permission to the AndroidManifest.xml.
     * and add the scope to the GoogleApiClient Builder .addScope(new Scope(Scopes.FITNESS_BODY_READ))
     */
    public static void getLatestSampleOfHeight(String requestID, Callback.QuantityReceiver quantityReceiver) {
        new ReadHeightAsyncTask(apiClient, requestID, quantityReceiver).execute();
    }

    /**
     * Calls back with the latest entry of the user's weight. If no entry is found, a Quantity of zero
     * is returned.
     * Remember to add permission to the AndroidManifest.xml.
     * and add the scope to the GoogleApiClient Builder .addScope(new Scope(Scopes.FITNESS_BODY_READ))
     */
    public static void getLatestSampleOfWeight(String requestID, Callback.QuantityReceiver quantityReceiver) {
        new ReadWeightAsyncTask(apiClient, requestID, quantityReceiver).execute();
    }

    /**
     * Calls back with an observation with a component with each, maximum, average, and minimum weight
     * between the specified dates. If no entries are found, the observation will not contain any
     * components.
     * Remember to add permission to the AndroidManifest.xml.
     * and add the scope to the GoogleApiClient Builder .addScope(new Scope(Scopes.FITNESS_BODY_READ))
     */
    public static void getWeightSummaryBetween(Date start, Date end, String requestID, Callback.ObservationReceiver observationReceiver) {

        new ReadWeightSummaryAsyncTask(apiClient, requestID, start, end, observationReceiver).execute();
    }

    /**
     * For debug: prints the DataSet to the Log Console
     * */
    private static void dumpDataSet(DataSet dataSet) {
        Log.d(Logging.logTag, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.d(Logging.logTag, "Data point:");
            Log.d(Logging.logTag, "\tType: " + dp.getDataType().getName());
            Log.d(Logging.logTag, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.d(Logging.logTag, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.d(Logging.logTag, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

    /**
     * Writes a current weight reading to google fit history. The current date and time is used for
     * the data point as start and end time.
     * Permission has to be declared in the AndroidManifest.
     * and Api and scope added when building the GoogleApiClient: .addApi(Fitness.HISTORY_API)
     * .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
     */
    public static void enterWeightDataPoint(Context context, float weight) {
        long now = new Date().getTime();
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(context)
                .setDataType(DataType.TYPE_WEIGHT)
                .setType(DataSource.TYPE_RAW)
                .build();

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint().setTimeInterval(now, now, TimeUnit.MILLISECONDS);
        dataPoint = dataPoint.setFloatValues(Float.valueOf(weight));
        dataSet.add(dataPoint);

        new WriteToGoogleFitAsyncTask(apiClient, dataSet).execute();
    }

    /**
     * Writes a current height reading to google fit history. The current date and time is used for
     * the data point as start and end time.
     * Permission has to be declared in the AndroidManifest.
     * and Api and scope added when building the GoogleApiClient: .addApi(Fitness.HISTORY_API)
     * .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
     */
    public static void enterHeightDataPoint(Context context, float height) {
        long now = new Date().getTime();
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(context)
                .setDataType(DataType.TYPE_HEIGHT)
                .setType(DataSource.TYPE_RAW)
                .build();

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint().setTimeInterval(now, now, TimeUnit.MILLISECONDS);
        dataPoint = dataPoint.setFloatValues(Float.valueOf(height));
        dataSet.add(dataPoint);

        new WriteToGoogleFitAsyncTask(apiClient, dataSet).execute();
    }
}
