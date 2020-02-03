/**
 * Analytics module
 */

// Global Google Analytics var
var _gaq = _gaq || [];

(function (window, angular, undefined) {
    'use strict';

    /**
     * Analytics Module
     */
    angular.module('Analytics', [])
        .run(['$http', '$window', function($http, $window) {

            if(window.Conf && window.Conf.gaID){
                // Set Google Analytics config vars
                // GA PROD - UA-38725756-1
                // GA DEV - UA-38725756-2
                _gaq.push(['_setAccount', $window.Conf.gaID]);
                _gaq.push(['_setDomainName', 'prepado.com']);
                _gaq.push(['_trackPageview']);

                // Add Google Analytics to page
                var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
                ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
                var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
            }

        }])
        .service('analytics', ['$rootScope', '$window', '$location', '$routeParams', function($rootScope, $window, $location, $routeParams) {

            this.load = function(name) {
                //console.log('GA Loaded');

                var track = function() {
                    var path = convertPathToQueryString($location.path(), $routeParams);

                    if(window.Conf && window.Conf.gaID){
                        //console.log('GA path: '+path);
                        $window._gaq.push(['_trackPageview', path]);
                    }
                };

                var convertPathToQueryString = function(path, $routeParams) {
                    for (var key in $routeParams) {
                        var queryParam = '/' + $routeParams[key];
                        path = path.replace(queryParam, '');
                    }

                    var querystring = decodeURIComponent($.param($routeParams));

                    if (querystring === '') return path;

                    return path + "?" + querystring;
                };

                $rootScope.$on('$viewContentLoaded', track);

            };

        }])

    ;

})(window, window.angular);