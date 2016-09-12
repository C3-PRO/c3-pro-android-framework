package ch.usz.c3pro.c3_pro_android_framework.dataqueue;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;

import org.hl7.fhir.instance.model.api.IBaseResource;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.CreateResourceJobEncrypted;
import ch.usz.c3pro.c3_pro_android_framework.encryption.AESUtility;
import ch.usz.c3pro.c3_pro_android_framework.encryption.RSAUtility;
import ch.usz.c3pro.c3_pro_android_framework.errors.Logging;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.Pyro;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.Callback;

/**
 * C3-PRO
 * <p>
 * Created by manny Weber on 08/22/2016.
 * Copyright © 2016 University Hospital Zurich. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This DataQueue will manage async jobs to upload and download data from the FHIRServer. It will
 * encrypt resources before sending them and use the public key in the certificate to wrap the
 * secrete key used to encrypt the data.
 * Initialize the Class in the onCreate method of your application and access the
 * Queue as a Singleton.
 */
public class EncryptedDataQueue extends DataQueue {
    public static String C3PROFHIRVersion = "dstu 3";

    private AESUtility aesUtility;
    private RSAUtility rsaUtility;
    private String encryptionServerURL;

    /**
     * The EncryptedDataQueue has to be initialized and can then be accessed as a singleton.
     *
     * @param context             application context
     * @param FHIRServerURL       server handling normal FHIR requests.
     * @param encryptionServerURL Server handling encrypted resources as json objects
     * @param certificatePath     path to RSA certificate containing public key
     */
    public static void init(Context context, String FHIRServerURL, String encryptionServerURL, String certificatePath) throws CertificateException, NoSuchAlgorithmException {
        instance = new EncryptedDataQueue(FHIRServerURL, encryptionServerURL, new JobManager(getDefaultBuilder(context).build()), context, certificatePath);
    }

    /**
     * The EncryptedDataQueue has to be initialized and can then be accessed as a singleton.
     *
     * @param context             application context
     * @param FHIRServerURL       server handling normal FHIR requests.
     * @param encryptionServerURL Server handling encrypted resources as json objects
     * @param certificate         RSA certificate containing public key
     */
    public static void init(Context context, String FHIRServerURL, String encryptionServerURL, Certificate certificate) throws CertificateException {
        instance = new EncryptedDataQueue(FHIRServerURL, encryptionServerURL, new JobManager(getDefaultBuilder(context).build()), certificate);
    }

    /**
     * The DataQueue needs the URL to a normal FHIR server and a URL to the server handling encrypted resources as json objects.
     *
     * @param FHIRServerURL       Server handling normal FHIR requests.
     * @param encryptionServerURL Server handling encrypted resources as json objects
     * @param manager             JobManager for async jobs
     * @param certificatePath     path to RSA certificate containing public key
     */
    private EncryptedDataQueue(String FHIRServerURL, String encryptionServerURL, JobManager manager, Context context, String certificatePath) throws CertificateException, NoSuchAlgorithmException {
        super(FHIRServerURL, manager);
        aesUtility = new AESUtility();
        rsaUtility = new RSAUtility(context, certificatePath);
        this.encryptionServerURL = encryptionServerURL;
    }

    /**
     * The DataQueue needs the URL to a normal FHIR server and a URL to the server handling encrypted resources as json objects.
     *
     * @param FHIRServerURL       Server handling normal FHIR requests.
     * @param encryptionServerURL Server handling encrypted resources as json objects
     * @param manager             JobManager for async jobs
     * @param certificate         RSA certificate containing public key
     */
    private EncryptedDataQueue(String FHIRServerURL, String encryptionServerURL, JobManager manager, Certificate certificate) throws CertificateException {
        super(FHIRServerURL, manager);
        aesUtility = new AESUtility();
        rsaUtility = new RSAUtility(certificate);
        this.encryptionServerURL = encryptionServerURL;
    }

    public static EncryptedDataQueue getInstance() {
        return (EncryptedDataQueue) instance;
    }

    public void sendEncrypted(IBaseResource resource, Callback.UploadCallback uploadCallback) {
        CreateResourceJobEncrypted job = new CreateResourceJobEncrypted(resource, uploadCallback);
        addJob(job);
    }

    /**
     * Encrypts a FHIR resource and wraps it in a C3-PRO json object that can be sent to a C3-PRO server.
     */
    public JsonObject encryptResource(IBaseResource resourceToEncrypt) throws CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException {
        // convert resource to json
        String resourceString = Pyro.getFhirContext().newJsonParser().encodeResourceToString(resourceToEncrypt);
        // encrypt jsson
        byte[] encryptedResource = aesUtility.encryptData(resourceString.getBytes());
        // encrypt secret key
        byte[] encryptedKey = rsaUtility.getSecretKeyWrapped(aesUtility.getSecretKey());
        // create new resource
        JsonObject jsonToSend = new JsonObject();
        jsonToSend.addProperty("key_id", "some_key_id");
        // put encrypted private key
        jsonToSend.addProperty("symmetric_key", Base64.encodeToString(encryptedKey, Base64.DEFAULT));
        // put encrypted resource
        jsonToSend.addProperty("message", Base64.encodeToString(encryptedResource, Base64.DEFAULT));
        // put details
        jsonToSend.addProperty("version", C3PROFHIRVersion);

        // return
        return jsonToSend;
    }

    /**
     * Decrypts a FHIR resource contained in a json object received from a C3-PRO server.
     */
    public IBaseResource decryptResource(JsonObject jsonObject) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        if (jsonObject.has("symmetric_key") && jsonObject.has("message")) {
            String keyString = jsonObject.get("symmetric_key").getAsString();
            String objectString = jsonObject.get("message").getAsString();

            if (!Strings.isNullOrEmpty(keyString) && !Strings.isNullOrEmpty(objectString)) {
                byte[] keyBytes = Base64.decode(keyString, Base64.DEFAULT);
                byte[] objectBytes = Base64.decode(objectString, Base64.DEFAULT);

                SecretKey key = rsaUtility.unwrapKey(keyBytes);
                byte[] resourceBytes = aesUtility.deCryptData(objectBytes, key);
                String resourceString = new String(resourceBytes);
                return Pyro.getFhirContext().newJsonParser().parseResource(resourceString);
            }
        }
        Log.e(Logging.logTag, "The jsonObject could not be decrypted, fields are invalid: " + jsonObject.toString());
        return null;
    }

    /**
     * Returns the URL to the C3-PRO server that can provide and receive encrypted resources.
     */

    public String getEncryptionServerURL() {
        return encryptionServerURL;
    }
}
