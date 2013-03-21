What?
=====

An sbt plugin for publishing wiki pages to any wiki system supporting WikiRPC protocol.

How do I install it?
====================

For now, only source code access is provided until we can have (time to have) the binary published to Maven Central, fortunately sbt can use the source quite easily just by adding the plugin project in the build's build project and setting it as a dependency.

If you don't have a "full" definition of the build's build you'll have to create one by adding a ```project/project/build.scala``` file containing something like:

```scala
object BuildDef extends Build {

  override lazy val projects = Seq(build)
  
  lazy val build = Project("build", file(".")) dependsOn (sbtWikiPagesPlugin)
  
  lazy val sbtWikiPagesPlugin = uri("https://github.com/plalloni/sbt-wikipages-plugin.git#v0.1")
  
}
```

If you already have a such definition then you only need to add the missing parts from above.

And that's it.

Is there anything I need to setup?
==================================

You'll need to add some settings to your project for configuring the plugin's behavior as follows:

### Import plugin default settings

This will import plugin's default settings and tasks into your project.

```scala
plalloni.wikipages.WikiPages.newSettings
```

### Configure wiki pages source location

This tells the plugin where to find your wiki pages to publish to the wiki system of your choice.

```scala
WikiPagesKeys.wikiPages <<= baseDirectory(_ / "src/wiki")
```

This setting is optional and currently defaults to the value shown above.

### Configure published pages base name

This setting configures a String prefix to be prepended to the published page names.

As an example (we use) the following sets up a prefix based on the current project version:

```scala
WikiPagesKeys.wikiPagesBase <<= version("Some/Page/Name/" + _)
```

### Configure WikiRPC URL

This setting configures the WikiRPC endpoint exposed by the wiki system you are using.

An example for using with a [Trac](http://trac.edgewall.org/) system having the [XmlRpcPlugin](http://trac-hacks.org/wiki/XmlRpcPlugin) installed for exposing the JSON-RPC API and using authentication would be:

```scala
WikiPagesKeys.wikiPagesRpcUrl := "https://some.host/projects/somemodule/login/jsonrpc"
```

### Authentication

You will have to configure credentials for authenticating to the host specified in the WikiRPC URL using [common SBT practice](http://www.scala-sbt.org/release/docs/Detailed-Topics/Publishing.html#credentials).

A common case could be like:

```scala
credentials += Credentials(Path.userHome / ".sbt" / "wiki-credentials")
```

Then fill that file with appropriate the definitions for your URL and your wiki system, for the above URL it could be like the following:

```properties
realm=Trac
host=some.host
user=sometracuser
password=thepassword
```

How do I use it?
================

After all of the above is set you just need to execute the task ```publish-wiki-pages``` to set it off.

Running this task will recursively find all files named ```*.wiki``` inside the folder configured in ```WikiPagesKeys.wikiPages```, then it will map every file to a page name following this (theoretical) steps:

1. Make it relative to ```WikiPagesKeys.wikiPages``` (remove that value from the prefix of the name)
2. Remove the ```.wiki``` postfix
3. Prepend appropriately the ```WikiPagesKeys.wikiPagesBase``` value
4. Remove any postfix equal to ```/_```

Then it will send the files as wiki pages with the resulting names.

Example
=======

Asuming you have the following files in ```WikiPagesKeys.wikiPages```:

```
_.wiki
Page.wiki
Other/
      _.wiki
      Page.wiki
```

Then, asuming you have the value ```Prefix``` set in ```WikiPagesKeys.wikiPagesBase```, the task will send every one of those files as the corresponding pages as shown below:

```
   Files           Pages
--------------------------------
 _.wiki          Prefix
 Page.wiki       Prefix/Page
 Other/
      _.wiki     Prefix/Other
      Page.wiki  Prefix/Other/Page
```

Of course it is possible to create a collision of names by having both ```Other.wiki``` and ```Other/_.wiki``` but, for simplicity, that's not checked, both will be sent to the server and the last one (according to the FS) sent will win overwriting the previous page in the wiki.
