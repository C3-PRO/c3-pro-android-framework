package ch.usz.c3pro.c3_pro_android_framework.errors;

/**
 * C3-PRO
 * <p/>
 * Created by manny Weber on 07/15/2016.
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
 * Error codes used for communicating errors during async processing and encryption.
 * */
public enum C3PROErrorCode {
    C_3_PRO_ERROR(0, "An error has occurred."),
    RESULT_CANCELLED(1, "User cancelled activity"),
    JOB_HANDLER_ERROR(2, "Job handler could not receive message object"),
    JOB_CANCELLED(3, "Job manager cancelled Job"),
    CAUGHT_IO_EXCEPTION(4, "Caught IO exception while executing job"),
    ENCRYPTION_EXCEPTION(5, "Caught Encryption Exception, see throwable");

    private final int code;
    private final String description;
    private Throwable throwable = null;

    private C3PROErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    public C3PROErrorCode addThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}