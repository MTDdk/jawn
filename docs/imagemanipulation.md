# Image manipulation

Integrated into **jawn** is the ability to do some simple image manipulations.

In a *controller*, the following methods are available:

```java
/**
 * Used when the file is from a upload by form.
 */
image(FormItem)

/**
 * If you already have the reference to a file on the server.
 */
image(File)

/**
 * If you have the path to a file on the server.
 * Could be from a request parameter.
 */
image(String)
``` 

Once wrapped you get access to a builder with the possibility of chaining commands:
```java
String pathToThumb = image(serverPath)
								.crop(x,y,w,h)
								.resize(desiredWidth, desiredHeight)
								.append("_thumb")
								.save();
```

## Resize
* `resize(width, height)` - Resizes to the exact measures stated, without considering the original proportions
* `resizeToHeight(height)` - Resize to a defined height, maintaining its original proportions
* `resizeToWidth(width)` - Resize to a defined width, maintaining the images original proportions


## Crop
* `crop(x, y, width, height)` - Crops the image. If the stated width/height goes out of the bounds of the image, the function automatically snaps to the edge


## Rename
* `name(string)` - Change the original name of the image. When a file is uploaded with FormItem, the file name is preserved, so this function could be used to define a common naming policy. _This function also updates the  **file extension**, so be sure to include that_.
* `append(string)` - Appends a string to the name of the file. Does *not* affect the file extension.


## Save
All images are always saved in `Configuration.imageUploadFolder`, which by default is: `webapp/WEB-INF/uploads/images`. If another folder is wanted for upload, this can be changed in the *Configuration*.

* `folder(string)` - State a folder the file should be saved in. The stated folder will be put inside the `imageUploadFolder`.
* `save()` - Write the file onto disk. If a file already is present with the exact same name, a versioning scheme will be applied, that appends *_1v*, *_2v* and so on.


## Send / respond
* `send()` - Respond the image directly to the browser with the content type set appropriately for the type of image.