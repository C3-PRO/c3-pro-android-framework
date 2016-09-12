package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async;

import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;

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
 * This AsyncTask can be used to write a DataSet to the GoogleFit History in the background.
 * */
public class WriteToGoogleFitAsyncTask extends AsyncTask<Void, Void, Void> {
    private GoogleApiClient apiClient;
    private DataSet dataSetToAdd;

    public WriteToGoogleFitAsyncTask(GoogleApiClient googleApiClient, DataSet dataSet) {
        apiClient = googleApiClient;
        dataSetToAdd = dataSet;
    }

    @Override
    protected Void doInBackground(Void... params) {
        com.google.android.gms.common.api.Status insertStatus =
                Fitness.HistoryApi.insertData(apiClient, dataSetToAdd)
                        .await(1, TimeUnit.MINUTES);
        return null;
    }
}
