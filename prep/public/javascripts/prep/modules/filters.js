(function (window, app, undefined) {
    'use strict';

    /**
     *  Formats dates without performing any conversion from UTC to the local timezone.  Useful for redisplaying
     *  user-entered values such as graduation date or date of birth.  If no format is specified dates are formatted as MM/DD/YYYY
     */
    app.filter('utcDateFormat', function(){
        return function(string, format){
            var outputFormat = format || 'MM/DD/YYYY';

            //if the string already appears to be properly formatted then
            //just return it
            if(string && string.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/)){
                return string;
            }

            return string ? moment.utc(string).format(outputFormat) : '';
        }
    })

    /**
     *  Formats dates and converts given dates from UTC to the local timezone.  Useful for displaying system generated
     *  dates like created or updated dates relative to the User's current time zone.
     */
    .filter('localDateFormat', function(){
        return function(string, format){
            var outputFormat = format || 'MM/DD/YYYY';

            return string ? moment(string).format(outputFormat) : '';
        }
    })

    ;

})(window, window.app);