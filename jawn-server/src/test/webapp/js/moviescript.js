$.fn.editable.defaults.ajaxOptions = {type: "PUT"};
$(document).ready(function() {
    $('.movie-name').editable();
    $('.movie-year').editable();
});