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

##### DataQueue

The `DataQueue` is best initialized in the onCreate() method of the C3PROApplication class to make sure it survives Activities' lifecycles.


```java
public class C3PROApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: initialize C3-PRO
        /**
         * Initialize DataQueue:
         * You have to provide a context (your application) and an URL to the FHIR Server.
         * Once initialized, DataQueue can write and read Resources from your server in a
         * background thread.
         * */
        DataQueue.init(this, "http://fhirtest.uhn.ca/baseDstu3");
    }
}
```
Once the DataQueue is set up, it can be accessed anywhere through a singleton. It can then be used to send Resources to the specified FHIR server. Uploads will wait for connectivity in the background and upload as soon as the devices is connected to the internet.

```java
DataQueue.getInstance().create(resource);
```

[hapi]: http://hapifhir.io
[jobqueue]: https://github.com/yigit/android-priority-jobqueue
