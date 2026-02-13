Initialized native services in: C:\Users\woute\.gradle\native
Initialized jansi services in: C:\Users\woute\.gradle\native
Received JVM installation metadata from 'C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot': {JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot, JAVA_VERSION=17.0.17, JAVA_VENDOR=Eclipse Adoptium, RUNTIME_NAME=OpenJDK Runtime Environment, RUNTIME_VERSION=17.0.17+10, VM_NAME=OpenJDK 64-Bit Server VM, VM_VERSION=17.0.17+10, VM_VENDOR=Eclipse Adoptium, OS_ARCH=amd64}
Found daemon DaemonInfo{pid=793228, address=[c20a581f-436d-4310-b53c-4e6f9bd70701 port:49341, addresses:[/127.0.0.1]], state=Idle, lastBusy=1769634495203, context=DefaultDaemonContext[uid=cf7a66de-5eea-4c5f-8dd8-f892e30df3e0,javaHome=C:\Users\woute\.jdk\jdk-11.0.28,javaVersion=11,javaVendor=Microsoft,daemonRegistryDir=C:\Users\woute\.gradle\daemon,pid=793228,idleTimeout=10800000,priority=NORMAL,applyInstrumentationAgent=true,nativeServicesMode=ENABLED,daemonOpts=--add-opens=java.base/java.util=ALL-UNNAMED,--add-opens=java.base/java.lang=ALL-UNNAMED,--add-opens=java.base/java.lang.invoke=ALL-UNNAMED,--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED,--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED,--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED,--add-opens=java.base/java.nio.charset=ALL-UNNAMED,--add-opens=java.base/java.net=ALL-UNNAMED,--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED,-XX:MaxMetaspaceSize=384m,-XX:+HeapDumpOnOutOfMemoryError,-Xms256m,-Xmx512m,-Dfile.encoding=windows-1252,-Duser.country=GB,-Duser.language=en,-Duser.variant]} however its context does not match the desired criteria.
JVM is incompatible.
Wanted: DaemonRequestContext{jvmCriteria=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot (no JDK specified, using current Java home), daemonOpts=[--add-opens=java.base/java.util=ALL-UNNAMED, --add-opens=java.base/java.lang=ALL-UNNAMED, --add-opens=java.base/java.lang.invoke=ALL-UNNAMED, --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED, --add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED, --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED, --add-opens=java.base/java.nio.charset=ALL-UNNAMED, --add-opens=java.base/java.net=ALL-UNNAMED, --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED, -XX:MaxMetaspaceSize=384m, -XX:+HeapDumpOnOutOfMemoryError, -Xms256m, -Xmx512m, -Dfile.encoding=windows-1252, -Duser.country=GB, -Duser.language=en, -Duser.variant], applyInstrumentationAgent=true, nativeServicesMode=ENABLED, priority=NORMAL}
Actual: DefaultDaemonContext[uid=cf7a66de-5eea-4c5f-8dd8-f892e30df3e0,javaHome=C:\Users\woute\.jdk\jdk-11.0.28,javaVersion=11,javaVendor=Microsoft,daemonRegistryDir=C:\Users\woute\.gradle\daemon,pid=793228,idleTimeout=10800000,priority=NORMAL,applyInstrumentationAgent=true,nativeServicesMode=ENABLED,daemonOpts=--add-opens=java.base/java.util=ALL-UNNAMED,--add-opens=java.base/java.lang=ALL-UNNAMED,--add-opens=java.base/java.lang.invoke=ALL-UNNAMED,--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED,--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED,--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED,--add-opens=java.base/java.nio.charset=ALL-UNNAMED,--add-opens=java.base/java.net=ALL-UNNAMED,--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED,-XX:MaxMetaspaceSize=384m,-XX:+HeapDumpOnOutOfMemoryError,-Xms256m,-Xmx512m,-Dfile.encoding=windows-1252,-Duser.country=GB,-Duser.language=en,-Duser.variant]

  Looking for a different daemon...
