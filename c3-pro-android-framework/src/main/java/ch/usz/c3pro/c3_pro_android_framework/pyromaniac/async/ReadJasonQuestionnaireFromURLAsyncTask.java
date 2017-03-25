package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async;

import com.google.common.base.Strings;

import org.hl7.fhir.dstu3.model.Questionnaire;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import ca.uhn.fhir.parser.IParser;
import ch.usz.c3pro.c3_pro_android_framework.errors.C3PROErrorCode;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.Pyro;

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
 * This Async Task will read a FHIR {@link Questionnaire} directly from a json file at the specified
 * URL. The json file has to contain a valid dstu3 FHIR Questionnaire.
 * */
public class ReadJasonQuestionnaireFromURLAsyncTask extends LoadResultAsyncTask<Questionnaire> {
    private String loadURL;

    public ReadJasonQuestionnaireFromURLAsyncTask(String requestID, String URL, Callback.QuestionnaireReceiver receiver){
        super(requestID, receiver);
        loadURL = URL;
    }

    @Override
    protected Questionnaire doInBackground(Void... params) {
        try {
            URLConnection connection = new URL(loadURL).openConnection();
            String qString = "";
            Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8");
            while (scanner.hasNextLine()) {
                qString = qString.concat(scanner.nextLine());
            }
            scanner.close();
            if (!Strings.isNullOrEmpty(qString)) {
                IParser parser = Pyro.getFhirContext().newJsonParser();
                Questionnaire q = parser.parseResource(Questionnaire.class, qString);
                return q;
            }

        } catch (MalformedURLException e) {
            resultCallback.onFail(requestID, C3PROErrorCode.CAUGHT_IO_EXCEPTION);
        } catch (IOException e) {
            resultCallback.onFail(requestID, C3PROErrorCode.CAUGHT_IO_EXCEPTION);
        }
        return new Questionnaire();
    }
}
