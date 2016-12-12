Google Fit
----------
The Google Fit Module, once set up properly, allows for easy access of step count, height, weight and weight summary from the Google Fit history.
Weight and height readings can also easily be written into Google Fit.

### Module Interface

IN
- `GoogleApiClient`
- Weight and height readings
- (data subscriptions and permissions)

OUT
- FHIR `Quantity` for step count, heigt und weight
- FHIR `Observation` for weight summary

##### Setup

To use the `GoogleFitAgent` some setting up is needed. Only a short overview will be given here.
All the information about how and why can be found at https://developers.google.com/fit/android/get-started

### OAuth 2.0 client ID
Only apps with a OAuth 2.0 client ID are allowed to access the Google Fit API.
If you haven't already enabled the Fitness API and obtained an OAuth 2.0 client ID, follow these instructions to do so:
https://developers.google.com/fit/android/get-api-key
The package name for the key has to match the one declared in the AndroidManifest.xml !

### Permissions

In order to access Google Fit data, the user has to be asked for permission. Required permissions have to be declared in the AndroidManifest.
- Accessing the step count requires the FITNESS_ACTIVITY_READ permission
- Accessing weight and height requires the BODY_SENSORS permission.
- To write weight and height readings into Google Fit, the the FITNESS_ACTIVITY_READ_WRITE permission is needed

```xml
<uses-permission android:name="android.permission.FITNESS_ACTIVITY_READ_WRITE" />
<uses-permission android:name="android.permission.BODY_SENSORS" />
```

### APIs and Scopes

When building the GoogleApiClient, the proper APIs and the needed scopes have to be added.
APIs
- Fitness.HISTORY_API to read data previously written to Google Fit, like step count
- Fitness.SENSORS_API to read height and weight data
- Fitness.RECORDING_API to subscribe to and start recording step count data

Scopes
- Scopes.FITNESS_ACTIVITY_READ to access step count
- Scopes.FITNESS_BODY_READ to access height and weight
- Scopes.FITNESS_BODY_READ_WRITE to access and write height and weight data


### GoogleApiClient

To initialize the GoogleFitAgent, a `GoogleApiClient` has to be built, for example in the onCreate() method of your main activity. This is where the APIs and Scopes have to be added.

```java
private void buildFitnessClient() {
        //TODO: Create the Google API Client
        /**
         * In order to access Google Fit data and write data to Google Fit history, the APIs and
         * scopes have to be defined here.
         * Only APIs that are going to be used should be added here. Read only scopes can be used if
         * no data has to be written back to the Google Fit history. Permissions have to be asked for
         * and declared in the AndroidManifest.xml
         * */
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(LTAG, "Connected!!!");
                                /**
                                 * The connection is established, calls to the Fitness APIs can now
                                 * be made. See subscribe() function to see how to subscribe to i.e.
                                 * step count data.
                                 * */
                                //TODO: Subscribe to some data sources!
                                subscribe();

                                /**
                                 * If no fit data is available on the test phone, create height and
                                 * weight entries for testing
                                 * */
                                //GoogleFitAgent.enterHeightDataPoint(getApplicationContext(), 1.68f);
                                //GoogleFitAgent.enterWeightDataPoint(getApplicationContext(), 64f);
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                /**
                                 * Decide what to do if the connection to the Fitness sensors is lost
                                 * at some point. The cause can be read from the ConnectionCallbacks
                                 * */
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(LTAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(LTAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.i(LTAG, "Google Play services connection failed. Cause: " +
                                result.toString());
                        Snackbar.make(
                                MainActivity.this.findViewById(android.R.id.content),
                                "Exception while connecting to Google Play services: " +
                                        result.getErrorMessage(),
                                Snackbar.LENGTH_INDEFINITE).show();
                    }
                })
                .build();
    }
```

### Subscriptions

There might not yet be any other app on the user's phone that records fitness data. In order to start recording, data has to be subscribed to.
```java
public void subscribe() {
        /**
         * To create a subscription, invoke the Recording API. As soon as the subscription is
         * active, fitness data will start recording.
         * The recording API has to have been added while building the FitnessClient!
         */
        Fitness.RecordingApi.subscribe(googleApiClient, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(LTAG, "Step Count: Existing subscription for activity detected.");
                            } else {
                                Log.i(LTAG, "Step Count: Successfully subscribed!");
                            }
                        } else {
                            Log.i(LTAG, "Step Count: There was a problem subscribing.");
                        }
                    }
                });

        Fitness.RecordingApi.subscribe(googleApiClient, DataType.TYPE_HEIGHT)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(LTAG, "Height: Existing subscription for activity detected.");
                            } else {
                                Log.i(LTAG, "Height: Successfully subscribed!");
                            }
                        } else {
                            Log.i(LTAG, "Height: There was a problem subscribing.");
                        }
                    }
                });

        Fitness.RecordingApi.subscribe(googleApiClient, DataType.TYPE_WEIGHT)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(LTAG, "Weight: Existing subscription for activity detected.");
                            } else {
                                Log.i(LTAG, "Weight: Successfully subscribed!");
                            }
                        } else {
                            Log.i(LTAG, "Weight: There was a problem subscribing.");
                        }
                    }
                });
    }
```

##### Google Fit Agent

With the properly built `GoogleApiClient`, the GoogleFitAgent can be initialized
```java
GoogleFitAgent.init(googleApiClient);
```

The Agent is a static class and can, once initialized, be accessed anywhere
```java
        /**
         * The click of the button gets the step count for the past two weeks and shows it in a toast notification.
         * */
        AppCompatButton stepButton = (AppCompatButton) findViewById(R.id.step_button);
        stepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date now = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.add(Calendar.WEEK_OF_YEAR, -2);
                Date startTime = cal.getTime();

                GoogleFitAgent.getAggregateStepCountBetween(startTime, now, "stepCount", new GoogleFitAgent.QuantityReceiver() {
                    @Override
                    public void receiveQuantity(String requestID, Quantity quantity) {
                        /**
                         * request ID can be used to identify the request. (Not necessary in this
                         * case! Just for demonstration here.)
                         * */
                        if (requestID.equals("stepCount")) {
                            Toast.makeText(MainActivity.this, quantity.getValue().intValue() + " " + quantity.getUnit(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
```

Writing a DataPoint with the current time and date as start and end time:
```java
GoogleFitAgent.enterHeightDataPoint(getApplicationContext(), 1.68f);
GoogleFitAgent.enterWeightDataPoint(getApplicationContext(), 64f);
```


