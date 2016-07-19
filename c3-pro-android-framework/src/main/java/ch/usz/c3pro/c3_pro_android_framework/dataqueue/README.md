DataQueue
---------
The `DataQueue` uses the [HAPI FHIR][hapi] library to talk to FHIR servers and the [Android Priority Job Queue][jobqueue] to provide an easy persistent way to queue FHIR resources for upload.
Requests will be run asynchronously and upload tasks are persistent, even through activity lifecycle.

### Module Interface

IN
- Any FHIR `Resource` to be sent to a FHIR server
- Search URLs to retrieve `Resource`s from a FHIR server
- HAPI queries to be run on a client
OUT
- A Queue that will upload queued `Resources` once a network connection becomes available
- FHIR `Resource`s retrieved from the FHIR server
- asynchronous execution of otherwise UI blocking tasks

##### Setup

If the C3PRO class is set up (preferably in the onCreate() method of the `Application` subclass), a FHIR server URL can be passed and
it will provide a DataQueue in return.
```java
C3PRO.init(this, "http://fhirtest.uhn.ca/baseDstu3");
```

##### Creating and Reading Resources

The DataQueue can then be accessed anywhere through the C3PRO class, for example to upload a `Resource` to the previously provided server:
```java
C3PRO.getDataQueue().create(questionnaireResponse)
```
Or to read resources
```java
C3PRO.getDataQueue().read(String requestID, String searchURL, BundleReceiver resourceReceiver)
```


[hapi]: http://hapifhir.io
[jobqueue]: https://github.com/yigit/android-priority-jobqueue