package ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.hl7.fhir.dstu3.model.Questionnaire;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import ca.uhn.fhir.parser.IParser;
import ch.usz.c3pro.c3_pro_android_framework.C3PRO;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.DataQueue;
import ch.usz.c3pro.c3_pro_android_framework.utils.StringUtil;

/**
 * C3PRO
 * <p/>
 * Created by manny Weber on 06/22/2016.
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
public class ReadQuestionnaireFromURLJob extends Job {
    private static int HANDLER_MESSAGE_QUESTIONNAIRE = 2;
    private String loadURL;
    private DataQueue.QuestionnaireReceiver receiver;
    private Handler dataHandler;

    public ReadQuestionnaireFromURLJob(final String requestID, String URL, DataQueue.QuestionnaireReceiver questionnaireReceiver) {
        super(new Params(Priority.HIGH).requireNetwork().singleInstanceBy(requestID));
        loadURL = URL;

        receiver = questionnaireReceiver;
        dataHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == HANDLER_MESSAGE_QUESTIONNAIRE) {
                    Questionnaire questionnaire = (Questionnaire) msg.obj;
                    receiver.receiveQuestionnaire(requestID, questionnaire);
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

        Message msg = new Message();
        msg.what = HANDLER_MESSAGE_QUESTIONNAIRE;
        msg.obj = readQuestionnaire(loadURL);
        dataHandler.sendMessage(msg);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }

    private Questionnaire readQuestionnaire(String qURL) {
        try {
            URLConnection connection = new URL(qURL).openConnection();
            String qString = "";
            Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8");
            while (scanner.hasNextLine()) {
                qString = qString.concat(scanner.nextLine());
            }
            scanner.close();
            if (StringUtil.isNotNullOrEmpty(qString)) {
                IParser parser = C3PRO.getFhirContext().newJsonParser();
                Questionnaire q = parser.parseResource(Questionnaire.class, qString);
                return q;
            }

        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return new Questionnaire();
    }
}