Found daemon DaemonInfo{pid=175788, address=[79d39f20-2445-4f02-a4f5-6f674dae3555 port:18840, addresses:[/127.0.0.1]], state=Idle, lastBusy=1770150814717, context=DefaultDaemonContext[uid=d82e5333-06bd-4275-a97b-d0b3c2ddd9a8,javaHome=C:\Users\woute\.vscode\extensions\redhat.java-1.51.0-win32-x64\jre\21.0.9-win32-x86_64,javaVersion=21,javaVendor=Eclipse Adoptium,daemonRegistryDir=C:\Users\woute\.gradle\daemon,pid=175788,idleTimeout=10800000,priority=NORMAL,applyInstrumentationAgent=true,nativeServicesMode=ENABLED,daemonOpts=--add-opens=java.base/java.util=ALL-UNNAMED,--add-opens=java.base/java.lang=ALL-UNNAMED,--add-opens=java.base/java.lang.invoke=ALL-UNNAMED,--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED,--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED,--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED,--add-opens=java.base/java.nio.charset=ALL-UNNAMED,--add-opens=java.base/java.net=ALL-UNNAMED,--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED,-XX:MaxMetaspaceSize=384m,-XX:+HeapDumpOnOutOfMemoryError,-Xms256m,-Xmx512m,-Dfile.encoding=UTF-8,-Duser.country=GB,-Duser.language=en,-Duser.variant]} however its context does not match the desired criteria.
JVM is incompatible.
Wanted: DaemonRequestContext{jvmCriteria=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot (no JDK specified, using current Java home), daemonOpts=[--add-opens=java.base/java.util=ALL-UNNAMED, --add-opens=java.base/java.lang=ALL-UNNAMED, --add-opens=java.base/java.lang.invoke=ALL-UNNAMED, --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED, --add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED, --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED, --add-opens=java.base/java.nio.charset=ALL-UNNAMED, --add-opens=java.base/java.net=ALL-UNNAMED, --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED, -XX:MaxMetaspaceSize=384m, -XX:+HeapDumpOnOutOfMemoryError, -Xms256m, -Xmx512m, -Dfile.encoding=windows-1252, -Duser.country=GB, -Duser.language=en, -Duser.variant], applyInstrumentationAgent=true, nativeServicesMode=ENABLED, priority=NORMAL}
Actual: DefaultDaemonContext[uid=d82e5333-06bd-4275-a97b-d0b3c2ddd9a8,javaHome=C:\Users\woute\.vscode\extensions\redhat.java-1.51.0-win32-x64\jre\21.0.9-win32-x86_64,javaVersion=21,javaVendor=Eclipse Adoptium,daemonRegistryDir=C:\Users\woute\.gradle\daemon,pid=175788,idleTimeout=10800000,priority=NORMAL,applyInstrumentationAgent=true,nativeServicesMode=ENABLED,daemonOpts=--add-opens=java.base/java.util=ALL-UNNAMED,--add-opens=java.base/java.lang=ALL-UNNAMED,--add-opens=java.base/java.lang.invoke=ALL-UNNAMED,--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED,--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED,--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED,--add-opens=java.base/java.nio.charset=ALL-UNNAMED,--add-opens=java.base/java.net=ALL-UNNAMED,--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED,-XX:MaxMetaspaceSize=384m,-XX:+HeapDumpOnOutOfMemoryError,-Xms256m,-Xmx512m,-Dfile.encoding=UTF-8,-Duser.country=GB,-Duser.language=en,-Duser.variant]

  Looking for a different daemon...
