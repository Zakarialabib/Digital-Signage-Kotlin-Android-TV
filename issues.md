Executing tasks: [:app:assembleDebug] in project /home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV

> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:checkKotlinGradlePluginConfigurationErrors
> Task :app:generateDebugBuildConfig UP-TO-DATE
> Task :app:checkDebugAarMetadata UP-TO-DATE
> Task :app:generateDebugResValues UP-TO-DATE
> Task :app:processDebugGoogleServices UP-TO-DATE
> Task :app:mapDebugSourceSetPaths UP-TO-DATE
> Task :app:generateDebugResources UP-TO-DATE
> Task :app:mergeDebugResources UP-TO-DATE
> Task :app:packageDebugResources UP-TO-DATE
> Task :app:parseDebugLocalResources UP-TO-DATE
> Task :app:createDebugCompatibleScreenManifests UP-TO-DATE
> Task :app:extractDeepLinksDebug UP-TO-DATE
> Task :app:processDebugMainManifest UP-TO-DATE
> Task :app:processDebugManifest UP-TO-DATE
> Task :app:processDebugManifestForPackage UP-TO-DATE
> Task :app:processDebugResources UP-TO-DATE
> Task :app:javaPreCompileDebug UP-TO-DATE
> Task :app:mergeDebugShaders UP-TO-DATE
> Task :app:compileDebugShaders NO-SOURCE
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:mergeDebugAssets UP-TO-DATE
> Task :app:compressDebugAssets UP-TO-DATE
> Task :app:desugarDebugFileDependencies UP-TO-DATE
> Task :app:checkDebugDuplicateClasses UP-TO-DATE
> Task :app:mergeExtDexDebug UP-TO-DATE
> Task :app:mergeLibDexDebug UP-TO-DATE
> Task :app:mergeDebugJniLibFolders UP-TO-DATE
> Task :app:mergeDebugNativeLibs NO-SOURCE
> Task :app:stripDebugDebugSymbols NO-SOURCE
> Task :app:validateSigningDebug UP-TO-DATE
> Task :app:writeDebugAppMetadata UP-TO-DATE
> Task :app:writeDebugSigningConfigVersions UP-TO-DATE
> Task :app:kspDebugKotlin

> Task :app:compileDebugKotlin FAILED
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:9:12 Redeclaration: Content
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:43:14 Redeclaration: Content
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:48:5 Serializable class has duplicate serial name of property 'id', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:48:5 Serializable class has duplicate serial name of property 'url', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:48:5 Serializable class has duplicate serial name of property 'duration', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:48:5 Serializable class has duplicate serial name of property 'type', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:49:5 Function 'component1' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:49:5 Function 'component2' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:49:5 Function 'component3' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:49:5 Function 'component4' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:50:9 'id' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:51:13 'url' hides member of supertype 'Content' and needs 'override' modifier
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:53:9 'duration' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:54:16 No value passed for parameter 'id'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:54:16 No value passed for parameter 'name'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:54:16 No value passed for parameter 'type'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:55:9 'type' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:58:5 Serializable class has duplicate serial name of property 'id', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:58:5 Serializable class has duplicate serial name of property 'url', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:58:5 Serializable class has duplicate serial name of property 'duration', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:58:5 Serializable class has duplicate serial name of property 'type', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:59:5 Function 'component1' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:59:5 Function 'component2' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:59:5 Function 'component3' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:59:5 Function 'component4' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:59:5 Function 'component5' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:59:5 Function 'component6' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:60:9 'id' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:61:13 'url' hides member of supertype 'Content' and needs 'override' modifier
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:65:9 'duration' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:66:16 No value passed for parameter 'id'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:66:16 No value passed for parameter 'name'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:66:16 No value passed for parameter 'type'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:67:9 'type' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:70:5 Serializable class has duplicate serial name of property 'id', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:70:5 Serializable class has duplicate serial name of property 'url', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:70:5 Serializable class has duplicate serial name of property 'duration', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:70:5 Serializable class has duplicate serial name of property 'type', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:71:5 Function 'component1' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:71:5 Function 'component2' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:71:5 Function 'component3' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:71:5 Function 'component4' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:72:9 'id' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:73:13 'url' hides member of supertype 'Content' and needs 'override' modifier
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:75:9 'duration' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:76:16 No value passed for parameter 'id'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:76:16 No value passed for parameter 'name'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:76:16 No value passed for parameter 'type'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:77:9 'type' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:77:54 Unresolved reference: HTML
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:80:5 Serializable class has duplicate serial name of property 'id', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:80:5 Serializable class has duplicate serial name of property 'duration', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:80:5 Serializable class has duplicate serial name of property 'type', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:81:5 Function 'component1' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:81:5 Function 'component2' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:81:5 Function 'component3' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:81:5 Function 'component4' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:81:5 Function 'component5' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:82:9 'id' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:86:9 'duration' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:87:16 No value passed for parameter 'id'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:87:16 No value passed for parameter 'name'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:87:16 No value passed for parameter 'type'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:88:9 'type' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:88:54 Unresolved reference: CAROUSEL
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:91:5 Serializable class has duplicate serial name of property 'id', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:91:5 Serializable class has duplicate serial name of property 'url', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:91:5 Serializable class has duplicate serial name of property 'duration', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:91:5 Serializable class has duplicate serial name of property 'type', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:92:5 Function 'component1' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:92:5 Function 'component2' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:92:5 Function 'component3' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:93:9 'id' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:94:13 'url' hides member of supertype 'Content' and needs 'override' modifier
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:95:9 'duration' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:96:16 No value passed for parameter 'id'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:96:16 No value passed for parameter 'name'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:96:16 No value passed for parameter 'type'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:97:9 'type' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:97:54 Unresolved reference: WEBPAGE
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:100:5 Serializable class has duplicate serial name of property 'id', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:100:5 Serializable class has duplicate serial name of property 'duration', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:100:5 Serializable class has duplicate serial name of property 'type', either in the class itself or its supertypes
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:101:5 Function 'component1' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:101:5 Function 'component2' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:101:5 Function 'component3' generated for the data class conflicts with member of supertype 'Content'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:102:9 'id' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:104:9 'duration' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:105:16 No value passed for parameter 'id'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:105:16 No value passed for parameter 'name'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:105:16 No value passed for parameter 'type'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:106:9 'type' in 'Content' is final and cannot be overridden
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/model/Content.kt:106:54 Unresolved reference: PLAYLIST
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/repository/ContentRepository.kt:9:51 Unresolved reference: Playlist
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/repository/ContentRepositoryImpl.kt:15:51 Unresolved reference: Playlist
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/repository/ContentRepositoryImpl.kt:215:34 Unresolved reference: Image
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/repository/ContentRepositoryImpl.kt:216:34 Unresolved reference: Video
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/repository/ContentRepositoryImpl.kt:220:13 Cannot find a parameter with this name: duration
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/repository/ContentRepositoryImpl.kt:220:13 No value passed for parameter 'name'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/repository/ContentRepositoryImpl.kt:220:13 No value passed for parameter 'description'
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/core/data/repository/ContentRepositoryImpl.kt:228:36 Unresolved reference: Image
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/features/display/renderers/HtmlRenderer.kt:18:26 Unresolved reference: Html
e: file:///home/user/StudioProjects/Digital-Signage-Kotlin-Android-TV/app/src/main/java/com/signagepro/app/features/display/renderers/WebPageRenderer.kt:20:29 Unresolved reference: WebPage

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileDebugKotlin'.
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction
   > Compilation error. See log for more details

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 11s
30 actionable tasks: 3 executed, 27 up-to-date
