package ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs;

import android.util.Log;

import com.google.gson.JsonObject;

import org.hl7.fhir.instance.model.api.IBaseResource;

import java.security.GeneralSecurityException;

import ch.usz.c3pro.c3_pro_android_framework.dataqueue.EncryptedDataQueue;
import ch.usz.c3pro.c3_pro_android_framework.errors.C3PROErrorCode;
import ch.usz.c3pro.c3_pro_android_framework.errors.Logging;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.Callback;

/**
 * C3-PRO
 * <p/>
 * Created by manny Weber on 08/22/2016.
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
public class CreateResourceJobEncrypted extends CreateResourceJob {

    public CreateResourceJobEncrypted(IBaseResource resource, Callback.UploadCallback uploadCallback) {
        super(resource, EncryptedDataQueue.getInstance().getEncryptionServerURL(), uploadCallback);
    }

    @Override
    public void onRun() throws Throwable {
        try {
            JsonObject jasonToSend = EncryptedDataQueue.getInstance().encryptResource(uploadResource);
            Log.d(Logging.asyncLogTag, jasonToSend.getAsString());

            //TODO: send that thing to serverURL


        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            callback.onFail(uploadResource, C3PROErrorCode.ENCRYPTION_EXCEPTION.addThrowable(e));
        }
    }
}
