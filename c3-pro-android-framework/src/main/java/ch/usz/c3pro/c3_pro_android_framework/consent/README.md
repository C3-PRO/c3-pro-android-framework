Consent
-------------
The Consent module provides the tool to get the user's consent based on a FHIR `Contract`

### Module Interface

IN
- FHIR `Contract`

OUT
- ConsentSummary

##### ViewConsentTaskActivity

Although the `ViewConsentTaskActivity` is not a subclass of the ViewTaskActivity, it does work in the same way. It can be set up with a FHIR `Contract` and then be shown to the user through the `startActivityForResult` method. In the sample app, the Contract is read from a local resource:

```java
    private void launchConsent() {
        String contractString = ResourcePathManager.getResourceAsString(this, contractFilePath);
        Contract contract = Pyro.getFhirContext().newJsonParser().parseResource(Contract.class, contractString);

        Intent intent = ViewConsentTaskActivity.newIntent(this, contract);
        startActivityForResult(intent, 111);
    }
```

When the activity returns, the `ConsentSummary` can be read from the Extras:

```java
@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AppCompatActivity.RESULT_OK) {

            switch (requestCode) {
               case 111:
                    ConsentSummary summary = (ConsentSummary) data.getExtras().get(ViewConsentTaskActivity.EXTRA_CONSENT_SUMMARY);
                    Toast.makeText(MainActivity.this, "Eligibility Status is: " + eligible + "\nConsent Status is: " + summary.hasConsented(), Toast.LENGTH_SHORT).show();
                    break;
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
        
        }
    }
```
