This code implement a custom collector for Opentext/NetIQ [Identity Governance](https://www.opentext.com/products/identity-governance-and-administration). 
It allows IG to to query account and permissions data from systems provisioned by the [Opentext/NetIQ Identity Manager](https://www.opentext.com/products/netiq-identity-manager) product.
Queries are submitted to the Identity Manager (DirXML) engine and the results are returned to IG. This collector can be used in place of the IDM Entitlement Collector that ships with IG.
The stock entitlement collector collects a limited set of attributes and only supports specific DirXML drivers. This collector is more generic and can be used with any DirXML driver.
This collector will work with the stock entitlement fulfillment code in IG if the appropriate attribute are mapped.

This code is provided as-is and is not supported by Opentext/NetIQ. Paid support for this code is available from [Idenhuas Consulting](https://idenhaus.com).
If ypu would like to contribute to this project, please fork the repository and submit a pull request.
Binary builds are available [here](https://software.pointbluetech.com/pb/oss/eec/v1.0/)

Example use cases for this driver include:
* Collecting accounts where you need more than association, id, description, and active status.
* Collecting permission assignments in the connected system for a driver that is not using IDM entitlements.

The collection process is shown in the following diagram:
![Process Flow](ProcessFlow.png)



Currently, the driver needs to explicitly return an "id" attribute if you want to use the OOTB mappings provided by the stock entitlement collector. You will need to add this attribute via policy if your driver shim does not return it.

In addition to any attributes returned by the driver, this collector will add "association", "class", and "entitlementDn" to the returned JSON objects.
The "id2" is the src-dn attribute of the instance. The "entitlementDn" is the dn configured in IG. This is returned because the stock fulfillment code depends on it.
The "class" is the object class of the returned instance element.

This collector does NOT use the entitlement DN in any other way and the entitlement does not need to exist in eDir if you are only collecting data.

If you enter only a search class, a basic DirXML query that includes no read-attr or search-attr is generated. You will get back whatever your driver shim returns by default.
If you enter a custom query, that query will be used.  Once again, this collector does not try to use any of the entitlement configuration from eDir when generating queries. 
This is intentional.

The stock entitlement collector transform the eDir GUID to a hex string. This collector does not do that. The GUID is returned as a Base64 encoded byte array. This is intentional.
You can perform the transformation in the collector mapping using the following JavaScript code:
```
function guidToString(guid) {
    if (guid != null && guid.length > 0) {
        return "";
    }
    let decoded = Buffer.from(guid, 'base64');
    let hex = decoded.toString('hex');
    let guidStr = hex.padStart(32, '0');
    return guidStr.toUpperCase();
}
outputValue = guidToString(inputValue);
```



This release requires LDAPS and does not validate the LDAP server certificate by default.

The following dependencies are required to build the project:

* Daas-SDKServer.jar 

* dirxml_misc.jar

* jettison-1.3.7.jar

* ldap.jar

* logging-common-1.4.2-57.jar

* slf4j-api-1.7.22.jar

* XDS-4.8.0.0.jar

All except the Daas SDK are available from IG 4.2.  The Daas SDK is had to find but it is linked from the IG 3.7 docs. You can satisfy the dependencies from the SDK with additional jars from IG 4.2 if you need to.