Found daemon DaemonInfo{pid=3644, address=[06008d7b-57b2-470a-ae0d-44ab8a2ff1dd port:21503, addresses:[/127.0.0.1]], state=Idle, lastBusy=1771015963580, context=DefaultDaemonContext[uid=e51a0ef7-abd0-4412-adea-061a2fb3c0b6,javaHome=C:\Users\woute\.vscode\extensions\redhat.java-1.52.0-win32-x64\jre\21.0.9-win32-x86_64,javaVersion=21,javaVendor=Eclipse Adoptium,daemonRegistryDir=C:\Users\woute\.gradle\daemon,pid=3644,idleTimeout=10800000,priority=NORMAL,applyInstrumentationAgent=true,nativeServicesMode=ENABLED,daemonOpts=--add-opens=java.base/java.util=ALL-UNNAMED,--add-opens=java.base/java.lang=ALL-UNNAMED,--add-opens=java.base/java.lang.invoke=ALL-UNNAMED,--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED,--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED,--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED,--add-opens=java.base/java.nio.charset=ALL-UNNAMED,--add-opens=java.base/java.net=ALL-UNNAMED,--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED,-XX:MaxMetaspaceSize=384m,-XX:+HeapDumpOnOutOfMemoryError,-Xms256m,-Xmx512m,-Dfile.encoding=UTF-8,-Duser.country=GB,-Duser.language=en,-Duser.variant]} however its context does not match the desired criteria.
JVM is incompatible.
Wanted: DaemonRequestContext{jvmCriteria=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot (no JDK specified, using current Java home), daemonOpts=[--add-opens=java.base/java.util=ALL-UNNAMED, --add-opens=java.base/java.lang=ALL-UNNAMED, --add-opens=java.base/java.lang.invoke=ALL-UNNAMED, --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED, --add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED, --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED, --add-opens=java.base/java.nio.charset=ALL-UNNAMED, --add-opens=java.base/java.net=ALL-UNNAMED, --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED, -XX:MaxMetaspaceSize=384m, -XX:+HeapDumpOnOutOfMemoryError, -Xms256m, -Xmx512m, -Dfile.encoding=windows-1252, -Duser.country=GB, -Duser.language=en, -Duser.variant], applyInstrumentationAgent=true, nativeServicesMode=ENABLED, priority=NORMAL}
Actual: DefaultDaemonContext[uid=e51a0ef7-abd0-4412-adea-061a2fb3c0b6,javaHome=C:\Users\woute\.vscode\extensions\redhat.java-1.52.0-win32-x64\jre\21.0.9-win32-x86_64,javaVersion=21,javaVendor=Eclipse Adoptium,daemonRegistryDir=C:\Users\woute\.gradle\daemon,pid=3644,idleTimeout=10800000,priority=NORMAL,applyInstrumentationAgent=true,nativeServicesMode=ENABLED,daemonOpts=--add-opens=java.base/java.util=ALL-UNNAMED,--add-opens=java.base/java.lang=ALL-UNNAMED,--add-opens=java.base/java.lang.invoke=ALL-UNNAMED,--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED,--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED,--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED,--add-opens=java.base/java.nio.charset=ALL-UNNAMED,--add-opens=java.base/java.net=ALL-UNNAMED,--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED,-XX:MaxMetaspaceSize=384m,-XX:+HeapDumpOnOutOfMemoryError,-Xms256m,-Xmx512m,-Dfile.encoding=UTF-8,-Duser.country=GB,-Duser.language=en,-Duser.variant]

  Looking for a different daemon...
The client will now receive all logging from the daemon (pid: 94668). The daemon log file: C:\Users\woute\.gradle\daemon\8.10\daemon-94668.out.log
Starting 4th build in daemon [uptime: 23 mins 41.391 secs, performance: 100%, GC rate: 0.00/s, heap usage: 0% of 512 MiB, non-heap usage: 20% of 384 MiB]
Using 16 worker leases.
Now considering [C:\Users\woute\Code\fork\shortest-path] as hierarchies to watch
Watching the file system is configured to be enabled if available
File system watching is active
Starting Build
Settings evaluated using settings file 'C:\Users\woute\Code\fork\shortest-path\settings.gradle'.
Projects loaded. Root project using build file 'C:\Users\woute\Code\fork\shortest-path\build.gradle'.
Included projects: [root project 'shortest-path']

> Configure project :
Evaluating root project 'shortest-path' using build file 'C:\Users\woute\Code\fork\shortest-path\build.gradle'.
All projects evaluated.
Task name matched 'test'
Selected primary task 'test' from project :
Tasks to be executed: [task ':compileJava', task ':processResources', task ':classes', task ':compileTestJava', task ':processTestResources', task ':testClasses', task ':test']
Tasks that were excluded: []
Resolve mutations for :compileJava (Thread[Execution worker,5,main]) started.
:compileJava (Thread[Execution worker,5,main]) started.

> Task :compileJava UP-TO-DATE
Caching disabled for task ':compileJava' because:
  Build cache is disabled
