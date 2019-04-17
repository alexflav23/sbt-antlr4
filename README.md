# sbt-antlr3

This plugin provides an ability to run antlr3 when compiling in sbt 1.1.x and 0.13.x.

## How to use

Put your .g4 files in `src/main/antlr3` directory and make `project/sbt-antlr3.sbt`
file with the following contents:

    // sbt 1.1.x
    addSbtPlugin("com.outworkers" % "sbt-antlr3" % "0.1.0")

And, enable the plugin in your `build.sbt` file.

    // sbt 1.1.x
    enablePlugins(Antlr3Plugin)

## Settings

You can select an antlr3 version with:

    antlr4Version in Antlr3 := "3.5.2" // default: 3.5.2

`-package` option can be defined by the following setting:

    antlr4PackageName in Antlr3 := Some("com.outworkers")

You can also adjust `-listener`, `-no-listener`, `-visitor`, `-no-visitor`, `-Werror` options:

    antlr4GenListener in Antlr3 := true // default: true

    antlr4GenVisitor in Antlr3 := false // default: false

    antlr4TreatWarningsAsErrors in Antlr3 := true // default: false
 
## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
