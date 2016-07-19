C3PRO
-----
The `C3PRO` class provides the resource hungry `FhirContext` as a singleton and can also provide a `DataQueue` if set up with a FHIR server URL.

### Module Interface

IN
- FHIR server URL
- Android `Context`
OUT
- HAPI `FhirContext`
- `DataQueue`


##### Setup

In the `Application` class, initialize with context and FHIR server url:
```java
C3PRO.init(this, "http://fhirtest.uhn.ca/baseDstu3");
```