

# How I authored the doc

I studied the following article to learn how to author the document using Asciidoc with Gradle.

- [AsciidoctorとGradleでつくる文書執筆環境](https://h1romas4.github.io/asciidoctor-gradle-template/index.html)

This articles provides a good sample project to get started with Asciidoctor. But the sample project
has a problem : it contains 3 sets of Fonts, which has 240 MB size. It is too large to include in source code project. So, I decided to write in the `.gitignore` file:

```
src/docs/asciidoc/@font/
```

And I will manually download the Fonts from:
- https://ja.osdn.net/downloads/users/8/8579/genshingothic-20150607.zip
- https://github.com/ButTaiwan/genyo-font/releases/download/v1.501/GenYoMin.zip
- https://github.com/edihbrandon/RictyDiminished/archive/refs/tags/3.2.3.zip
- 