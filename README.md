# EPD Editor
This is an editor for [ILCD](http://eplca.jrc.ec.europa.eu/LCDN/developer.xhtml)
data sets with [EPD format extensions](http://www.oekobaudat.de/en/info/working-group-indata.html). 

## Building from source
The EPD editor is an [Eclipse RCP](https://wiki.eclipse.org/Rich_Client_Platform)
application. To compile it from source you need to have the following tools
installed:

* a [Java Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven](http://maven.apache.org/)
* the [Eclipse package for RCP developers](https://www.eclipse.org/downloads/)

When you have these tools installed you can build the application from source
via the following steps:

#### Install the openLCA core modules and ILCD validation API
The EPD Editor uses the current version of the 
[openLCA core modules](https://github.com/GreenDelta/olca-modules) for reading
and writing ILCD data sets and the [ILCD validation API](https://bitbucket.org/okusche/ilcdvalidation)
for validating them. These modules are plain Maven projects and can be
installed via `mvn install`. See the documentation of both projects for a
more information.

#### Get the source code of the application
We recommend to use Git to manage the source code but you can also download the
source code as a [zip file](https://github.com/GreenDelta/epd-editor/archive/master.zip).
If you have Git installed, just clone the repository via:

    git clone https://github.com/GreenDelta/epd-editor.git

The project folder should look like this:

    epd-editor
      .git/
      src/
      icons/
      META_INF/
      ...
      pom.xml
      ...

#### Copy the Maven dependencies
We use Maven to manage our non-Eclipse library dependencies. To pull them into
the project, just execute `mvn package` in the project folder:

```bash
cd epd-editor
mvn package
```

This will copy these libraries under the `epd-editor/libs` folder. 

#### Set up the Eclipse project
Open Eclipse and select/create a workspace directory. Import the `epd-editor` 
project into Eclipse via `Import/General/Existing Projects into Workspace`
(select the `epd-editor` folder). Open the file `platform.target` and click on
'Set as target platform' on the top right of the editor. This will download the
runtime platform into the folder `.metadata/.plugins/org.eclipse.pde.core/.bundle_pool`
of your workspace and thus may take a bit of time. After this, the project should
have no compile errors and you should be able to open the `app.product` file
and launch the application (click on `Launch an Eclipse application`).

#### Labels and translations
Labels and translations are externalized in the `src/app/messages*.properties`
files. The keys in these files map to a static field in the class `app.M` which
are then used in the Java code. It is recommended to use 
[JLokalize](http://jlokalize.sourceforge.net) to edit the `messages*.properties`
files. Labels that are not externalized yet start with a hash mark `#`. Thus,
searching the Java source code for `"#` should give a list of strings that need
to be externalized (if there are any). The script `scripts/make_messages_fields.py`
generates the list of fields for the class `app.M` from the `messages.properties`
file.

#### Validation profile
The EPD-Editor uses the EPD profile from the 
[ILCD Validation API](https://bitbucket.org/okusche/ilcdvalidation). This
profile needs to be located under `validation_profile/EPD_validation_profile.jar`
and is not added to this repository. Thus, you need to copy the EPD profile
from the validation API to this location before testing the validation feature
or running a build.

#### Building the distribution packages
...

## License
Unless stated otherwise, all source code of the openLCA project is licensed
under the [Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/). Please
see the [LICENSE.md](./LICENSE.md) file in the root directory of the source code.

