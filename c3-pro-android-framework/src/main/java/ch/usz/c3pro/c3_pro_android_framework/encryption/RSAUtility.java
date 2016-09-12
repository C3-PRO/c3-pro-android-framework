package ch.usz.c3pro.c3_pro_android_framework.encryption;

import android.content.Context;
import android.util.Log;

import org.researchstack.backbone.ResourcePathManager;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import ch.usz.c3pro.c3_pro_android_framework.errors.Logging;

/**
 * C3-PRO
 * <p/>
 * Created by manny Weber on 08/16/2016.
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
 * This utility provides the tools to read a certificate file and use the public key to encrypt data.
 * For testing purposes, a
 * */
public class RSAUtility {

    private Certificate certificate;
    private PublicKey publicKey;

    //for testing
    private PrivateKey privateKey;

    public RSAUtility(Certificate certificate) throws CertificateException {
        this.certificate = certificate;
        publicKey = certificate.getPublicKey();
    }

    public RSAUtility(Context context, String certificatePath) throws CertificateException, NoSuchAlgorithmException {

        /* not tested yet with real certificate
        certificate = readCertificate(context, certificatePath);
        Log.d(LTAG, "certificate read from path");
        publicKey = certificate.getPublicKey();
        Log.d(LTAG, "public Key :");
        Log.d(LTAG, new String(publicKey.getEncoded()));
        */

        generateKeyPair();

    }

    public Certificate readCertificate(Context context, String certificatePath) throws CertificateException {
        InputStream certificateStream = ResourcePathManager.getResouceAsInputStream(context, certificatePath);
        return CertificateFactory.getInstance("X.509").generateCertificate(certificateStream);
    }

    public byte[] encryptData (byte[] toEncrypt) throws CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (publicKey == null){
            //error
        }
        return  encryptDataWithKey(toEncrypt, publicKey);
    }

    public byte[] encryptDataWithKey(byte[] toEncrypt, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(toEncrypt);
        return  encryptedBytes;
    }

    public byte[] getSecretKeyWrapped(SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.WRAP_MODE, publicKey);
        return cipher.wrap(secretKey);
    }

    public SecretKey unwrapKey(byte[] wrappedKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.UNWRAP_MODE, privateKey);
        return (SecretKey) cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);
    }

    private void generateKeyPair()throws CertificateException, NoSuchAlgorithmException{
        Log.d(Logging.logTag, "generating key pair");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair kp = keyGen.generateKeyPair();
        publicKey = kp.getPublic();
        privateKey = kp.getPrivate();
    }
}
