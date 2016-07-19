package ch.usz.c3pro.c3_pro_android_framework;

/**
 * C3PRO
 *
 * Created by manny Weber on 07/15/2016.
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
public enum C3PROErrorCode {
    C_3_PRO_ERROR(0, "An error has occurred."),
    QUESTIONNAIRE_FRAGMENT_CONTEXT_NULL(1, "Context in QuestionnaireFragment is null"),
    QUESTIONNAIRE_FRAGMENT_TASK_NULL(2, "Task in QuestionnaireFragment has not been prepared"),
    RESULT_CANCELLED(3, "User cancelled activity"),
    JOB_HANDLER_ERROR(4, "Job handler could not receive message object"),
    JOB_CANCELLED(5, "Job manager cancelled Job"),
    CAUGHT_IO_EXCEPTION(5, "Caught IO exception while executing job");

    private final int code;
    private final String description;

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

    @Override
    public String toString() {
        return code + ": " + description;
    }
}