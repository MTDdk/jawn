# Views
**Layouts** contains one or several **templates**. 


## Renderer / template engine
Using [StringTemplate](http://www.stringtemplate.org/) as default.

The framework supports some configuration of the template engine, like [adaptors](https://theantlrguy.atlassian.net/wiki/display/ST4/Model+adaptors) and [renderers](https://theantlrguy.atlassian.net/wiki/display/ST4/Renderers). 

The original usage of [FreeMarker](http://freemarker.org/) is still supported, however.

> How to switch between template engines needs to be documented.



## Templates
`index.st` = standard naming convention of templates.

Templates can be nested.
> Some example

For more information, refer to the [template guide](https://theantlrguy.atlassian.net/wiki/display/ST4/Templates) of StringTemplate.



## Layouts
Generally used to outline the structure of the HTML generated as a result of any controller action.


`index.html.st` = default layout. This is located in the root of `views` folder
and base for all controller templates to generate HTML.

Is the `index.html.st` layout present within a view subfolder as illustrated in the [example structure](structure_of_jawn_project.md), then this layout overrides the default layout for that view.
Like so:
```
webapp/views/WEB-INF/some/index.html.st
```

A layout should always contain a `$content$` where the controller template will be injected.