Skipping task ':compileJava' as it is up-to-date.
Resolve mutations for :processResources (Thread[Execution worker,5,main]) started.
:processResources (Thread[Execution worker,5,main]) started.

> Task :processResources
Caching disabled for task ':processResources' because:
  Build cache is disabled
  Not worth caching
Task ':processResources' is not up-to-date because:
  Input property 'rootSpec$1' file C:\Users\woute\Code\fork\shortest-path\src\main\resources\transports\agility_shortcuts.tsv has changed.
  Input property 'rootSpec$1' file C:\Users\woute\Code\fork\shortest-path\src\main\resources\transports\transports.tsv has changed.
  Input property 'rootSpec$1' file C:\Users\woute\Code\fork\shortest-path\src\main\resources\transports\.~lock.agility_shortcuts.tsv# has been added.
Resolve mutations for :classes (Thread[Execution worker,5,main]) started.
:classes (Thread[Execution worker,5,main]) started.

> Task :classes
Skipping task ':classes' as it has no actions.
Resolve mutations for :compileTestJava (Thread[Execution worker,5,main]) started.
:compileTestJava (Thread[Execution worker,5,main]) started.

> Task :compileTestJava
Caching disabled for task ':compileTestJava' because:
  Build cache is disabled
Task ':compileTestJava' is not up-to-date because:
  Input property 'stableSources' file C:\Users\woute\Code\fork\shortest-path\src\test\java\shortestpath\AgilityShortcutTest.java has changed.
Compilation mode: in-process compilation
Created classpath snapshot for incremental compilation in 0.008 secs.
Compiling with toolchain 'C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot'.
Compiling with JDK Java compiler API.
Incremental compilation of 2 classes completed in 0.189 secs.
Class dependency analysis for incremental compilation took 0.003 secs.
Resolve mutations for :processTestResources (Thread[Execution worker,5,main]) started.
:processTestResources (Thread[Execution worker,5,main]) started.

> Task :processTestResources NO-SOURCE
Skipping task ':processTestResources' as it has no source files and no previous output files.
Resolve mutations for :testClasses (Thread[Execution worker,5,main]) started.
:testClasses (Thread[Execution worker,5,main]) started.

> Task :testClasses
Skipping task ':testClasses' as it has no actions.
Resolve mutations for :test (Thread[Execution worker,5,main]) started.
:test (Thread[Execution worker,5,main]) started.
Gradle Test Executor 3 started executing tests.

> Task :test
Custom actions are attached to task ':test'.
Caching disabled for task ':test' because:
  Build cache is disabled
  Gradle does not know how file 'build\test-results\test\binary\output.bin.idx' was created (output property 'binaryResultsDirectory'). Task output caching requires exclusive access to output paths to guarantee correctness (i.e. multiple tasks are not allowed to produce output in the same location).
Task ':test' is not up-to-date because:
  Task has failed previously.
Starting process 'Gradle Test Executor 3'. Working directory: C:\Users\woute\Code\fork\shortest-path Command: C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe -Dorg.gradle.internal.worker.tmpdir=C:\Users\woute\Code\fork\shortest-path\build\tmp\test\work -javaagent:C:\Users\woute\Code\fork\shortest-path\build\tmp\.cache\expanded\zip_cde35f471dab581134460fc9a50e2c59\jacocoagent.jar=destfile=build/jacoco/test.exec,append=true,inclnolocationclasses=false,dumponexit=true,output=file,jmx=false @C:\Users\woute\.gradle\.tmp\gradle-worker-classpath13791511471072756483txt -Xms1g -Xmx8g -Dfile.encoding=windows-1252 -Duser.country=GB -Duser.language=en -Duser.variant -ea worker.org.gradle.process.internal.worker.GradleWorkerMain 'Gradle Test Executor 3'
Successfully started process 'Gradle Test Executor 3'

