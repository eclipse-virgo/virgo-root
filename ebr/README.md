### Publishing 3rd party bundles

Please note this workaround when uploading spring-test:

    ./gradlew clean org.springframework.test-4.2.9.RELEASE:rewriteJar
    ./gradlew -x bundlor org.springframework.test-4.2.9.RELEASE:publish
    ./gradlew -x bundlor -x publish org.springframework.test-4.2.9.RELEASE:upload

Otherwise the upload will contain empty files.
