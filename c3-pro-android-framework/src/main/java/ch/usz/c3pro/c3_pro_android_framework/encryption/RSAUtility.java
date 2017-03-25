package ch.usz.c3pro.c3_pro_android_framework.encryption;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.common.base.Strings;

import org.researchstack.backbone.ResourcePathManager;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import ch.usz.c3pro.c3_pro_android_framework.errors.Logging;

/**
 * C3-PRO
 *
 * Created by manny Weber on 08/16/2016.
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
 * This utility provides the tools to read a certificate file and use the public key to encrypt data.
 * For testing purposes, a
 */
public class RSAUtility {

    // certificate that contains the publicKey to encrypt resources before sending
    private Certificate certificate;
    private PublicKey publicKey;

    //for decrypting received messages
    private PrivateKey privateKey;

    public RSAUtility(Certificate certificate) throws CertificateException {
        this.certificate = certificate;
        publicKey = certificate.getPublicKey();
    }

    /**
     * RSA Utility set up for encryption and decryption
     */
    public RSAUtility(Context context, String certificatePath, String pemPath) throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

        // for testing, generate test keyPair
        if (Strings.isNullOrEmpty(certificatePath) || Strings.isNullOrEmpty(pemPath)) {
            generateKeyPair();
        }else {
            // not tested yet with real certificate
            certificate = readCertificate(context, certificatePath);
            Log.d(Logging.logTag, "certificate read from path");
            publicKey = certificate.getPublicKey();
            Log.d(Logging.logTag, "public Key :");
            Log.d(Logging.logTag, new String(publicKey.getEncoded()));

            privateKey = readPrivateKeyFromPem(context, pemPath);
            Log.d(Logging.logTag, "private Key: " + privateKey.getEncoded());
        }
    }

    /**
     * RSA Utility set up for encryption only
     */
    public RSAUtility(Context context, String certificatePath) throws CertificateException, NoSuchAlgorithmException {

        // not tested yet with real certificate
        certificate = readCertificate(context, certificatePath);
        Log.d(Logging.logTag, "certificate read from path");
        publicKey = certificate.getPublicKey();
        Log.d(Logging.logTag, "public Key :");
        Log.d(Logging.logTag, new String(publicKey.getEncoded()));
    }

    public Certificate readCertificate(Context context, String certificatePath) throws CertificateException {
        InputStream certificateStream = ResourcePathManager.getResouceAsInputStream(context, certificatePath);
        return CertificateFactory.getInstance("X.509").generateCertificate(certificateStream);
    }

    public PrivateKey readPrivateKeyFromPem(Context context, String pemPath) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pemFile = ResourcePathManager.getResourceAsString(context, pemPath);
        Log.d(Logging.logTag, "pemString: " + pemFile);
        String pemNoBreak = pemFile.replace("\r\n", "");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pemNoBreak.getBytes());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    public byte[] encryptData(byte[] toEncrypt) throws CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (publicKey == null) {
            //error
        }
        return encryptDataWithKey(toEncrypt, publicKey);
    }

    public byte[] encryptDataWithKey(byte[] toEncrypt, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(toEncrypt);
        return encryptedBytes;
    }

    public byte[] getSecretKeyWrapped(SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.WRAP_MODE, publicKey);
        return cipher.wrap(secretKey);
    }

    public SecretKey unwrapKey(byte[] wrappedKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        if (privateKey == null) {
            Log.e(Logging.logTag, "RSAUtility is not set up with a PrivateKey.");
            return null;
        } else {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.UNWRAP_MODE, privateKey);
            return (SecretKey) cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);
        }
    }

    private void generateKeyPair() throws CertificateException, NoSuchAlgorithmException {
        Log.d(Logging.logTag, "generating key pair");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair kp = keyGen.generateKeyPair();
        publicKey = kp.getPublic();
        privateKey = kp.getPrivate();
    }
}