AgilityShortcutTest > everyEnumHasTsvEntry STANDARD_OUT
    Skipping excluded ID 46815 for WEISS_BROKEN_FENCE
    Skipping excluded ID 11948 for AGILITY_PYRAMID_ROCKS_WEST
    Skipping excluded ID 23644 for KARAMJA_GLIDER_LOG
    Skipping excluded ID 23568 for KARAMJA_MOSS_GIANT_SWING
    Skipping excluded ID 23569 for KARAMJA_MOSS_GIANT_SWING
    Missing TSV entries for the following AgilityShortcut enum constants:
    CRABCLAW_CAVES_ROCKS
    DEEP_WILDERNESS_DUNGEON_CREVICE_NORTH
    DEEP_WILDERNESS_DUNGEON_CREVICE_SOUTH
    TONALI_CAVERN_STEPPING_STONE
    TONALI_CAVERN_LOG_BALANCE
    GREAT_CONCH_CLIFF_SHORTCUT_TOWN
    ANGLERS_RETREAT_SHORTCUT
    GREAT_CONCH_CLIFF_SHORTCUT_EAST_1
    MOTHERLODE_MINE_WALL_EAST
    MOTHERLODE_MINE_WALL_WEST
    GREAT_CONCH_CLIFF_SHORTCUT_SOUTHEAST_BOTTOM
    GREAT_CONCH_CLIFF_SHORTCUT_EAST_2
    ASGARNIA_ICE_DUNGEON_TUNNEL_WEST
    GREAT_CONCH_STEPPING_STONE
    LAGUNA_AURORAE_SHORTCUT_1
    LAGUNA_AURORAE_SHORTCUT_2
    HEROES_GUILD_TUNNEL_EAST
    HEROES_GUILD_TUNNEL_WEST
    FENKENSTRAIN_MAUSOLEUM_BRIDGE
    GU_TANOTH_CRUMBLING_WALL
    ASGARNIA_ICE_DUNGEON_BASIC_SOUTH
    BARROWS_WALL_JUMP
    COF_PLATFORM_TOP
    COF_PLATFORM_MID
    MEIYERDITCH_LAB_TUNNELS_NORTH
    MEIYERDITCH_LAB_TUNNELS_SOUTH
    FOSSIL_ISLAND_ZIPLINE
    MOKHAIOTL_PIT_JUMP
    DARKFROST_CLIFF_SCRAMBLE
    WILDERNESS_SLAYER_CAVE_CREVICE_NORTH_EAST
    WILDERNESS_SLAYER_CAVE_CREVICE_SOUTH_EAST
    WILDERNESS_SLAYER_CAVE_CREVICE_NORTH_WEST
    WILDERNESS_SLAYER_CAVE_CREVICE_SOUTH_WEST
    IORWERTHS_DUNGEON_NORTHERN_SHORTCUT_EAST
    IORWERTHS_DUNGEON_NORTHERN_SHORTCUT_WEST
    KHARAZI_JUNGLE_VINE_CLIMB
    SLAYER_TOWER_IVY
    SLAYER_TOWER_TOP_WINDOW
    WATERBIRTH_DUNGEON_CREVICE
    ASGARNIA_ICE_DUNGEON_ADEPT_EAST
    COF_SHORTCUT_TOP
    GRIMSTONE_SHORTCUT_SOUTH
    IORWERTHS_DUNGEON_SOUTHERN_SHORTCUT_EAST
    IORWERTHS_DUNGEON_SOUTHERN_SHORTCUT_WEST
    DEEPFIN_CAVE_SHORTCUT
    DEEPFIN_CAVE_SHORTCUT_ICON
    BRIMHAVEN_DUNGEON_VINE_EAST
    BRIMHAVEN_DUNGEON_VINE_WEST
    MOUNT_KARUULM_PIPE_SOUTH
    MOUNT_KARUULM_PIPE_NORTH
    VIYELDI_ROCK_CLIMB
    MEIYERDITCH_LAB_ADVANCED_TUNNELS_WEST
    MEIYERDITCH_LAB_ADVANCED_TUNNELS_MIDDLE
    MEIYERDITCH_LAB_ADVANCED_TUNNELS_EAST
    VIYELDI_CAVES_CREVICE

AgilityShortcutTest > everyEnumHasTsvEntry FAILED
    java.lang.AssertionError: Missing TSV entries for AgilityShortcut enum constants (see stdout for list)
        at org.junit.Assert.fail(Assert.java:89)
        at shortestpath.AgilityShortcutTest.everyEnumHasTsvEntry(AgilityShortcutTest.java:68)

