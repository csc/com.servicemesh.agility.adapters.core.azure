# com.servicemesh.agility.adapters.core.azure

The com.servicemesh.agility.adapters.core.azure bundle is provided to aid in the development of a CSC Agility Platform&trade; adapter for Microsoft Azure&trade;. This bundle provides communications and utility functions for interacting with Microsoft Azure.

## core.azure Usage
The primary interfaces in core.azure for communications are:
* com.servicemesh.agility.adapters.core.azure.AzureConnection
* com.servicemesh.agility.adapters.core.azure.AzureEndpoint

`AzureEnpdoint` represents the access point for a Microsoft Azure service REST API, including data serialization.

`AzureConnection` provides create/retrieve/update/delete operations to an AzureEndpoint.

The core.azure bundle uses log4j and has two levels to assist in adapter troubleshooting - *DEBUG* and the finer-grained *TRACE* - that by default are not enabled. To enable both, add the following line to `/opt/agility-platform/etc/com.servicemesh.agility.logging.cfg`:
```
log4j.logger.com.servicemesh.agility.adapters.core.azure=TRACE
```
To only enable the *DEBUG* level, use *DEBUG* instead of *TRACE* in `com.servicemesh.agility.logging.cfg`.

### Build Configuration
core.azure is compatible with Java 8 and ant 1.9.3.

core.azure is dependent on the [csc-agility-platform-sdk project](https://github.com/csc/csc-agility-platform-sdk). The core.azure ant build file requires that csc-agility-platform-sdk be built first.

If you want to edit core.azure using Eclipse you'll need to define Eclipse build path variables:
* IVY-LIB: Contains the path to the *ivy-lib* directory under csc-agility-platform-sdk
* COMMON-LIB: Contains the path to the *lib* directory under csc-agility-platform-sdk
* DIST: Contains the path to the *dist* directory under csc-agility-platform-sdk

### Reference Implementations
Examples of utilizing the core.azure bundle with an Azure API is provided with the unit test:
* TestTrafficMgrIntegration.java: Microsoft Azure Traffic Manager&trade;

Two complete reference implementations that utilize the CSC Agility Platform Services SDK and the core.azure bundle are provided under the [csc-agility-platform-services-sdk-reference-info project](https://github.com/csc/csc-agility-platform-services-sdk-reference-info).

* com.servicemesh.agility.adapters.service.azure.sql is a CSC Agility Platform
  service adapter to the Microsoft Azure SQL&trade; service.
* com.servicemesh.agility.adapters.service.azure.trafficmanager is a CSC Agility
  Platform service adapter to the Microsoft Azure Traffic Manager&trade; service.

## Unit Testing
For maximum unit testing that includes direct interaction with Microsoft Azure, populate a junit.properties file in the base directory with valid credentials:
```
azure_certificate=<full-path-to-file-containing-certificate>
azure_certificate_password=<unencrypted-password>
azure_subscription=<subscription-string>
```

To generate and view code coverage metrics, open the coverage/report/index.html file after running this command:
```
$ ant clean compile coverage-report -Dcoverage.format=html
```

## License
core.azure is distributed under the Apache 2.0 license. See the [LICENSE](https://github.com/csc/com.servicemesh.agility.adapters.core.azure/blob/master/LICENSE) file for full details.
