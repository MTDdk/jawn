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
            <h1>"jawn" 
                <small>Harness the drowsiness</small>
                <a class="btn btn-primary btn-lg pull-right" href="http://javapla.net" target="_blank" role="button">Read docs</a>
            </h1>
        </div>
        
        <ul class="nav nav-tabs" id="navigation" style="margin-bottom:10px;">
            <li role="presentation" id="navigation_index" class="active"><a href="/">Index</a></li>
            <li role="presentation" id="navigation_movie"><a href="/movie">Movie</a></li>
            <li role="presentation" id="navigation_some"><a href="/some">Some</a></li>
        </ul>

        $site.content$
    </div>

    $site.scripts$
</body>
</html>
