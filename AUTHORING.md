# Authoroing this document

## Asciidoc

I used https://asciidoc.org/[Asciidoc] to author the doc. I studied the following article to learn how to author the document using Asciidoc with Gradle.

- https://h1romas4.github.io/asciidoctor-gradle-template/index.html[AsciidoctorとGradleでつくる文書執筆環境]

I was inspired by the following thread and got the `readmeconv.sh` and `docs/indexconv.sh` files:

- https://github.com/github/markup/issues/1095

## ./README

edit `./README_.adoc` file, then run the following command:

```
$ cd $materialstore-tutorial
$ ./readmeconv.sh
$ git add .
$ git commit -m "some comment"
$ git push
```

## docs/index

edit `docs/index_.adoc` file, then run the following command:

```
$ cd $materialstore-tutorial/docs
$ ./indexconv.sh -t
$ cd ..
$ git add .
$ git commit -m "some comment"
$ git push
```

