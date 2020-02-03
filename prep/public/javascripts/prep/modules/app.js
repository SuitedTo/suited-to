/**
 * App
 */

(function (window, angular, undefined) {
    'use strict';

    function setConnectedUser(scope, init){
        var user = {};

        if (init){
            user.authenticated = window.Conf.authenticated_userid;
            user.id = window.Conf.authenticated_userid;
            user.roles = window.Conf.roles;
            user.firstName = null;
            user.email = null;
            user.paymentRequired = null;
            user.subscriptionType = null;
        } else {
            user.authenticated = window.Conf['authenticated_userid'] != null;
            user.id = window.Conf['authenticated_userid'];
            user.roles = window.Conf['userRoles'];
            user.firstName = window.Conf['userName'];
            user.email = window.Conf['userEmail'];
            user.paymentRequired = window.Conf['userPaymentRequired'];
            user.subscriptionType = window.Conf['userSubscriptionType'];
        }
        scope.global.connectedUser = user;
    }

    angular.module('App', ['Common', '$strap.directives'])

        /**
         * App Module Config
         */
        .config(['$httpProvider', function ($httpProvider) {

            $httpProvider.defaults.headers.common['X-CSRF-Token'] = window.Conf.csrf_token;

            // Adding header value directly into typeahead.js
            // $.ajaxSetup({
            //    //headers: {'X-CSRF-Token': window.Conf.csrf_token}
            //});
        }])


        /**
         * App Run
         */
        .run(['$rootScope', '$log', '$location', 'analytics', function(scope, log, $location, analytics) {



            scope.error = {};

            scope.setError = function(error){
                this.error[error] = true;
            };
            scope.clearError = function(error){
                this.error[error] = false;
            };

            scope.clearErrors = function(){
                this.error = {};
            };

            scope.updateHttpStatus = function(status, responseData){
                switch(status)
                {
                    case 401:
                        scope.httpStatus = "You're not authorized to access the requested resource. Please log in.";
                        break;
                    case 403:
                        scope.httpStatus = "Access to the requested resource has been denied.";
                        break;
                    case 404:
                        scope.httpStatus = "The requested resource was not found.";
                        break;
                    default:
                    	var errStr = 'this exception has been logged with id ';
                    	var pos = responseData.toLowerCase().indexOf(errStr);
                    	var suffix = 'The error has been logged with id ';
                    	if(pos != -1){
                    		suffix += responseData.toLowerCase().split(errStr)[1] + '.';
                    	} else {
                    	    suffix = 'Please refresh the page.'
                    	}
                        scope.httpStatus = "We've encountered a problem. " + suffix;
                }

            };

            var guestRoles = {

                reviewer: {keyName: 'reviewerKey'}

            }

            /*
                 Basically just matching the name of the key to a role. The validity of the key's value
                 needs to be checked server side.
             */
            function checkGuestAccess(route){
                var guest = establishedGuest();
                if(guest){
                    var access = route.guestAccess;
                    if(!access){
                        return false;
                    }
                    for(var i = 0; i < access.length; ++i){
                        if(guest.role === access[i]){
                            return true;
                        }
                    }
                }
                return false;
            }

            function establishedGuest(){
                if(scope.global.connectedGuest){
                    return scope.global.connectedGuest;
                }
                for(var role in guestRoles){
                    var accessKey = $location.search()[guestRoles[role].keyName];

                    if(accessKey){
                        scope.global.connectedGuest = {role: role, accessKey: accessKey};
                        scope.global["is" + role.charAt(0).toUpperCase() + role.slice(1)] = true;
                        return scope.global.connectedGuest;
                    }
                }
                return undefined;
            }

            scope.upgrade = function(){$location.path('/subscribe');};

            //For readability and to avoid conflicts the root scope is encapsulated
            //within a global object.
            scope.global = {};

            scope.global.updateSubscriptionType = function(type){
                if(type == 'RECURRING'){
                    scope.global.connectedUser.subscriptionType = "Monthly Subscription";
                } else if (type == 'BASIC'){
                    scope.global.connectedUser.subscriptionType = "30 Day Subscription";
                } else {
                    scope.global.connectedUser.subscriptionType = "Free Trial";
                }
            }

            scope.global.updateConnectedUser = function(user){

                scope.global.connectedUser.id = user.id;
                scope.global.connectedUser.firstName = user.firstName || 'Friend';
                scope.global.connectedUser.email = user.email;
                scope.global.connectedUser.paymentRequired = user.paymentRequired;
                scope.global.updateSubscriptionType(user.subscriptionType);

            }

            setConnectedUser(scope,true);

            scope.global.presets = window.Conf['presets'];
            scope.global.pageTitle = 'Prepado';

            scope.$on('$routeChangeStart', function (event, next, current) {
                if(checkGuestAccess(next)){
                    return;
                } else if (!window.Conf['authenticated_userid']){
                    // If user is trying to access a restricted page without being logged in
                    // except if index, login, or register, then redirect to login
                    if (!(next.templateUrl == '/public/partials/prep/main/index.html'  ||
                        next.templateUrl == '/public/partials/prep/main/login.html' ||
                        next.templateUrl == '/public/partials/prep/main/register.html' ||
                        next.templateUrl == '/public/partials/prep/main/forgotPasswordRequest.html' ||
                        next.templateUrl == '/public/partials/prep/main/forgotPassword.html')) {
                        // User arrived at login page, which means their session expired or
                        // something similar, so clear the global connectedUser so login
                        // form is shown
                            scope.global.connectedUser = null;
                            $location.url('/login?req='+$location.path());
                            scope.global.authorizationErrors = [{message:'Please log in before accessing that page.'}];
                    }
                    // redirect index to login
                    if (next.templateUrl == '/public/partials/prep/main/index.html'){
                        $location.url('/login');
                    }
                } else {
                    // If logged in user is trying to access index, redirect to dashboard
                    if (next.templateUrl == '/public/partials/prep/main/index.html'){
                        $location.url('/dashboard');
                    }
                    // If logged in and trying to go to login page, redirect to dashboard
                    if (next.templateUrl == '/public/partials/prep/main/login.html'){
                        $location.url('/dashboard');
                    }
                }
            });

            /**
             * On Route Change Success
             */
            scope.$on('$routeChangeSuccess', function(event, current, previous) {

                //when the route is changed successfully clear any errors out of the root scope
                scope.clearErrors();

                scope.httpStatus = undefined;

                // Set Page Title
                scope.global.pageTitle = current.$route.title !== undefined ? current.$route.title+' - Prepado' : 'Prepado';
                // Set Page Path
                scope.global.pagePath = $location.path();

                //log.log('RoutePath: '+scope.global.pagePath);
            });

            /**
             * On Partial Content Loaded
             */
            scope.$on('$viewContentLoaded', function(){
                $('html.lt-ie10 input, html.lt-ie10 textarea').placeholder();
            });

            /**
             * Check if current page
             * @route       angular route - ex. '/dashboard'
             */
            scope.checkRoute = function(route){
                if (scope.global.pagePath === route){
                    return true;
                } else {
                    return false;
                }
            };

            analytics.load();

            /**
             * Helper function to transition page to the top
             */
            scope.scrollToTop = function() {
                $("html, body").animate({ scrollTop: 0 }, "slow");
            };

        }])


        /**
         * Factory for Authentication Manager
         */
        .factory('AuthenticationManager', ['$rootScope',
            function (scope) {
                return {
                    update:function (user) {

                        window.Conf['authenticated_userid'] = user.id;
                        window.Conf['id'] = user.id;
                        window.Conf['userRoles'] = user.roles;
                        window.Conf['userName'] = user.firstName;
                        window.Conf['userEmail'] = user.email;
                        window.Conf['userPaymentRequired'] = user.paymentRequired;

                        if(user.subscriptionType == 'RECURRING'){
                            window.Conf['userSubscriptionType'] = "Monthly Subscription";
                        } else if (user.subscriptionType == 'BASIC'){
                            window.Conf['userSubscriptionType'] = "30 Day Subscription";
                        } else {
                            window.Conf['userSubscriptionType'] = "Free Trial";
                        }


                        setConnectedUser(scope);
                    },

                    clear:function () {

                        // reset global connecedUser
                        scope.global.connectedUser = null;

                        window.Conf['authenticated_userid'] = undefined;
                        window.Conf['id'] = undefined;
                        window.Conf['userRoles'] = undefined;
                        window.Conf['userName'] = undefined;
                        window.Conf['userEmail'] = undefined;
                        window.Conf['userPaymentRequired'] = undefined;
                        window.Conf['userPaymentRequired'] = undefined;

                        setConnectedUser(scope);
                    }
                };
            }
        ])

    ;

})(window, window.angular);