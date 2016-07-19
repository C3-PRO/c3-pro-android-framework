package ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs;

import com.birbit.android.jobqueue.Params;
import com.google.common.base.Strings;

import org.hl7.fhir.dstu3.model.Questionnaire;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import ca.uhn.fhir.parser.IParser;
import ch.usz.c3pro.c3_pro_android_framework.C3PRO;
import ch.usz.c3pro.c3_pro_android_framework.C3PROErrorCode;

/**
 * C3PRO
 *
 * Created by manny Weber on 06/22/2016.
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
public class ReadQuestionnaireFromURLJob extends LoadResultJob<Questionnaire> {
    private String loadURL;


    public ReadQuestionnaireFromURLJob(final String requestID, String URL, LoadResultJob.LoadResultCallback callback) {
        super(new Params(Priority.HIGH).requireNetwork().singleInstanceBy(requestID), requestID, callback);
        loadURL = URL;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        returnResult(readQuestionnaire(loadURL));
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
            if (!Strings.isNullOrEmpty(qString)) {
                IParser parser = C3PRO.getFhirContext().newJsonParser();
                Questionnaire q = parser.parseResource(Questionnaire.class, qString);
                return q;
            }

        } catch (MalformedURLException e) {
            resultCallback.onFail(request, C3PROErrorCode.CAUGHT_IO_EXCEPTION);
        } catch (IOException e) {
            resultCallback.onFail(request, C3PROErrorCode.CAUGHT_IO_EXCEPTION);
        }
        return new Questionnaire();
    }
}
