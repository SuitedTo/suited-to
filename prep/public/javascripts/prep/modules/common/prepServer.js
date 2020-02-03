/**
 * The PrepServer module represents the lowest layer available. This layer
 * directly reflects the restful prep services. This module should not be used
 * directly, use the PrepAPI module.
 */

(function (window, angular, undefined) {
    'use strict';

    function isErrorResponse(obj){
        return obj && obj.hasOwnProperty('error');
    }

    angular.module('PrepServer', ['Utils'])

        /**
         * Resources
         */
        .factory('Resources', ['$resource', function ($resource) {

            var customActions = {update:{method:'PUT'}};

            var _resources = {
                user: $resource('/user/:id', {id:'@id'}, customActions),
                session: $resource('/session/:id', {id:'@id'}, customActions),
                interview: $resource('/interview/:id', {id:'@id'}, customActions),
                'reviewer-interview': $resource('/interview/:id', {id:'@id'},
                    {get:{method:'GET', params: {reviewKey: '@reviewKey'}}, update:{method:'PUT', params: {reviewKey: '@reviewKey'}}}),
                'interview-review': $resource('/interview/:id/review/:reviewKey', {id:'@id', reviewKey:'@reviewKey'}, customActions),
                questionreview: $resource('/questionreview/:id?reviewKey=:reviewKey', {id:'@id'},
                    {update:{method:'PUT', params: {reviewKey: '@reviewKey'}}, save:{method:'POST', params: {reviewKey: '@reviewKey'}}}),
                interviewbuild: $resource('/interviewbuild/:id', {id:'@id'}, customActions),
                interviewcategorylistbuild: $resource('/interviewcategorylistbuild/:id'),
                interviewcategorylist: $resource('/interviewcategorylist/:id', {id:'@id'}, customActions),
                jobmatch: $resource('/jobmatch/', {id:'@id'}, customActions),
                question: $resource('/question/:id', {id:'@id'}, customActions),
                questionVideo: $resource('/question/:id/video', {id:'@id'}, customActions),
                category: $resource('/category/:id', {id:'@id'}, customActions),
                discount: $resource('/discount/:id', {id:'@id'}, customActions),
                coupon: $resource('/coupon/:name', {name:'@name'}, customActions),
                card: $resource('/card/:id', {id:'@id'}, customActions),
                subscription: $resource('/subscription/:id', {id:'@id'}, customActions),
                forgotpasswordrequest: $resource('/forgotpasswordrequest/', {} , customActions),
                forgotpassword: $resource('/forgotpassword/', {}, customActions)
            };

            /**
             * Callback wrapper that prevents HTTP Error responses from being processed by controller error callbacks
             */
            function errorCallbackWrapper (resource) {
                function isAppError (response) {
                    return response.status == 200;
                }

                function wrapResourceFunc (resource, func) {
                    var originalFunc = resource[func];

                    resource[func] = function (data, successCallback, errorCallback) {
                        originalFunc(
                            data,
                            function (response) {
                                if (successCallback) {
                                    successCallback (response);
                                }
                            },
                            function (response) {
                                if (errorCallback && isAppError(response)) {
                                    errorCallback(response);
                                }
                            }
                        )
                    };

                    return resource;
                }

                for (var func in resource.prototype) {
                    resource = wrapResourceFunc(resource, func.split('$')[1]);
                }

                return resource;
            }

            return {
                get: function (key) {
                    return errorCallbackWrapper(_resources[key]);
                }
            };
        }])

        .service('responseInterceptor', ['$rootScope', '$log', '$q', '$location', function ($rootScope, $log, $q, $location) {
            this.successInterceptor = function (response) {

                //$log.info('HTTP OK: ' + response.data);
                if (isErrorResponse(response.data)) {
                    var errors = response.data.error;
                    for (var i = 0; i < errors.length; i++) {
                        $log.error(errors[i].type + ': ' + errors[i].message);
                    }

                    errors.status = response.status; // need to retain some consistency so we can check status of both success and errors later
                    return $q.reject(errors);
                }
                return response;
            };
            this.errorInterceptor = function (response) {
                var status = response.status;
                if (status == 401) {
                    $rootScope.global.connectedUser = null;
                    $location.url('/login?req='+$location.path());
                    if(!$rootScope.$$phase) {
                        $rootScope.$apply();
                    }
                }

                $log.error('HTTP ' + status);
                $rootScope.updateHttpStatus(status, response.data);

                return $q.reject({'status': status, 'message': 'HTTP Error'});
            };
        }])

        /**
         * Http Interceptor
         */
        .factory('httpInterceptor', ['responseInterceptor', function (responseInterceptor) {
            return function (promise) {
                return promise.then(responseInterceptor.successInterceptor, responseInterceptor.errorInterceptor);
            };
        }])


        /**
         * Module Config
         */
        .config(['$httpProvider', function($httpProvider){
            $httpProvider.responseInterceptors.push('httpInterceptor');
        }])

        ;

})(window, window.angular);
