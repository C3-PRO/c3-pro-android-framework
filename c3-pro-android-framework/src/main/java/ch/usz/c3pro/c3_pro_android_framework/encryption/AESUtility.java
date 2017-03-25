package ch.usz.c3pro.c3_pro_android_framework.encryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * C3-PRO
 *
 * Created by manny Weber on 08/22/2016.
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
 * This utility provides the tools to generate a AES SecretKey and use it to encrypt and decrypt data.
 * */
public class AESUtility {

    // as per C3-PRO specs, a zero vector is used as IV
    byte[] iv = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    // 32 byte = 256 bits
    private int keySize = 256;
    private SecretKey secretKey;

    public AESUtility(){
        randomizeKey();
    }

    public byte[] encryptData (byte[] toEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey.getEncoded(), "AES"), new IvParameterSpec(iv));
        return cipher.doFinal(toEncrypt);
    }

    public byte[] deCryptData (byte[] toDecrypt, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getEncoded(), "AES"), new IvParameterSpec(iv));
        return cipher.doFinal(toDecrypt);
    }

    public byte[] deCryptData (byte[] toDecrypt) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        return deCryptData(toDecrypt, secretKey);
    }

    public void randomizeKey(){
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGenerator.init(keySize);
        secretKey = keyGenerator.generateKey();
    }

    public SecretKey getSecretKey(){
        return secretKey;
    }
}
