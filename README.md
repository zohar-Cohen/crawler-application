# crawler-application
A web crawler application that scans recursively using multi-thread fork join implementation, parses the given domain address, and provides a response with all the
internal pages and their internal links that are associated with the static page assets (please see example.json file for example response).

Once the application is up and running, it listens to the default 8080 port, and expects the url as a path parameter in the HTTP Get method.
Example: the below end-point will scan www.sedna.com, and will provide all dependent pages and links with the associated assets.
curl --location --request GET 'http://localhost:8080/web-crawler/v1/scan?url=https://www.sedna.com' --header 'activity-id: 1234'
