---
layout: default
title: WEB
parent: Matcher Packaging
nav_order: 3
permalink: /matcher-packaging/web
---
# Web Packaging

The web packaging requires the matcher to provide an HTTP endpoint.
This can be achieved with a docker container or any other server.

There are two possible `content types` (below: drop-down menu for "Request body"): (1) An URL encoded form and (2) multipart upload. For the first option, URLs are provided pointing to the data files. In this case, the matching server will access the URL and download the data. For the second option, the files are directly contained in the HTTP POST request. 

`source` and  `target` are plain RDF files (RDF/XML, Turtle, N3). 

`inputAlignment` is an alignment in the [alignment format](https://moex.gitlabpages.inria.fr/alignapi/format.html).

`parameters` are used to provide task-specific information (for example "match only classes"). Some keys are already defined by [MELT](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/ParameterConfigKeys.java). The parameters can be transferred as JSON (preferred) or YAML.

The system directly returns the resulting alignment in the [alignment format](https://moex.gitlabpages.inria.fr/alignapi/format.html).

Interactive matching is not supported.



## Swagger documentation of the Web API
melt/matcher-packaging/
[Full screen documentation in a new tab](/melt/6_matcher_packaging/swagger_ui_melt.html){:target="_blank"}

<iframe src="/melt/6_matcher_packaging/swagger_ui_melt.html" scrolling="no"
    style="border:0; width:100%; height:3000px; overflow:hidden;">