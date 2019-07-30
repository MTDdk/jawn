<!DOCTYPE html>
<html>

<head>
  <title>$site.title$</title>
  
  <meta name="viewport" content="width=device-width, initial-scale=1">
  
  <link rel="shortcut icon" href="/favicon.ico">
  <link rel="icon" href="/favicon.ico">
  
  $site.styles$

</head>

<body>
  <div class="container">
    <div class="page-header">
      <h1>
        "jawn" <small>Harness the drowsiness</small> <a class="btn btn-primary btn-lg pull-right" href="http://javapla.net" target="_blank" role="button">Read docs</a>
      </h1>
    </div>

    <ul class="nav nav-tabs" id="navigation" style="margin-bottom: 10px;">
      <li role="presentation" id="navigation_index" class="active"><a href="/">Index</a></li>
      <li role="presentation" id="navigation_redirect"><a href="/redirect">Redirect</a></li>
      <li role="presentation" id="navigation_movie"><a href="/movie">Movie</a></li>
      <li role="presentation" id="navigation_cache"><a href="/cache">Cache</a></li>
      <li role="presentation" id="navigation_url"><a href="/url">Url</a></li>
      <li role="presentation" id="navigation_upload"><a href="/upload">Upload</a></li>
      <li role="presentation" id="navigation_cookie"><a href="/cookie">Cookie</a></li>
      <li role="presentation" id="navigation_misc"><a href="/misc">Misc.</a></li>
    </ul>

    $site.content$
  </div>

  $site.scripts$
</body>
</html>