PathfinderTest > testTeleportationLevers STANDARD_OUT
    Successfully completed 7 TELEPORTATION_LEVER transport length tests

PathfinderTest > testTeleportationPortals STANDARD_OUT
    Successfully completed 104 TELEPORTATION_PORTAL transport length tests

PathfinderTest > testImpossibleCharterShips STANDARD_OUT
    Successfully completed transport length test from (1455, 2968, 0) to (1514, 2971, 0) with actual length = 3 >= minimum length = 3
    Successfully completed transport length test from (1514, 2971, 0) to (1455, 2968, 0) with actual length = 3 >= minimum length = 3
    Successfully completed transport length test from (3702, 3503, 0) to (3671, 2931, 0) with actual length = 3 >= minimum length = 3
    Successfully completed transport length test from (3671, 2931, 0) to (3702, 3503, 0) with actual length = 3 >= minimum length = 3
    Successfully completed transport length test from (1808, 3679, 0) to (1496, 3403, 0) with actual length = 3 >= minimum length = 3
    Successfully completed transport length test from (1496, 3403, 0) to (1808, 3679, 0) with actual length = 3 >= minimum length = 3
    Successfully completed transport length test from (3038, 3192, 0) to (1496, 3403, 0) with actual length = 3 >= minimum length = 3
    Successfully completed transport length test from (1496, 3403, 0) to (3038, 3192, 0) with actual length = 3 >= minimum length = 3
    Successfully completed transport length test from (3038, 3192, 0) to (2954, 3158, 0) with actual length = 3 >= minimum length = 3
    Successfully completed transport length test from (2954, 3158, 0) to (3038, 3192, 0) with actual length = 3 >= minimum length = 3
    Successfully completed transport length test from (3038, 3192, 0) to (1808, 3679, 0) with actual length = 3 >= minimum length = 3
    Successfully completed transport length test from (1808, 3679, 0) to (3038, 3192, 0) with actual length = 3 >= minimum length = 3

PathfinderTest > testBoats STANDARD_OUT
    Successfully completed 111 BOAT transport length tests

PathfinderTest > testCaves STANDARD_OUT
    Successfully completed transport length test from (2892, 3671, 0) to (2893, 10074, 2)
    Successfully completed transport length test from (2893, 3671, 0) to (2893, 10074, 2)
    Successfully completed transport length test from (2894, 3671, 0) to (2893, 10074, 2)
    Successfully completed transport length test from (2895, 3672, 0) to (2893, 10074, 2)
    Successfully completed transport length test from (2892, 10074, 2) to (2893, 3671, 0)
    Successfully completed transport length test from (2893, 10074, 2) to (2893, 3671, 0)
    Successfully completed transport length test from (2894, 10074, 2) to (2893, 3671, 0)

PathfinderTest > testMagicCarpets STANDARD_OUT
    Successfully completed 13 MAGIC_CARPET transport length tests

PathfinderTest > testShips STANDARD_OUT
    Successfully completed 31 SHIP transport length tests

PathfinderTest > testAgilityShortcuts STANDARD_OUT
    Successfully completed 552 AGILITY_SHORTCUT transport length tests

PathfinderTest > testGnomeGliders STANDARD_OUT
    Successfully completed 90 GNOME_GLIDER transport length tests

PathfinderTest > testPathViaOtherPlane STANDARD_OUT
    Successfully completed transport length test from (2894, 10199, 0) to (2864, 10199, 0)
    Successfully completed transport length test from (2864, 10199, 0) to (2894, 10199, 0)

PathfinderTest > testFairyRingsUsedWithDramenStaffWornInHand FAILED
    java.lang.AssertionError: (2027, 5700, 0) to (2744, 3719, 0) expected:<2> but was:<272>
        at org.junit.Assert.fail(Assert.java:89)
        at org.junit.Assert.failNotEquals(Assert.java:835)
        at org.junit.Assert.assertEquals(Assert.java:647)
        at shortestpath.pathfinder.PathfinderTest.testTransportLength(PathfinderTest.java:548)
        at shortestpath.pathfinder.PathfinderTest.testTransportLength(PathfinderTest.java:536)
        at shortestpath.pathfinder.PathfinderTest.testFairyRingsUsedWithDramenStaffWornInHand(PathfinderTest.java:161)

