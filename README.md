# materialstore-examples

## Overview

This project publishes a documentation of the [`materialstore`](https://github.com/kazurayam/materialstore) library with examples. Examples are written in Java >ver8. Examples uses [Gradle](https://gradle.org/) and [JUnit5](https://junit.org/junit5/).

## URL of documentation

- https://kazurayam.github.com/materialstore-example/

## How to publish the doc

In the command line, execute the following command

```
$ cd $materialstore-examples
$ gradle -b docs-build.gradle
```

then do `git add`, `git commit`, and `git publish`.

## How I authored the doc

I used [Asciidoc](https://asciidoc.org/) to author the doc. I studied the following article to learn how to author the document using Asciidoc with Gradle.

- [AsciidoctorとGradleでつくる文書執筆環境](https://h1romas4.github.io/asciidoctor-gradle-template/index.html)
