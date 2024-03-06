This is BETA code! Please don't use it in production yet. This code will be ready to release shortly and this repo will be made public.

No support of any kind is provided without seperate arrangement. 

Currently the driver needs to explicitly return an "id" attribute if you want to use the OOTB mappings provided by the stock entitlement collector. You will need to add this attribute via policy.

In addition to any attributes returned by the driver, this collector will add "association", "class", and "entitlementDn" to the returned JSON objects.
The "id2" is the src-dn attribute of the instance. The "entitlementDn" is the dn configured in IG. This is returned because the stock fulfillment code depends on it.
The "class" is the object class of the returned instance element.

This collector does NOT use the entitlement DN in any other way and the entitlement does not need to exist in eDir if you are only collecting data.

If you enter only a search class, a basic DirXML query that includes no read-attr or search-attr is generated. You will get back whatever your driver shim returns by default.
If you enter a custom query, that query will be used.  Once again, this collector does not try to use any of the entitlement configuration from eDir when generating queries. 
This is intentional.

The stock entitlement collector transform the eDir GUID to a string. I intend to handle that in a collector transform instead to make this driver more generic. The stock driver applies this transform to any attribute named "GUID" which could possibly conflict. Suggestions on how to handle this are welcome.

This release does not validate the LDAP server certificate. This will be changed before release.

The following dependencies are required to build the project:

Daas-SDKServer.jar 

dirxml_misc.jar

jettison-1.3.7.jar

ldap.jar

logging-common-1.4.2-57.jar

slf4j-api-1.7.22.jar

XDS-4.8.0.0.jar

All except the Daas SDK are availiable from IG 4.2.  The Daas SDK is had to find but it is linked from the IG 3.7 docs. You can satisfy the dependencies from the SDK with additional jars from IG 4.2 if you need to.