PathfinderTest > testFairyRingsUsedWithLumbridgeDiaryCompleteWithoutDramenStaff FAILED
    java.lang.AssertionError: (2027, 5700, 0) to (2744, 3719, 0) expected:<2> but was:<272>
        at org.junit.Assert.fail(Assert.java:89)
        at org.junit.Assert.failNotEquals(Assert.java:835)
        at org.junit.Assert.assertEquals(Assert.java:647)
        at shortestpath.pathfinder.PathfinderTest.testTransportLength(PathfinderTest.java:548)
        at shortestpath.pathfinder.PathfinderTest.testTransportLength(PathfinderTest.java:536)
        at shortestpath.pathfinder.PathfinderTest.testFairyRingsUsedWithLumbridgeDiaryCompleteWithoutDramenStaff(PathfinderTest.java:150)

PathfinderTest > testQuetzals STANDARD_OUT
    Successfully completed 184 QUETZAL transport length tests

PathfinderTest > testTeleportationMinigames STANDARD_OUT
    Successfully completed transport length test from (3440, 3334, 0) to (2658, 3157, 0)
    Successfully completed transport length test from (3136, 3525, 0) to (2658, 3157, 0)

PathfinderTest > testHotAirBalloons STANDARD_OUT
    Successfully completed 225 HOT_AIR_BALLOON transport length tests

PathfinderTest > testCharterShips STANDARD_OUT
    Successfully completed 228 CHARTER_SHIP transport length tests

PathfinderTest > testGrappleShortcuts STANDARD_OUT
    Successfully completed 14 GRAPPLE_SHORTCUT transport length tests

PathfinderTest > testChronicle STANDARD_OUT
    Successfully completed transport length test from (3199, 3336, 0) to (3200, 3355, 0)

PathfinderTest > testWildernessObelisks FAILED
    java.lang.AssertionError: (1888, 5717, 0) to (3035, 3732, 0) expected:<2> but was:<176>
        at org.junit.Assert.fail(Assert.java:89)
        at org.junit.Assert.failNotEquals(Assert.java:835)
        at org.junit.Assert.assertEquals(Assert.java:647)
        at shortestpath.pathfinder.PathfinderTest.testTransportLength(PathfinderTest.java:548)
        at shortestpath.pathfinder.PathfinderTest.testTransportLength(PathfinderTest.java:536)
        at shortestpath.pathfinder.PathfinderTest.testWildernessObelisks(PathfinderTest.java:299)

PathfinderTest > testAgilityShortcutAndTeleportItem STANDARD_OUT
    Successfully completed transport length test from (3149, 3363, 0) to (3154, 3363, 0)

PathfinderTest > testSpiritTrees FAILED
    java.lang.AssertionError: (2007, 5700, 0) to (2339, 3109, 0) expected:<2> but was:<217>
        at org.junit.Assert.fail(Assert.java:89)
        at org.junit.Assert.failNotEquals(Assert.java:835)
        at org.junit.Assert.assertEquals(Assert.java:647)
        at shortestpath.pathfinder.PathfinderTest.testTransportLength(PathfinderTest.java:548)
        at shortestpath.pathfinder.PathfinderTest.testTransportLength(PathfinderTest.java:536)
        at shortestpath.pathfinder.PathfinderTest.testSpiritTrees(PathfinderTest.java:213)

PathfinderTest > testTeleportationBoxes FAILED
    java.lang.AssertionError: (1886, 5760, 0) to (1579, 3530, 0) expected:<2> but was:<191>
        at org.junit.Assert.fail(Assert.java:89)
        at org.junit.Assert.failNotEquals(Assert.java:835)
        at org.junit.Assert.assertEquals(Assert.java:647)
        at shortestpath.pathfinder.PathfinderTest.testTransportLength(PathfinderTest.java:548)
        at shortestpath.pathfinder.PathfinderTest.testTransportLength(PathfinderTest.java:536)
        at shortestpath.pathfinder.PathfinderTest.testTeleportationBoxes(PathfinderTest.java:221)

PathfinderTest > testCanoes STANDARD_OUT
    Successfully completed 25 CANOE transport length tests

