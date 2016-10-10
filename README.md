C3PRO Android Framework
-------
C3PRO uses the [HAPI][hapi] FHIR library and [ResearchStack] in an attempt to bring the [C3-PRO] functionality to Android.

Combining [🔥 FHIR][fhir] and [ResearchStack], usually for data storage into [i2b2][], this framework allows you to use 
FHIR `Questionnaire` resources directly with a ResearchStack `ViewTaskActivity` and will return FHIR `QuestionnaireResponse` that 
you can send to your server.

### Usage
The library is hosted at [bintray].

To set up a project to use the C3PRO framework, the library is available on jCenter and can simply be added as a dependency:
```groovy
dependencies {
    compile ('ch.usz.c3pro:c3-pro-android-framework:0.1.4'){
        exclude module: 'javax.servlet-api'
        exclude module: 'hapi-fhir-base'
    }

}
```
Some packages of third party dependencies need to be excluded to avoid duplicate class names.

#####The setup

A sample application to demonstrate the setup is available [here][c3-pro-demo]

A subclass of `Application` is needed and set as main application in the AndroidManifest.
Most setup methods are best put in the onCreate() method of the C3PROApplication class to make sure they survive Activities' lifecycles.
The C3PRO class is initialized with the application's context and a FHIR Server URL.
There are also some ResearchStack settings. More details about that can be found on the [ResearchStack website][researchstack].

The `Application`file should look something like this:
```java
public class C3PROApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * Initialize C3PRO:
         * C3PRO will provide you with a FhirContext. This Object is expensive and you should
         * only have one instance in your app. Therefore, C3PRO will keep it as a singleton.
         * Access it by calling C3PRO.getFhirContext();
         * <p />
         * If you provide a context (your application) and an URL, C3PRO
         * will create a DataQueue for you to create and read Resources from your server in a
         * background thread.
         * */
        C3PRO.init(this, "http://fhirtest.uhn.ca/baseDstu3");

        // ResearchStack: Customize your pin code preferences
        PinCodeConfig pinCodeConfig = new PinCodeConfig(); // default pin config (4-digit, 1 min lockout)

        // ResearchStack: Customize encryption preferences
        EncryptionProvider encryptionProvider = new UnencryptedProvider(); // No pin, no encryption

        // ResearchStack: If you have special file handling needs, implement FileAccess
        FileAccess fileAccess = new SimpleFileAccess();

        // ResearchStack: If you have your own custom database, implement AppDatabase
        AppDatabase database = new DatabaseHelper(this,
                DatabaseHelper.DEFAULT_NAME,
                null,
                DatabaseHelper.DEFAULT_VERSION);

        StorageAccess.getInstance().init(pinCodeConfig, encryptionProvider, fileAccess, database);
    }
}
```
The `AndroidManifest`:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="ch.usz.c3pro.demo.android">

    <application
        android:name=".C3PROApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">

        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```
The `build.gradle`:
```groovy
apply plugin: 'com.android.application'

