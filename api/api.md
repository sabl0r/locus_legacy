locus API
=========

DRAFT!

API endpoint: $serverurl/api/

POST location
 * username:     non-empty string
 * latitude:     double between -90 and 90
 * longitude:    double between -180 and 180
 * accuracy:     non-negative double
 * provider:     network|gps
 * accesspoints: json a la [{id: MAC, l: negative integer}, ...]
