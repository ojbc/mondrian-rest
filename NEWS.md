## mondrian-rest news / release notes

#### Version 2.0.3

13 February 2020

* Improved and more consistent logging
* Allow pre-caching of metadata per connection

#### Version 2.0.2

8 February 2020

* Fix metadata cache to cache metadata objects by connection + role
* Add a disk cache for metadata objects
* Control cache parameters (for metadata and query caches) via spring properties

#### Version 2.0.1

23 January 2020

* Ensure `SchemaWrapper.getCubes()` returns cubes in order specified in mondrian schema xml

#### Version 2.0.0

21 January 2020

* Migrate to mondrian 8.x

#### Version 1.5.0

* Add saml token strategies and an impl that gets token from saml subject nameid
* Last version of the controller that supports Mondrian 4.x schemas

#### Versions prior to 1.5.0

_we will document prior versions as necessary; see `git log` for details_
