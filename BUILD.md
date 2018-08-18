### Building mondrian-rest

Over time we have encountered difficulty relying on stable Maven Nexus repositories at Pentaho, on which
Pentaho-administered codebases (Mondrian in particular) rely to build.  Specific to the case of mondrian-rest,
it does not appear (as of August 2018) that version 4.7.x of Mondrian is actually published to any Nexus repository,
anywhere.

As a result, to build mondrian-rest, you need to build mondrian first.

#### Minor OJBC changes to Mondrian codebase

mondrian-rest is dependent upon Mondrian version 4.4+.  Currently (Aug 2018) we use version 4.7.0.15.  However, we made two minor
tweaks to get it to build, which we did on a branch in our fork to make it easier to grab them.  You can see the diffs here:

https://github.com/pentaho/mondrian/compare/4.7.0.15-R...ojbc:ojbc-4.7.0.15-R

#### Build steps

You need to have Java 7+ and Maven installed.  But you'd need those for building mondrian-rest anyway, so hopefully you're all set with that.

1. `git clone https://github.com/ojbc/mondrian.git` into a working directory
1. `cd mondrian`
1. `git checkout ojbc-4.7.0.15-R`
1. `mvn -DskipTests install`

This will install mondrian in your local Maven repo, where the subsequent build of mondrian-rest will be able to find it.

We have not been able to get the Mondrian tests to run (they seem to require a lot of memory and at some point just hang...)

#### Continued Dependency on Pentaho Nexus repositories

mondrian-rest is no longer dependent upon Pentaho Nexus repositories directly, but the mondrian codebase is.  Its dependencies
are actually quite modest, and most of them are standard Java libraries that are available in Maven central.  But a few,
like olap4j and eigenbase, are not--at least not at the versions that mondrian 4.7 requires.  Rather than requiring that we also build those from source, for now we are leaving
the mondrian dependency on Pentaho Nexus alone.  But we could revisit this in the future if we encounter problems building
mondrian with Pentaho Nexus repos.
