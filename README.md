# NK-GraalVM Language Runtime

An experiment to demonstrate running [NetKernel](http://1060research.com/products/#netkernel) from [1060 Research](http://1060research.com/) on the Oracle [GraalVM](https://www.graalvm.org/) virtual machine.

**What is GraalVM?**

(From [www.graalvm.org](www.graalvm.org)):

>GraalVM is a universal virtual machine for running applications written in JavaScript, Python 3, Ruby, R, JVM-based languages like Java, Scala, Kotlin, and LLVM-based languages such as C and C++.
>GraalVM removes the isolation between programming languages and enables interoperability in a shared runtime. It can run either standalone or in the context of OpenJDK, Node.js, Oracle Database, or MySQL.

**What Is NetKernel?**

(From [1060research.com/products/](http://1060research.com/products/)):

>NetKernel® delivers a high performance, emergent and scalable platform to develop, integrate and operate complex service and data architectures. NetKernel implements Resource Oriented Computing® and achieves a new software economics. By taking Micro-Services to the ultimate level, NetKernel® delivers huge technical breakthroughs for development, operations and architecture engineering...

## Pre-requisites

[Download](http://1060research.com/resources/#quick) and install NetKernel (either EE or SE editions will do) on your machine.

[Download](https://www.graalvm.org/downloads/) and [install](https://www.graalvm.org/docs/getting-started/#install-graalvm) GraalVM on your machine (currently only supports Linux and Mac OSX).  

You will need to run NetKernel using the GraalVM JVM instead of your normal JVM installation. Follow the instructions on the GraalVM install page to temporarily set the PATH environment variable in a Terminal session in which you will launch NetKernel. 

e.g (for OSX):

```
$ export GRAALVM_HOME=/Users/richardsmith/Projects/graalvm-ce-1.0.0-rc6
$ export PATH=$GRAALVM_HOME/Contents/Home/bin:$PATH
```

To run all of the examples below, you will need to use the [GraalVM Updater utility](https://www.graalvm.org/docs/reference-manual/graal-updater/) to install additional language packs. Execute the following commands in the terminal session with the modified `$PATH`:

```
$ gu available
$ gu install python
$ gu install R
$ gu install ruby
$ gu list
ComponentId              Version             Component name
----------------------------------------------------------------
R                        1.0.0-rc6           FastR
python                   1.0.0-rc6           Graal.Python
ruby                     1.0.0-rc6           TruffleRuby
```

(Optional) Add the `graal.ShowConfiguration` GraalVM compiler flag to the NetKernel `bin/jvmsettings.cnf` file to print a confirmation message in the console log that NetKernel is indeed running on the GraalVM JVM:

```
-server -XX:+UseParallelOldGC -Xmx1024m -Xms1024m -Dfile.encoding=UTF-8 -Dgraal.ShowConfiguration=info
```

Then launch Netkernel in the same Terminal session:

```
$ cd /path/to/netkernel/bin
$ ./netkernel.sh
```

You should see a message similar to that below in the console log:

```
Using Graal compiler configuration 'community' provided by org.graalvm.compiler.hotspot.CommunityCompilerConfigurationFactory loaded from jar:file:/Users/richardsmith/Projects/graalvm-ce-1.0.0-rc6/Contents/Home/jre/lib/jvmci/graal.jar!/org/graalvm/compiler/hotspot/CommunityCompilerConfigurationFactory.class
I 10:53:32 Kernel
Starting 1060-NetKernel-EE
Resource Oriented Computing Platform
Version 6.1.1
Copyright 2002-2018, 1060 Research Limited  http://www.1060research.com
1060, NetKernel, Resource Oriented Computing are Trademarks of 1060 Research Ltd.
```

You will also need the [Gradle build tool](https://gradle.org/) to follow the module installation instructions below.

## Installation

1. Clone or download this repo onto your machine.

2. Edit the `gradle/common/common.properties.gradle` file, changing the `NKLOCATION` property to the path of your NetKernel installation. Make sure you leave a trailing `/` at the end of the path.

3. Edit the `modules/urn.uk.co.rsbatechnology.lang.graalvm/gradle.build` file, changing the `edition` property to either `EE` or `SE`, depending on the edition of your NetKernel installation.
 
4. To build this module, you must use the GraalVM java binaries, instead of your normal JDK.  Open another Terminal window and set the following environment variables (change to the GraalVM path on your machine):

```
$ export GRAALVM_HOME=/Users/richardsmith/Projects/graalvm-ce-1.0.0-rc6
$ export PATH=$GRAALVM_HOME/Contents/Home/bin:$PATH
$ export JAVA_HOME=$GRAALVM_HOME/Contents/Home
```

5. Run the following commands to deploy the GraalVM module into your NetKernel installation:

```
$ cd modules/urn.uk.co.rsbatechnology.lang.graalvm
$ gradle clean build deployModuleNK
```

6. The module includes an example C file, `src/resources/hello.c` compiled into LLVM bitcode, `src/resources/hello.bc` on OSX.  You may need to recompile the C file on your own machine (but try it as-is first).  Please see the [GraalVM LLVM Languages Reference](https://www.graalvm.org/docs/reference-manual/languages/llvm/) for more information.  For reference, this is the compile command I used on my OSX development machine:

```
clang -g -O1 -c -emit-llvm -I$GRAALVM_HOME/jre/languages/llvm hello.c
```



## Tests

Trace any of the following declarative requests into the `urn:uk:co:rsbatechnology:lang:graalvm:examples` space in the `Lang / GraalVM` module.  The examples space declaration in the module.xml file contains example literal endpoints with language-specific scripts.

Javascript:
```
<request>
    <verb>SOURCE</verb>
    <identifier>active:graalvm</identifier>
    <argument name="operator">res:/js/script</argument>
   <argument name="graalvm-language">js</argument>
</request>
```

R:
```
<request>
    <verb>SOURCE</verb>
    <identifier>active:graalvm</identifier>
    <argument name="operator">res:/r/script</argument>
   <argument name="graalvm-language">R</argument>
</request>
```

Ruby:
```
<request>
    <verb>SOURCE</verb>
    <identifier>active:graalvm</identifier>
    <argument name="operator">res:/ruby/script</argument>
   <argument name="graalvm-language">ruby</argument>
</request>
```

Python:
```
<request>
    <verb>SOURCE</verb>
    <identifier>active:graalvm</identifier>
    <argument name="operator">res:/python/script</argument>
   <argument name="graalvm-language">python</argument>
</request>
```

LLVM (e.g. C):
```
<request>
    <verb>SOURCE</verb>
    <identifier>active:graalvm</identifier>
    <argument name="operator">res:/resources/hello.bc</argument>
   <argument name="graalvm-language">llvm</argument>
</request>
```


Each language example performs the same actions, SOURCEing a dummy resource and using its return value as the request response.  If the scripts work, you will see `Hello from GraalVM` as the traced request response in the **Request Trace** tool screen.