PathfinderTest > testVarrockTeleport STANDARD_OUT
    Successfully completed transport length test from (3216, 3424, 0) to (3213, 3424, 0)
    Successfully completed transport length test from (3216, 3424, 0) to (3213, 3424, 0)

PathfinderTest > testMinecarts STANDARD_OUT
    Successfully completed 255 MINECART transport length tests

PathfinderTest > testFairyRings FAILED
    java.lang.AssertionError: (2027, 5700, 0) to (2744, 3719, 0) expected:<2> but was:<272>
        at org.junit.Assert.fail(Assert.java:89)
        at org.junit.Assert.failNotEquals(Assert.java:835)
        at org.junit.Assert.assertEquals(Assert.java:647)
        at shortestpath.pathfinder.PathfinderTest.testTransportLength(PathfinderTest.java:548)
        at shortestpath.pathfinder.PathfinderTest.testTransportLength(PathfinderTest.java:536)
        at shortestpath.pathfinder.PathfinderTest.testFairyRings(PathfinderTest.java:119)

PathfinderTest > testMagicMushtrees STANDARD_OUT
    Successfully completed 24 MAGIC_MUSHTREE transport length tests

TransportDataLintTest > testNoDuplicateOriginDestinationPairs FAILED
    java.lang.AssertionError: Found 4 duplicate transport entries in the data files:

    Exact duplicate transport: 1647 10010 0 -> 1645 10000 0 [AGILITY_SHORTCUT] '' Consumable:false Items:none Quests:[] Varbits:[] VarPlayers:[] ObjectInfo:Squeeze-through Crack 28892
    First occurrence: MaxWilderness: -1, Skills: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    Duplicate occurrence: MaxWilderness: -1, Skills: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]

    Exact duplicate transport: 1645 10000 0 -> 1647 10010 0 [AGILITY_SHORTCUT] '' Consumable:false Items:none Quests:[] Varbits:[] VarPlayers:[] ObjectInfo:Squeeze-through Crack 28892
    First occurrence: MaxWilderness: -1, Skills: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    Duplicate occurrence: MaxWilderness: -1, Skills: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]

    Exact duplicate transport: 1613 10070 0 -> 1609 10061 0 [AGILITY_SHORTCUT] '' Consumable:false Items:none Quests:[] Varbits:[] VarPlayers:[] ObjectInfo:Jump-to Stone 28893
    First occurrence: MaxWilderness: -1, Skills: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    Duplicate occurrence: MaxWilderness: -1, Skills: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]

    Exact duplicate transport: 1609 10061 0 -> 1613 10070 0 [AGILITY_SHORTCUT] '' Consumable:false Items:none Quests:[] Varbits:[] VarPlayers:[] ObjectInfo:Jump-to Stone 28893
    First occurrence: MaxWilderness: -1, Skills: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    Duplicate occurrence: MaxWilderness: -1, Skills: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        at org.junit.Assert.fail(Assert.java:89)
        at shortestpath.transport.TransportDataLintTest.testNoDuplicateOriginDestinationPairs(TransportDataLintTest.java:98)

TransportLoaderTest > testMalformedData STANDARD_OUT
    2026-02-13 22:18:55 CET [Test worker] ERROR shortestpath.transport.Transport - Invalid level and skill: invalid skill
    2026-02-13 22:18:55 CET [Test worker] ERROR shortestpath.transport.Transport - Invalid item or quantity: INVALID=ITEMS
    2026-02-13 22:18:55 CET [Test worker] ERROR shortestpath.transport.Transport - Invalid tick duration: invalid_duration
    2026-02-13 22:18:55 CET [Test worker] ERROR shortestpath.transport.Transport - Invalid wilderness level: invalid_wilderness

Gradle Test Executor 3 finished executing tests.

> Task :test FAILED
Finished generating test XML results (0.008 secs) into: C:\Users\woute\Code\fork\shortest-path\build\test-results\test
Generating HTML test report...
Finished generating test html results (0.013 secs) into: C:\Users\woute\Code\fork\shortest-path\build\reports\tests\test

Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/8.10/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
4 actionable tasks: 3 executed, 1 up-to-date
Watched directory hierarchies: [C:\Users\woute\Code\fork\shortest-path]