android {
    compileSdkVersion 23
    buildToolsVersion "23.0.3"

    defaultConfig {
        applicationId "ch.usz.c3pro.demo"
        minSdkVersion 16
        targetSdkVersion 23
        versionCode 1
        versionName "1.0"
        // TODO: enabling multidex support.
        multiDexEnabled true
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
    dexOptions {
        javaMaxHeapSize "3g"
    }
}

dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    testCompile 'junit:junit:4.12'
    // TODO: include C3PRO framework, but exclude some of the hapi library
    compile ('ch.usz.c3pro:c3-pro-android-framework:0.1.1'){
        exclude module: 'javax.servlet-api'
        exclude module: 'hapi-fhir-base'
    }
}
```
##### The C3PRO
Once set up (preferably in your Application class, so it survives the Activities' lifesycles), the C3PRO will provide you with a HAPI `FhirContext` and a `DataQueue` if you have provided a FHIR Server URL.

You can access it anywhere in your app code, for example to get a `JsonParser`:
```java
C3PRO.getFhirContext().newJsonParser();
```

Or to upload a resource:
```java
C3PRO.getDataQueue.create(resource);
```
##### The QuestionnaireFragment
Use the `QuestionnaireFragment` to represent a Questionnaire and conduct a Survey based on it.
```java
private void launchSurvey(Questionnaire questionnaire) {
        /**
         * Looking up if a fragment for the given questionnaire has been created earlier. if so,
         * the survey is started, assuming that the TaskViewActivity has been created before!!
         * The questionnaire IDs are used for identification, assuming they are unique.
         * */
        QuestionnaireFragment fragment = (QuestionnaireFragment) getSupportFragmentManager().findFragmentByTag(questionnaire.getId());
        if (fragment != null) {
            /**
             * If the fragment has been added before, the TaskViewActivity can be started directly,
             * assuming that it was prepared right after the fragment was created.
             * */
            fragment.startTaskViewActivity();
        } else {
            /**
             * If the fragment does not exist, create it, add it to the fragment manager and
             * let it prepare the TaskViewActivity
             * */
            final QuestionnaireFragment questionnaireFragment = new QuestionnaireFragment();
            questionnaireFragment.newInstance(questionnaire, new QuestionnaireFragment.QuestionnaireFragmentListener() {
                @Override
                public void whenTaskReady(String requestID) {
                    /**
                     * Only when the task is ready, the survey is started
                     * */
                    questionnaireFragment.startTaskViewActivity();
                }

                @Override
                public void whenCompleted(String requestID, QuestionnaireResponse questionnaireResponse) {
                    /**
                     * Where the response for a completed survey is received. Here it is printed
                     * to a TextView defined in the app layout.
                     * */
                    printQuestionnaireAnswers(questionnaireResponse);
                }

                @Override
                public void whenCancelledOrFailed() {
                    /**
                     * If the task can not be prepared, a backup plan is needed.
                     * Here the fragment is removed from the FragmentManager so it can be created
                     * again later
                     * TODO: proper error handling not yet implemented
                     * */
                    getSupportFragmentManager().beginTransaction().remove(questionnaireFragment).commit();
                }
            });

            /**
             * In order for the fragment to get the context and be found later on, it has to be added
             * to the fragment manager.
             * */
            getSupportFragmentManager().beginTransaction().add(questionnaireFragment, questionnaire.getId()).commit();
            /**
             * prepare the TaskViewActivity. As defined above, it will start the survey once the
             * TaskViewActivity is ready.
             * */
            questionnaireFragment.prepareTaskViewActivity();
        }
    }
```

### Versions

The library uses HAPI FHIR 1.5 for dstu3. Questionnaires in dstu2 (with group and question elements) will not work with this demo setup. 
Target Android sdk is 23, minimum sdk 16 due to ResearchStack.

### Issues

Implementation is ongoing, not everything is complete and nothing has been systematically tested.
- EnableWhen conditions have only been tested with boolean and singlechoice answertypes
- No proper error handling implemented as of yet.

Modules
-------
The framework will consist of several modules that complement each other, similar to the C3-PRO ios framework.

### Questionnaires

Enables the conversion of a FHIR `Questionnaire` resource to a ResearchSTack `task` that can be presented to the user using a 
`ViewTaskActivity` and conversion back from a `TaskResult` to a FHIR `QuestionnaireResponse` resource.

### DataQueue

This module provides a FHIR server implementation used to move FHIR resources, created on device, to a FHIR 
server, without the need for user interaction nor -confirmation.

### GoogleFit

Supports easy interaction with the Google Fit API. Step count, height and latest weight of the user can be read and returned as FHIR `Quantity` and `Observation`.
It can also write new height and weight data into the Fit history.

Licence
-------
This work will be [Apache 2][apache] licensed. A NOTICE.txt file will follow at some point, and don't forget to also add the licensing information of the submodules somewhere in your product:
- [ResearchStack][researchstack]
- [HAPI FHIR][hapi]
- [Android Priority Job Queue][jobqueue]

[hapi]: http://hapifhir.io
[researchstack]: http://researchstack.org
[C3-PRO]: http://c3-pro.org
[fhir]: http://hl7.org/fhir/
[researchkit]: http://researchkit.github.io
[i2b2]: https://www.i2b2.org
[apache]: http://www.apache.org/licenses/LICENSE-2.0
[jobqueue]: https://github.com/yigit/android-priority-jobqueue
[C3-PRO-android]:https://github.com/manuelaCalavera/c3-pro-android-framework
[c3-pro-demo]:https://github.com/manuelaCalavera/c3-pro-demo-android
[bintray]:https://bintray.com/manuelacalavera/maven/c3-pro-android-framework/view
