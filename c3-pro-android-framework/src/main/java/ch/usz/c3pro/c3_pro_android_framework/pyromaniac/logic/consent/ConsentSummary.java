package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent;

import java.io.Serializable;

/**
 * C3-PRO
 * <p/>
 * Created by manny Weber on 08/09/2016.
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
 * The ConsentSummary is saved as an extra by the ViewConsentTaskActivity after conducting the
 * eligibility assessment and consent task. It provides the information whether the user has
 * consented, has set a passcode and to what kind of sharing their data they have ageed.
 * */
public class ConsentSummary implements Serializable {

    private boolean consented;
    private int passcode;
    private boolean sharing = false;

    public ConsentSummary (boolean hasConsented, int passcode, boolean sharing){
        consented = hasConsented;
        this.passcode = passcode;
        this.sharing = sharing;
    }

    public boolean hasConsented() {
        return consented;
    }

    /**
     * Returns true if the participant consents to sharing data,
     * false if only the study team should access the data
     * false if question was not asked
     * */
    public boolean getSharing() {
        return sharing;
    }

    public int getPasscode() {
        return passcode;
    }
}
