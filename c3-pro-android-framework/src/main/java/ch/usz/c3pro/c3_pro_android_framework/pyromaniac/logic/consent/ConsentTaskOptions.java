package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent;

/**
 * C3-PRO
 *
 * Created by manny Weber on 07/25/2016.
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
 * ConsentTaskOptions can be used to configure the {@link ch.usz.c3pro.c3_pro_android_framework.consent.ViewConsentTaskActivity}
 *
 * */
public class ConsentTaskOptions {

    private boolean askForSharing = true;
    private String shareMoreInfoDocument = "consent_sharing";
    private String reviewConsentDocument;

    private boolean askToCreatePasscode = true;

    private boolean requiresSignature = true;



    private boolean requiresName = false;
    private boolean requiresBirthday = false;


    // TODO service permissions


    public boolean askForSharing(){
        return askForSharing;
    }
    public String getShareMoreInfoDocument(){
        return shareMoreInfoDocument;
    }
    public String getReviewConsentDocument(){
        return reviewConsentDocument;
    }
    public boolean askToCreatePasscode(){
        return askToCreatePasscode;
    }
    public boolean requiresSignature(){
        return requiresSignature;
    }
    public boolean reqiresName(){return reqiresName();}
    public boolean requiresBirthday(){return  requiresBirthday;}


    public void setAskForSharing(boolean askForSharing){
        this.askForSharing = askForSharing;
    }
    public void setShareMoreInfoDocument(String document){
        shareMoreInfoDocument = document;
    }
    public void setReviewConsentDocument(String document){
        reviewConsentDocument = document;
    }
    public void setAskToCreatePasscode(boolean askToCreatePasscode){
        this.askToCreatePasscode = askToCreatePasscode;
    }
    public void setRequiresName(boolean requiresName) {
        this.requiresName = requiresName;
    }
    public void setRequiresBirthday(boolean requiresBirthday){
        this.requiresBirthday = requiresBirthday;
    }
}
