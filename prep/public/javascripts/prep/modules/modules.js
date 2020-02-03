(function (window, angular, undefined) {
    'use strict';

window.app = angular.module('Prepado', ['App'])

    /**
     * Define Partials base URL
     */
    .constant('partialsUrl', '/public/partials/prep/')

    /**
     * Module Config
     */
    .config(['$routeProvider', '$locationProvider', 'partialsUrl', function ($routeProvider, $locationProvider, partialsUrl) {
        $locationProvider.html5Mode(true);

        // ¡NOTICE: add routes to prep/conf/routes as well, or they won't work when linking directly!

        $routeProvider
        /**
         * Common "main" routes
         */
            .when('/', {
                templateUrl: partialsUrl + 'main/index.html',
                title: 'Prepado: Prepare to be Hired!'
            })
            .when('/login', {
                templateUrl: partialsUrl + 'main/login.html',
                title: 'Login'
            })
            .when('/register', {
                templateUrl: partialsUrl + 'main/register.html',
                title: 'Register'
            })
            .when('/dashboard', {
                templateUrl: partialsUrl + 'main/dashboard.html',
                title: 'Dashboard'
            })
            .when('/myAccount', {
                templateUrl: partialsUrl + 'main/myAccount.html',
                title: 'My Account'
            })
            .when('/styleGuide', {
                templateUrl: partialsUrl + 'main/styleGuide.html',
                title: 'Style Guide'
            })
            .when('/subscribe', {
                templateUrl: partialsUrl + 'main/subscribe.html',
                title: 'Subscription',
                controller: 'subscribeController'
            })
            .when('/card', {
                templateUrl: partialsUrl + 'main/card.html',
                title: 'Credit Card Information',
                controller: 'cardController'
            })
            .when('/forgotPassword',{
                templateUrl: partialsUrl + 'main/forgotPassword.html',
                title: 'Forgot Password'
            })
            .when('/forgotPasswordRequest',{
                templateUrl: partialsUrl + 'main/forgotPasswordRequest.html',
                title: 'Forgot Password Request'
            })

        /**
         * Interviewer routes
         */
            .when('/interviewer', {
                templateUrl: partialsUrl + 'interviewer/index.html',
                title: 'Practice Interviews'
            })
            .when('/interviewer/categoryList', {
                templateUrl: partialsUrl + 'interviewer/categoryList.html',
                title: 'Interview Question Categories'
            })
            .when('/interviewer/:interviewId/view', {
                templateUrl: partialsUrl + 'interviewer/viewInterview.html',
                title: 'Practice Interview',
                controller: 'viewInterview',
                reloadOnSearch: false
            })
            .when('/interviewer/:interviewId/review', {
                templateUrl: partialsUrl + 'interviewer/reviewInterview.html',
                title: 'Practice Interview',
                controller: 'reviewInterview',
                guestAccess: ['reviewer']
            })
            .when('/interviewer/:interviewId/join',{
                templateUrl: partialsUrl + 'main/join.html',
                title: 'Join Today!'
            })

        /**
         * Scheduler routes
         */
            .when('/scheduler', {
                templateUrl: partialsUrl + 'scheduler/upcomingInterviews.html',
                controller: 'SchedulerCtrl',
                title: 'Scheduler'
            })

        /**
         * Pandas
         */
            .otherwise({
                redirectTo: '/'
            })
        ;
    }])

;

})(window, window.angular);