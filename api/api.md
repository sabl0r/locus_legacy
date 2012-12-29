locus API
=========

This is a draft!

The API endpoint is $serverurl/api/ . All strings must be non-empty if not
noted otherwise.

Types
-----

 * access point: JSON object {"id":...,"l":...}, where id is the MAC and l is
   the signal level as negative integer

POST location
-------------

Reports the user's current location.

 * username
 * latitude: double between -90 and 90
 * longitude: double between -180 and 180
 * accuracy: non-negative double
 * provider: network|gps
 * accesspoints: JSON list of access points

POST pois
---------

Tags the user's current location / POI, i.e. creates a POI or updates one if it
already exists.

 * username
 * name: name of the current location / POI
 * accesspoints: JSON list of access points
