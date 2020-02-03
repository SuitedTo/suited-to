(function (window, app, undefined) {
    'use strict';

    /**
     * Module Main Controllers
     */
    app.controller('main', ['$scope', '$location', 'PrepUser', function ($scope, $location, PrepUser) {

            $scope.logout = function () {
                PrepUser.logout(
                    function (data) {
                        $location.path('/');
                        $scope.user = null;
                    }
                );

            };


            //connected user contains some basic user information (id) that we can use to go get the full User object
            var userId = $scope.global.connectedUser.id;
            if (userId){
                PrepUser.get(userId, function (response) {
                    $scope.global.updateConnectedUser(response.data);
                });
            }

        }])


    /**
     * Dashboard controller
     */
        .controller('dashboardCtrl', ['$scope', 'PrepUser', '$http', '$location', function ($scope, PrepUser, $http, $location) {

            $scope.upgrade = function(){
                    $location.path('/subscribe');
            };

            $scope.myAccount = function(){
                    $location.path('/myAccount');
            };
        }])

    /**
     * Marketing/Join us! controller
     */
        .controller('joinCtrl', ['$scope', '$routeParams', '$location', '$http', function ($scope, $routeParams, $location, $http) {

            $scope.upgrade = function(){
                $location.url('/subscribe');
            };

            $scope.noThanks = function(){
                $location.url('/interviewer/'+$routeParams.interviewId+'/review');
            };
        }])

    /**
     * Controller for createInterviewForm
     */
        .controller('createInterviewController', ['$scope', '$location', 'InterviewBuilder', 'JobMatch', 'Interview','$timeout',
        function ($scope, $location, InterviewBuilder, JobMatch, Interview, $timeout) {

            $scope.skillLevel = 'MID';
            $scope.creatingInterview = false;

            /**
             * Submit the hints that have been selected from the typeahead
             * dropdown. If showCategoryList is set to true then the user will
             * have a chance to make changes to the compiled list of categories.
             * If showCategoryList is false then an interview will be built from
             * the compiled category list with no user intervention.
             */
            $scope.submitCategoryHints = function(showCategoryList){
                //timeout to ensure that selectedItem has been set
            	$timeout(submitHints(showCategoryList),200);
            }
            
            function submitHints(showCategoryList){

                $scope.createError = null;

                if($scope.selectedItem == null) {

                    // need to check with Pkat, but this should be obsolete
                    // $('#createJobTitle').change();

                    // if($scope.selectedItem == null) {
                        //console.log('You must choose a category or job.');
                        $scope.createError = 'You must select a valid Job Title or Category.';
                        return false;
                    // }
                }

                var selected = $.parseJSON($scope.selectedItem);

                $scope.creatingInterview = true;

                var requestObject = {};

                requestObject["entityId"] = selected.entityId;

                requestObject["type"] = selected.type;

                requestObject["level"] = $scope.skillLevel;

                //console.log($scope.skillLevel);
                //console.log(selected);

                var cats = {
                    "ENTRY": "ENTRY",
                    "MID": "MID",
                    "SENIOR": "SENIOR"
                };

                if (selected.type !== "PREP_CATEGORY"){
                    var checkSkill = false;
                    var skillArray = [];
                    $.each(selected.categories, function(index, val){

                        val.level = cats[val.level];
                        //console.log(val.level === $scope.skillLevel);
                        if ($.inArray(val.level,skillArray)==-1) skillArray.push(val.level);

                        if (val.level === $scope.skillLevel){
                            checkSkill = true;
                            return false;
                        }
                    });
                    //console.log('checkSkill',checkSkill);
                    //console.log('skillArray',skillArray);
                    if (checkSkill === false){
                        $scope.createError = 'This Job does not have skill level '+$scope.skillLevel+'. Please choose '+skillArray.join(' or ')+'.';
                        $scope.creatingInterview = false;
                        return false;
                    }
                }

                InterviewBuilder.createInterviewCategoryList(requestObject,
                    function (data) {

                        if(showCategoryList){
//                            TODO
//                            $location.search({interviewCategoryListBuildKey:data.id});
//                            $location.path('/interviewer/categoryList');
                        } else {
                            InterviewBuilder.safeGetInterviewCategoryListBuildResults(data.id,
                                function(build){

                                    InterviewBuilder.createInterviewFromCategoryList(build.categoryListId,
                                        function(build){
                                            Interview.get(build.interviewId,
                                                function(interview){
                                                    $scope.creatingInterview = false;
                                                    if(!interview.valid){
                                                        $scope.setError('INVALID_INTERVIEW');
                                                    } else {
                                                        $location.url('/interviewer/'+interview.id+'/view');
                                                    }
                                                },
                                                function(error){
                                                    $scope.creatingInterview = false;
                                                    $scope.setError(error[0].type);
                                                }
                                            );
                                        }, function(error){
                                            $scope.creatingInterview = false;
                                            $scope.setError(error[0].type);
                                        }
                                    );

                                }, function(error){
                                    $scope.creatingInterview = false;
                                    $scope.setError(error[0].type);
                                });
                        }
                    }, function(error){
                        $scope.creatingInterview = false;
                        $scope.setError(error[0].type);
                    }
                );
            };

            $scope.selectionMade = function(){

                return $scope.selectedItem != undefined;
            };

            $scope.onTypeaheadSelection = function(data){

                $scope.selectedItem = data;
                // Make sure that angular isn't currently $digest or $apply ing
                if(!$scope.$$phase) {
                    $scope.$digest();
                }
            };

            $scope.query =  "/jobmatch?searchTerm=%QUERY";

        }])


        .controller('forgotPasswordRequestController', ['$scope', '$rootScope', '$location', 'ForgotPasswordRequest', function($scope, $rootScope, $location, ForgotPasswordRequest) {
            /**
             * Reset password
             */
            $scope.resetPasswordClick = function (response) {
                ForgotPasswordRequest.resetPassword($scope.email,
                    function(data){
                        $rootScope.msg = "An email has been sent to: " + $scope.email;
                        $rootScope.user = {email: $scope.email};
                        $location.path('/forgotPassword');
                    },
                    function(error){
                        $scope.errors = error;
                    });
            }
        }])

    /**
     * Forgot Password Controller
     */
        .controller('forgotPasswordController', ['$scope', '$rootScope', '$location', 'PrepUser', 'ForgotPassword', function ($scope, $rootScope, $location, PrepUser, ForgotPassword) {
            $scope.successMsg = false;
            $scope.saving = false;
            $scope.user = $rootScope.user;
            $scope.emailSent = null;
            $scope.saveAccount = function (user){
                $scope.saving = true;
                ForgotPassword.saveAccount(user,
                    function(response){
                        PrepUser.login(
                            {email:user.email, password:user.password},
                            function (data) {
                                postLoginCallback(data);
                            },
                            function (error){
                                $scope.errors = error;
                            }
                        );
                        //$scope.global.connectedUser.id = user.id;
                        //$scope.global.connectedUser.firstName = user.firstName || 'Friend';
                        //$location.path('/dashboard');
                    },
                    function (error){
                        $scope.errors = error;
                    });
                $scope.saving = false;
            };

            var postLoginCallback = function (user) {
                if ($scope.req) {
                    $location.path($scope.req);
                } else {
                    $location.path('/dashboard');
                }
                $scope.global.connectedUser.id = user.id;
                $scope.global.connectedUser.firstName = user.firstName || 'Friend';
            };
        }])

    /**
     * Registration controller
     */
        .controller('registrationCtrl', ['$scope', '$location', 'PrepUser', 'Facebook', 'Google', function ($scope, $location, PrepUser, Facebook, Google) {

            /**
             * Standard registration with username and password
             * @param user
             */
            $scope.register = function (user) {
                PrepUser.register(user,
                    function (response_user) {
                        postRegistrationCallback(response_user);
                    },
                    function (error) {
                        $scope.errors = error;
                    }
                );
            };

            /**
             * Uses Facebook to register a new user
             */
            $scope.facebookRegistration = function () {
                Facebook.loginIfNeeded(function (authResponse) {
                    Facebook.registerFacebookUser(authResponse, postRegistrationCallback, postRegistrationErrorCallback);
                });
            };

            $scope.googleRegistration = function () {
                Google.registerGoogleUser(postRegistrationCallback, postRegistrationErrorCallback);
            };

            var postRegistrationCallback = function (user) {
                $location.path('/dashboard');

                $scope.global.connectedUser.id = user.id;
                $scope.global.connectedUser.firstName = user.firstName || 'Friend';
            };

            var postRegistrationErrorCallback = function (error) {
                $scope.errors = error;
            }
        }])

    /**
     * Login controller
     */
        .controller('loginCtrl', ['$scope', '$location', 'PrepUser', 'Facebook', 'Google', '$rootScope', function ($scope, $location, PrepUser, Facebook, Google, $rootScope) {
            $scope.req = $location.search()['req'];

            /**
             * Standard login with username and password
             * @param user
             */
            $scope.login = function (user) {
                PrepUser.login(user,
                    function (data) {
                        postLoginCallback(data);
                    },
                    function (error) {
                        if(error[0].message == "You are trying to login with your temp password."){
                            $rootScope.user = {
                                email: user.email,
                                temp: user.password
                            };
                            error = null;
                            $rootScope.msg = "Please change your password";
                            $location.path('/forgotPassword');
                        } else {
                            $scope.errors = error;
                        }
                    }
                );
            };

            /**
             * Forgot password
             */
            $scope.showForgotPassword = function () {
                $location.path('/forgotPasswordRequest');
            }


            /**
             * Uses Facebook to log in
             */
            $scope.facebookLogin = function () {
                Facebook.loginIfNeeded(function (authResponse) {
                    Facebook.loginFacebookUser(authResponse, postLoginCallback, postLoginErrorCallback);
                });
            };


            /**
             * Uses Google to log in
             */
            $scope.googleLogin = function () {
                Google.loginGoogleUser(postLoginCallback, postLoginErrorCallback);
            };

            var postLoginCallback = function (user) {
                if ($scope.req) {
                    $location.path($scope.req);
                } else {
                    $location.path('/dashboard');
                }

                $scope.global.connectedUser.id = user.id;
                $scope.global.connectedUser.firstName = user.firstName || 'Friend';
            };

            var postLoginErrorCallback = function (error) {
                $scope.errors = error;
            }
        }])


    /**
     * Discount Controller
     */
        .controller('discountController', ['$scope', 'PrepUser', 'Discount',
            function ($scope, PrepUser, Discount) {

                $scope.submitting = false;
                $scope.applyToUser = true;

                $scope.checkOnly = function(){
                    $scope.applyToUser = false;
                }

                //the onload directive isn't working for whatever reason
                //so hack in a temp workaround:
                $scope.applyToUser = $('#subscribeForm').length == 0;

                $scope.createDiscount = function (name) {

                    $scope.discountErrors = undefined;

                    var discountParams = {name:name};

                    if($scope.applyToUser){
                        discountParams.id = $scope.global.connectedUser.id;
                    }
                    $scope.submitting = true;
                    Discount.save(discountParams , function (response) {
                        var txt = $('#discountContainer').find('input:text');
                        txt.val('');
                        $scope.submitting = false;
                        $scope.$emit('discountCreated', response);
                    }, function (error) {
                        $scope.discountErrors = error;
                        $scope.submitting = false;
                    });
                };
            }])

    /**
     * My account controller
     */
        .controller('myAccountController', ['$scope', '$location', 'PrepUser', 'AuthenticationManager', '$modal', '$q',
            function ($scope, $location, PrepUser, AuthenticationManager, $modal, $q) {

                init();

                function init(){
                    $scope.creditCard = {};
                    PrepUser.get($scope.global.connectedUser.id, function (response) {
                        $scope.user = response.data;
                        $scope.initialUser = angular.copy($scope.user);

                        if($scope.user.isSubscriber){
                            $scope.creditCard.number = '************'+$scope.user.lastFourCardDigits;
                            $scope.creditCard.type = $scope.user.cardType;
                        }
                        $scope.submitting = false;
                    });
                }

                $scope.isDirty = function(){
                    var dirty = !angular.equals($scope.initialUser, $scope.user);
                    return !$scope.saving && dirty;
                }

                $scope.$on('discountCreated',
                    function(event, discount){
                        $scope.user.subscriptionCost = discount.resultingPrice;
                        $scope.initialUser = $scope.user;
                    }
                );

                $scope.renewAutomatically = function(){
                    $scope.subscriptionErrors = undefined;
                    $scope.submitting = true;
                    PrepUser.updateSubscriptionType($scope.user.id, "RECURRING",
                        function(){
                            $scope.global.updateSubscriptionType("RECURRING");
                            init();
                        },
                        function(errors){
                            $scope.submitting = false;
                            $scope.subscriptionErrors = errors;
                        });
                };

                $scope.stopSubscriptionWithPrompt = function(){
                    $scope.subscriptionErrors = undefined;
                    confirm("Stop Subscription?","Are you sure you want to stop your monthly subscription to Prepado?",$scope.stopSubscription);
                }

                $scope.stopSubscription = function () {
                    $scope.submitting = true;
                    PrepUser.updateSubscriptionType($scope.user.id, "BASIC",
                        function(){
                            $scope.global.updateSubscriptionType("BASIC");
                            init();
                        },
                        function(errors){
                            $scope.submitting = false;
                            $scope.subscriptionErrors = errors;
                        }
                    );
                };

                $scope.saveAccount = function (user) {
                    $scope.saving = true;

                    // Clear error messages
                    delete $scope.nameErrorMsg;
                    delete $scope.passwordErrorMsg;
                    delete $scope.emailErrorMsg;
                    delete $scope.errors;


                    var formValid = true;

                    if(isNullUndefinedWhitespaceEmpty(user.email)) {
                        $scope.emailErrorMsg = "Please enter an email";
                        formValid = false;
                    }

                    var passwordsMustMatchMsg = "Password must match confirm password";

                    // Treat empty password or confirm as null
                    if(user.password != undefined && user.password.length == 0) {
                        user.password = null;
                    }
                    if(user.confirm != undefined && user.confirm.length == 0) {
                        user.confirm = null;
                    }

                    var passwordIsNullorUndefined = user.password == null || user.password == undefined;
                    var confirmIsNullorUndefined = user.confirm == null || user.confirm == undefined;
                    if(passwordIsNullorUndefined) {
                        if(!confirmIsNullorUndefined) {
                            $scope.passwordErrorMsg = passwordsMustMatchMsg;
                            formValid = false;
                        }

                    }
                    else{
                        if(user.password.length < 6 || user.password.length > 20) {
                            $scope.passwordErrorMsg = "Password must contain between 6 and 20 characters";
                            formValid = false;
                        }
                        else if(confirmIsNullorUndefined) {
                            $scope.passwordErrorMsg = passwordsMustMatchMsg;
                            formValid = false;
                        }
                        else if(user.password.valueOf() != user.confirm.valueOf()) {
                            $scope.passwordErrorMsg = passwordsMustMatchMsg;
                            formValid = false;
                        }
                    }

                    if (formValid) {
                        /**
                         * Remove confirm password from object so we don't have to
                         * send it in the request.
                          */
                        delete user.confirm;

                        PrepUser.update(user,
                            function (response) {
                                AuthenticationManager.update(response);
                                $location.path('/dashboard');
                            }, function (response) {
                                $scope.saving = false;
                            }
                        );
                    } else {
                        $scope.saving = false;
                    }
                };

                function isNullUndefinedWhitespaceEmpty(str) {
                    return str == null || str == undefined ||
                        str.length <= 0 || str.trim().length <= 0;
                }

                function confirm(title, content, cb) {
                    $scope.action = cb;
                    $scope.modal = {
                        "title": title,
                        "content": content
                    };
                    $q.when(confirmModal).then(function(modalEl) {
                        modalEl.modal('show');
                    });
                };
                var confirmModal = $modal({template: '/public/partials/prep/common/confirmModal.html', backdrop: 'static', persist: true, show: false, scope: $scope});
            }])
    /**
     * Scheduler controllers
     */
        .controller('SchedulerCtrl', ['$scope', '$location', 'ActiveInterview',
            function ($scope, $location, ActiveInterview) {
                ActiveInterview.getUpcomingInterviews(function (response) {
                    $scope.interviews = response.data;
                });

                $scope.orderProp = 'name';
            }])

    /**
     * Category List Controller
     */
        .controller('categoryListCtrl', ['$scope', '$location','InterviewBuilder', 'Interview',
        function ($scope, $location, InterviewBuilder, Interview) {
            var search = $location.search();
            if (search.interviewCategoryListBuildKey) {
                InterviewBuilder.safeGetInterviewCategoryListBuildResults(search.interviewCategoryListBuildKey,
                    function (build) {

                        InterviewBuilder.getCategoryList(build.categoryListId, function (categoryList) {
                            $scope.categoryList = categoryList;
                        });

                    }, function (error) {
                        // TODO handle errors properly when this pages goes live
                        $scope.errorMessage = error.message;
                    });
            }

            $scope.createPracticeInterview = function () {

                if($scope.categoryListId){

                    //TODO: save any user updates:
                    // InterviewBuilder.saveInterviewCategoryList(...);

                    InterviewBuilder.createInterviewFromCategoryList($scope.categoryListId,
                        function(build){
                            $location.search({interviewBuildKey:build.id});
                            $location.path('/view');
                        }, function (error) {
                            // TODO handle errors properly when this pages goes live
                            $scope.errorMessage = error.message;
                        }
                    );
                }
            };

        }])

    /**
     * View Practice interview controller
     */
        .controller('viewInterview', ['$scope', 'RecordingService', 'Question', '$location', 'Interview', '$routeParams', '$rootScope', '$modal', '$q', '$window', '$http', 'PrepUser', '$timeout', function ($scope, RecordingService, Question, $location, Interview, $routeParams, $rootScope, $modal, $q, $window, $http, PrepUser, $timeout) {
            $scope.$window = $window;
            $scope.video_metadata = [];

            //$scope.supportsRTC = Modernizr.getusermedia;
            $scope.supportsRTC = /Chrome/.test(navigator.userAgent) && /Google Inc/.test(navigator.vendor);


            $scope.activeResponse = 'choose';
            $scope.textResponseBlock = false;
            $scope.videoResponseBlock = false;


            var recordTime = 60; //limit videos to 60 seconds

            var recorder;

            //Volume: audioControls.volume = (range from 0 "silent" --> 1 "loud")
            // var webCam = document.getElementById('video');
            // webCam.volume = 0; //Default, no sound.

            $scope.isDirty = function(){
                return !$scope.video_metadata[$scope.currentQuestionIndex].saving && !angular.equals($scope.initialQuestion, $scope.interview.questions[$scope.currentQuestionIndex]);
            }

            function updateCurrentQuestionIndex () {
                var index = $location.search()['q'] ? parseInt($location.search()['q']) - 1 : undefined;

                if (index === undefined) {
                    $scope.currentQuestionIndex = 0;
                    $scope.initialQuestion = angular.copy($scope.interview.questions[$scope.currentQuestionIndex]);
                } else if (index === -2) {
                    $scope.currentQuestionIndex = 0;
                    $scope.initialQuestion = angular.copy($scope.interview.questions[$scope.currentQuestionIndex]);
                    $location.search('q', 1);
                    $scope.interview.currentQuestion = 0;
                    Interview.update($scope.interview);
                } else {
                    $scope.currentQuestionIndex = index;
                    $scope.initialQuestion = angular.copy($scope.interview.questions[$scope.currentQuestionIndex]);
                    $scope.interview.currentQuestion = index;
                    Interview.update($scope.interview);
                }
                $scope.answersEnabled = false;

            };
            function getVideo(questionIndex){
                var questionId = $scope.interview.questions[$scope.currentQuestionIndex].id;
                Question.waitForVideo(questionId,
                    function(video){
                        if(video){
                            console.log(questionIndex)
                            $scope.video_metadata[questionIndex].videoPending = false;
                            $scope.video_metadata[questionIndex].hasVideo = true;
                            $scope.video_metadata[questionIndex].videoUrl = video.getUrl;
                        } else {
                            $timeout(getVideo(questionIndex),0);
                        }
                    },
                    function(err){
                        $scope.video_metadata[questionId].hasVideo = false;
                    }
                );
            }

            $scope.deleteVideo = function(){
                var questionId = $scope.interview.questions[$scope.currentQuestionIndex].id;
                Question.removeVideo(questionId,
                    function(){
                        loadInterview();
                    }
                );
            }

            function isVideoPending(){
                return "PENDING" === $scope.interview.questions[$scope.currentQuestionIndex].videoStatus;
            }

            function isVideoAvailable(){
                return "AVAILABLE" === $scope.interview.questions[$scope.currentQuestionIndex].videoStatus;
            }

            function init(){
                $scope.video_metadata[$scope.currentQuestionIndex] = $scope.video_metadata[$scope.currentQuestionIndex] || {};
                $scope.video_metadata[$scope.currentQuestionIndex].saving = false;
                $scope.video_metadata[$scope.currentQuestionIndex].videoPending = isVideoPending();
                $scope.video_metadata[$scope.currentQuestionIndex].hasVideo = $scope.video_metadata[$scope.currentQuestionIndex].videoPending || isVideoAvailable();
                $scope.video_metadata[$scope.currentQuestionIndex].savingVideo = false;
                $scope.video_metadata[$scope.currentQuestionIndex].saving = false;
                $scope.video_metadata[$scope.currentQuestionIndex].startingVideo = false;
                $scope.video_metadata[$scope.currentQuestionIndex].pendingPermission = false;
                $scope.video_metadata[$scope.currentQuestionIndex].videoCountdown = false;
                $scope.video_metadata[$scope.currentQuestionIndex].recordingVideo = false;
                $scope.video_metadata[$scope.currentQuestionIndex].pausedVideo = false;

                if($scope.video_metadata[$scope.currentQuestionIndex].hasVideo){
                    getVideo($scope.currentQuestionIndex);
                } else {
                    // webCam.src = '';
                }
            }

            $scope.$on('$routeUpdate', function (e, arg) {
                loadInterview();
            });

            function loadInterview(){
                Interview.get($routeParams.interviewId,
                    function(interview){
                        $scope.interview = interview;
                        updateCurrentQuestionIndex();
                        init();
                    }
                );
            }

            loadInterview();

            $scope.$watch(
                function() { return $scope.interview; },
                function(newValue) {
                    if(newValue !== undefined){
                        if($scope.interview.questions[$scope.currentQuestionIndex].videoStatus != "UNAVAILABLE"){
                            $scope.showVideoResponse();
                        } else if($scope.interview.questions[$scope.currentQuestionIndex].answers != null) {
                            $scope.showTextResponse();
                        }
                    }
                }
            )

            $scope.nextQuestion = function () {
                function next() {
                    $scope.video_metadata[$scope.currentQuestionIndex].saving = true;
                    $scope.submitAnswer();
                    $scope.scrollToTop();
                    $location.search('q', $scope.currentQuestionIndex + 2); // 0 = 1 to the user's GET parameter
                    if ($scope.activeResponse != 'video') {
                        prepInterviewWrittenResponse.focus();
                    }
                    if(recorder){
                        recorder.reset();
                    }
                }

                videoSavedCheck(next);
            };

            $scope.previousQuestion = function () {
                function previous() {
                    $scope.video_metadata[$scope.currentQuestionIndex].saving = true;
                    $scope.submitAnswer();
                    $scope.scrollToTop();
                    $location.search('q', $scope.currentQuestionIndex); // 0 = 1 to the user's GET parameter
                    if(recorder){
                        recorder.reset();
                    }
                }

                videoSavedCheck(previous);
            };

            $scope.submitAnswer = function () {
                Question.update($scope.interview.questions[$scope.currentQuestionIndex]);
            };

            PrepUser.get($scope.global.connectedUser.id,
                function(user){
                    $scope.user = user;
                }
            );

            $scope.finishQuestion = function () {
                function finish() {
                    $scope.submitAnswer();
                    $scope.interview.currentQuestion = -1;
                    Interview.update($scope.interview);
                    if($scope.user.data.isSubscriber){
                        $location.url('/interviewer/'+$scope.interview.id+'/review');
                    }else{
                        $location.url('/interviewer/'+$scope.interview.id+'/join');
                    }
                }

                videoSavedCheck(finish);
            };

            $scope.saveEnd = function () {
                $scope.submitAnswer();
                $scope.interview.currentQuestion = -1;
                Interview.update($scope.interview);
                $location.url('/interviewer');
            };

            $scope.showTextResponse = function () {
                function changeTab() {
                    $scope.activeResponse = 'text';
                    $scope.textResponseBlock = true;
                    $scope.videoResponseBlock = false;
                }
                videoSavedCheck(changeTab);
            };

            $scope.unSavedVideo = function() {
                return recorder && (recorder.state != recorder.states.STOPPED);
            }

            function videoSavedCheck(fn) {
                if ($scope.unSavedVideo()) {
                    confirmUnsaved(fn);
                } else {
                    fn();
                }
            }

            $scope.showVideoResponse = function () {
                if ($scope.activeResponse == 'text') {
                   $scope.submitAnswer();
                }
                $scope.activeResponse = 'video';
                $scope.textResponseBlock = false;
                $scope.videoResponseBlock = true;
            };

            $scope.setVideoUuid = function(uuid) {
                $scope.interview.questions[$scope.currentQuestionIndex].videoUuid = uuid;
            };



            $scope.startRecording = function() {
                $scope.video_metadata[$scope.currentQuestionIndex].videoPending = false;
                $scope.video_metadata[$scope.currentQuestionIndex].startingVideo = true;
                var startedCallback = function(){
                    $scope.video_metadata[$scope.currentQuestionIndex].startingVideo = false;
                    $scope.video_metadata[$scope.currentQuestionIndex].pendingPermission = false;
                    $scope.video_metadata[$scope.currentQuestionIndex].videoCountdown = false;
                    $scope.video_metadata[$scope.currentQuestionIndex].recordingVideo = true;
                    $scope.video_metadata[$scope.currentQuestionIndex].pausedVideo = false;
                    $scope.showUploadButton = false;
                    $scope.canRecordMore = true;
                }

                var failedToStart = function(err){
                    $scope.$apply(function(){
                        $scope.video_metadata[$scope.currentQuestionIndex].startingVideo = false;
                        requestAllow();
                    })

                }

                var expiredCallback = function(){
                    $scope.$apply(function(){
                        $scope.video_metadata[$scope.currentQuestionIndex].startingVideo = false;
                        $scope.video_metadata[$scope.currentQuestionIndex].recordingVideo = true;
                        $scope.video_metadata[$scope.currentQuestionIndex].pausedVideo = true;
                        $scope.showUploadButton = true;
                        $scope.canRecordMore = false;
                    });
                }

                if(!recorder){
                    var options = {
                        recordTime: recordTime,

                        //"expired" means the timer ran down to zero while recording and
                        //recording was stopped as a result
                        expiredCallback: expiredCallback,

                        //Called when user denies access to web cam
                        errorCallback: failedToStart,

                        //called with every tick of the timer while recording
                        timerCallback: function(time){
                            $scope.videoTimer = time;
                        },

                        //called with every tick of the timer while counting down to record
                        countdownCallback: function(time){
                            $scope.$apply(function(){
                                $scope.video_metadata[$scope.currentQuestionIndex].pendingPermission = false;
                                $scope.video_metadata[$scope.currentQuestionIndex].videoCountdown = true;
                                $scope.countdownTimer = time
                            });
                        }
                    };
                    recorder = RecordingService.createRecorder(document.getElementById('video'), options);
                }
                $scope.countdownTimer = 3;
                recorder.startRecording($scope.countdownTimer, startedCallback, failedToStart);

                /*
                    If after a fair delay the countdown hasn't started then we must be waiting for
                    the user to grant permission to use the cam/mic
                 */
                $timeout(function(){
                    if($scope.video_metadata[$scope.currentQuestionIndex].startingVideo && !$scope.video_metadata[$scope.currentQuestionIndex].videoCountdown){
                        $scope.video_metadata[$scope.currentQuestionIndex].pendingPermission = true;
                    }
                },3000);


            };

            $scope.stopRecording = function() {
                if(recorder){
                    var stoppedCallback = function(){
                        $scope.video_metadata[$scope.currentQuestionIndex].recordingVideo = true;
                        $scope.video_metadata[$scope.currentQuestionIndex].pausedVideo = true;
                        $scope.showUploadButton = true;
                        $scope.canRecordMore = true;
                    }

                    recorder.stopRecording(stoppedCallback);
                }
            };

            $scope.cancelRecording = function() {
                init();
                if(recorder){
                    recorder.reset();
                }
            };

            $scope.finishRecording = function() {
                $scope.video_metadata[$scope.currentQuestionIndex].recordingVideo = false;
                $scope.video_metadata[$scope.currentQuestionIndex].savingVideo = true;
                $scope.uploadVid(
                    function(){
                        $scope.video_metadata[$scope.currentQuestionIndex].savingVideo = false;
                        recorder.reset();
                        $scope.$apply(function () {
                            loadInterview();
                        });
                });

            };



            $scope.uploadVid = function(callback) {

                if(recorder){

                    var numberOfSegments = recorder.audio.length;
                    var s3PutURLGetters = [];
                    var videoPutURL = [];
                    var audioPutURL = [];
                    var video = [], audio = [];
                    for (var i = 0; i < numberOfSegments; i++) {
//                        s3PutURLGetters.push(
//                            function(callback){
//                                $.post('/s3upload', "{'ext':'.webm', 'contentType':'video/webm'}",
//                                    function(result) {
//                                        videoPutURL.push(result.url);
//                                        video.push(result.contents);
//                                        callback();
//                                    }
//                                );
//                            }
//                        );
                        s3PutURLGetters.push(
                            function(callback){
                                $.post('/s3upload', "{'ext':'.wav', 'contentType':'audio/wav'}",
                                    function(result) {
                                        audioPutURL.push(result.url);
                                        audio.push(result.contents);
                                        callback();
                                    }
                                );
                            }
                        );

                    }

                    var createCORSRequest = function(method, url) {
                        var xhr = new XMLHttpRequest();
                        if ("withCredentials" in xhr) {

                            // Check if the XMLHttpRequest object has a "withCredentials" property.
                            // "withCredentials" only exists on XMLHTTPRequest2 objects.
                            xhr.open(method, url, true);

                        } else if (typeof XDomainRequest != "undefined") {

                            // Otherwise, check if XDomainRequest.
                            // XDomainRequest only exists in IE, and is IE's way of making CORS requests.
                            xhr = new XDomainRequest();
                            xhr.open(method, url);

                        } else {

                            // Otherwise, CORS is not supported by the browser.
                            xhr = null;

                        }
                        return xhr;
                    }

                    var storeBlob = function(options){

                        var xhr = createCORSRequest('PUT', options.url);
                        if (!xhr) {
                            throw new Error('CORS not supported');
                        }

                        //xhr.upload.addEventListener("progress", progress_handler, true);
                        //xhr.addEventListener("error", error_handler, true);
                        //xhr.addEventListener("timeout", error_handler, true);

                        xhr.addEventListener("readystatechange",
                            function(evt){
                                if(options.callback && (xhr.readyState == 4)){
                                    options.callback();
                                }
                            }
                            , true);

                        xhr.send(options.blob);

                    }

                    var s3Putters = [];
                    async.parallel(
                        s3PutURLGetters,
                        function(err){
                            for (var i = 0; i < numberOfSegments; i++) {
                                s3Putters.push(
                                    (function(i){
                                        return function(callback){
                                            storeBlob({url:videoPutURL[i], blob:recorder.video[i], callback:callback});
                                        };
                                    }(i))
                                );
                                s3Putters.push(
                                    (function(i){
                                        return function(callback){
                                            storeBlob({url:audioPutURL[i], blob:recorder.audio[i], callback:callback});
                                        };
                                    }(i))
                                );
                            }
                            async.parallel(
                                s3Putters,
                                function(err){
                                    //console.log("Finished storing all audio and video files");

                                    var request = new XMLHttpRequest();

                                    var data = {
                                        questionId: $scope.interview.questions[$scope.currentQuestionIndex].id,
                                        audio: audio,
                                        video: video
                                    };
                                    $.post("/answervideobuilds", JSON.stringify(data));

                                    $scope.video_metadata[$scope.currentQuestionIndex].videoPending = true;
                                    if(callback){
                                        callback(err);
                                    }
                                }
                            );
                        }
                    );


                }

            };

            function confirmUnsaved(fn) {
                var confirmUnsavedModal = $modal({template: '/public/partials/prep/common/confirmModal.html', backdrop: 'static', persist: true, show: false, scope: $scope});
                $scope.action = fn;
                $scope.modal = {
                    "title": "Confirm unsaved video",
                    "content": "Your video is not yet saved, are you sure you want to continue?"
                };
                $q.when(confirmUnsavedModal).then(function(modalEl) {
                    modalEl.modal('show');
                });
            };



            function requestAllow(cb) {
                var requestAllowModal = $modal({template: '/public/partials/prep/common/informationModal.html', backdrop: 'static', persist: true, show: false, scope: $scope});
                $scope.informedCallback = cb;
                $scope.modal = {
                    "title": "Camera and Microphone Access Required",
                    "content": "Prepado needs access to your camera and microphone in order to record video answers. Please update your browser settings to grant access."
                };
                $q.when(requestAllowModal).then(function(modalEl) {
                    modalEl.modal('show');
                });
            };

        }])

    /**
     * Review Practice interview controller
     */
        .controller('reviewInterview', ['$scope','Question', 'Interview', 'InterviewReview', 'QuestionReview','$location', '$routeParams', '$rootScope', '$window', '$http',
                                            function ($scope, Question, Interview, InterviewReview, QuestionReview, $location, $routeParams, $rootScope, $window, $http) {
            $scope.$window = $window;
            $scope.numQuestionsAnswered = 0;

            $scope.saving = false;

            $scope.isDirty = function(){
                var dirty = false;
                for (var i = 0; i < $scope.interview.questions.length; i++) {
                    if(!angular.equals($scope.serverSideAnswers[i],
                        $scope.interview.questions[i].answers)){
                        dirty = true;
                        break;
                    }
                }
                return !$scope.saving && dirty;
            }

            $scope.STATUS_CONSTANTS = {"PENDING" : "Pending",
                "IN_PROGRESS" : "In Progress",
                "COMPLETED" : "Completed"};

            if(!$scope.global.isReviewer){
                Interview.get($routeParams.interviewId,
                    function(interview){
                        $scope.interview = interview;
                        /**
                         * This lists contains the server's view of this user's answers,
                         *  including updates via submitAnswer. Used to bring answers
                         *  back to their state if the user cancels a question edit.
                         * @type {Array}
                         */
                        $scope.serverSideAnswers = [];
                        for (var i=0; i<$scope.interview.questions.length; i++) {
                            $scope.serverSideAnswers[i] = $scope.interview.questions[i].answers; // stored in case the user cancels before submit
                        }

                        $scope.numQuestionsAnswered = $scope.getNumAnsweredQuestions();
                        InterviewReview.list({id : $routeParams.interviewId}, function(result) {
                            $scope.reviewers = result;

                            for (var i=0; i<$scope.interview.questions.length; i++) {
                                $scope.interview.questions[i].reviews = [];
                                for(var j=0; j < $scope.reviewers.length; ++j){
                                    for(var k=0; k < $scope.reviewers[j].questions.length; ++k){
                                        if($scope.reviewers[j].questions[k].question.id === $scope.interview.questions[i].id){
                                            var reviewer = $scope.reviewers[j].reviewerEmail.substring(0,$scope.reviewers[j].reviewerEmail.indexOf('@'));
                                            $scope.interview.questions[i].reviews.push($.extend({},{reviewer:reviewer},$scope.reviewers[j].questions[k]));
                                            break;
                                        }
                                    }
                                }
                            }


                            //Check for video answers
                            $http.get('/answervideo/videosforrinterview/' + $routeParams.interviewId)
                                .success(function(response) {
                                    var videos = response.data.videos;
                                    for(var m=0; m < videos.length; m++) {
                                        for(var n = 0; n < $scope.interview.questions.length; n++) {
                                            if(videos[m].questionId == $scope.interview.questions[n].id) {
                                                $scope.interview.questions[n].videoAnswer = videos[m];
                                            }
                                        }
                                    }
                                });
                        })

                    }
                );
            } else {

                Interview.getForReview($routeParams.interviewId, $scope.global.connectedGuest.accessKey,
                    function(interview){
                        $scope.interview = interview;
                        /**
                         * This lists contains the server's view of this user's answers,
                         *  including updates via submitAnswer. Used to bring answers
                         *  back to their state if the user cancels a question edit.
                         * @type {Array}
                         */

                        $scope.serverSideAnswers = [];
                        for (var i=0; i<$scope.interview.questions.length; i++) {
                            $scope.serverSideAnswers[i] = $scope.interview.questions[i].answers; // stored in case the user cancels before submit
                        }

                        $scope.numQuestionsAnswered = $scope.getNumAnsweredQuestions();



                        InterviewReview.get($routeParams.interviewId, $scope.global.connectedGuest.accessKey,
                            function(review){
                                $scope.review = angular.copy(review);
                                $scope.review.questions = [];

                                $scope.serverSideQuestionReviews = [];
                                for (var i=0; i<$scope.interview.questions.length; i++) {
                                    for(var k=0; k < review.questions.length; ++k){
                                        if(review.questions[k].question.id === $scope.interview.questions[i].id){
                                            $scope.review.questions.push(review.questions[k]);
                                            break;
                                        }
                                    }
                                    if(!$scope.review.questions[i]){
                                        var emptyQuestionReview = {question: $scope.interview.questions[i],text: ''};
                                        $scope.review.questions.push(emptyQuestionReview);
                                    }
                                    $scope.review.questions[i].prepInterviewReviewId = $scope.review.id;
                                    $scope.serverSideQuestionReviews[i] = $scope.review.questions[i].text; // stored in case the user cancels before submit

                                }
                            }
                        );
                    },
                    //Interview is not found, or reviewerKey is invalid (deleted or never existed)
                    function(result) {
                        $rootScope.httpStatus = "We're Sorry!  The interview you're trying to access has been deleted or you are no longer a reviewer."
                            + " If you think this is incorrect, contact the user who requested your review.";
                    }
                );

            }
            $scope.answerEditable = {prepInterview : $routeParams.interviewId };

            $scope.answerVisible = {};
            $scope.reviewVisible = {};
            $scope.reviewRequest = {};
            $scope.reviewEditable = {};
            $scope.reviewers = [];
            $scope.allVisible = false;
            $scope.numQuestionsAnswered = 0;

            $scope.showAnswers = function () {
                $scope.allVisible =! $scope.allVisible;
                for (var i = 0; i < $scope.interview.questions.length; i++) {
                    $scope.answerVisible[i] = $scope.allVisible;
                    $scope.reviewVisible[i] = $scope.allVisible;
                }
            };

            $scope.getNumAnsweredQuestions = function () {
                var answered = 0;
                if ($scope.interview) {
                    for (var i = 0; i < $scope.interview.questions.length; i++) {
                        //if there is a text or video answer then mark the question as answered
                        if ($scope.interview.questions[i].answers || $scope.interview.questions[i].answerVideoId) {
                            answered++;
                        }
                    }
                }
                return answered;
            };

            $scope.cancelAnswer = function (i) {
                $scope.answerEditable[i] =! $scope.answerEditable[i];
                $scope.interview.questions[i].answers = $scope.serverSideAnswers[i];
            };

            $scope.submitAnswer = function (i) {
                $scope.saving = true;
                $scope.answerEditable[i] =! $scope.answerEditable[i];
                Question.update($scope.interview.questions[i],
                    function(response) {
                        /**
                         * Update serverSideAnswers to match serverside data
                          */
                        $scope.serverSideAnswers[i] = $scope.interview.questions[i].answers;
                        $scope.numQuestionsAnswered = $scope.getNumAnsweredQuestions();
                        $scope.saving = false;
                    });
            };

            $scope.cancelQuestionReview = function (i) {
                $scope.reviewEditable[i] =! $scope.reviewEditable[i];
                $scope.review.questions[i].text = $scope.serverSideQuestionReviews[i];
            };

            $scope.finishReview = function () {
                $scope.saving = true;
                $scope.review.status = 'COMPLETED';
                InterviewReview.update($scope.review, $scope.global.connectedGuest.accessKey);
            };

            $scope.submitQuestionReview = function (i) {
                $scope.saving = true;
                $scope.reviewEditable[i] =! $scope.reviewEditable[i];

                var persist = $scope.review.questions[i].id?QuestionReview.update:QuestionReview.save;

                persist($scope.review.questions[i], $scope.global.connectedGuest.accessKey,
                    function(response) {
                        /**
                         * Update serverSideAnswers to match serverside data
                         */
                        $scope.serverSideQuestionReviews[i] = $scope.review.questions[i].text;
                        $scope.saving = false;
                    });

            };

            $scope.retakeInterview = function () {
                $scope.confirmRetake = true;
            };

            $scope.confirmRetakeInterview = function () {
                $scope.interview.reset = true;
                Interview.update($scope.interview);
                $location.url('/interviewer/'+$scope.interview.id+'/view');
            };

            $scope.confirmReviewRequest = function() {
                $scope.reviewError = undefined;
                var confirmation = true;
                // We won't need to warn the user more than once
                if($scope.reviewers.length == 0){
                    var confirmation = confirm("Are you sure?\nIf you add a reviewer, you cannot retake this practice interview.");
                }
                if(confirmation) {
                    $scope.showInvitationFields = true;
                }
            };

            $scope.submitReviewRequest = function() {
                $scope.reviewRequest.prepInterview = $scope.interview;
                InterviewReview.save($scope.reviewRequest, function(result) {

                    InterviewReview.list({id : $routeParams.interviewId}, function(result) {
                        $scope.reviewers = result;
                    });

                    $scope.reviewError = undefined;
                    $scope.reviewRequest = {};
                    $scope.reviewRequest.status = "PENDING";
                    $scope.showInvitationFields = false;

                }, function(errorResult) {
                    $scope.reviewError = errorResult[0].message;
                })
            };

            $scope.noThanks = function() {
                $http.post('/emails/' + "WEBAPP_TRIAL_COMPLETE" + "/" + $scope.global.connectedUser.id);
                $location.url('/dashboard');
            };

            $scope.removeReviewer = function(index) {
                if(confirm("Are you sure you want to remove this reviewer?")) {
                    InterviewReview.remove($routeParams.interviewId, $scope.reviewers[index].reviewKey, function() {
                        InterviewReview.list({id : $routeParams.interviewId}, function(result) {
                            $scope.reviewers = result;
                        });
                    });
                }
            };

            $scope.remindReviewer = function(index) {
                InterviewReview.resendEmail($scope.reviewers[index].id);
            }

        }])

    /**
     * Interviewer Index controller
     */
        .controller('interviewerCtrl', ['$scope', 'PrepUser', '$http', function ($scope, PrepUser, $http) {

            $scope.showCreateForm = function () {
                $scope.createInterviewForm =! $scope.createInterviewForm;
            };

        }])

    /**
     * Interviewer Index controller
     */
        .controller('interviewListCtrl', ['$scope', 'PrepUser', 'Interview', '$location', function ($scope, PrepUser, Interview, $location) {
            PrepUser.get($scope.global.connectedUser.id, function (response) {
                $scope.user = response.data;
            });

            $scope.noInterviewsMSG = "You haven't created any practice interviews.";

            // TODO: Add pagination controls on /interviewer/index
            var pagination = {
                page : '0,999'
            };

            var getList = function(){
                Interview.list(pagination,
                    function(data) {
                        $scope.recentInterviews = data.rows;
                    });
            };

            $scope.initList = function(limit){
                pagination.page = '0,' + limit;
                getList();
            };

            // SAMPLE FOR MIKE
            $scope.nextPage = function(limit){
                // clear old list
                pagination.page = '1,' + limit;
                getList();
            };

            $scope.loadInterview = function(interview){
                if($scope.user.isSubscriber){
                    if(interview.currentQuestion != -1){
                        $location.url('/interviewer/'+interview.id+'/view?q='+(parseInt(interview.currentQuestion)+1));
                    } else {
                        $location.url('/interviewer/'+interview.id+'/review');
                    }
                }else{
                    $location.url('/interviewer/'+interview.id+'/join');
                }
            };

            $scope.isFinishedText = function(interview){
                if(interview.currentQuestion != -1){
                    return 'Finish';
                } else {
                    return 'Review';
                }
            };

            var formatDate = function (dateString) {
                //Can't just use datestring to create the object becuase IE and Safari do not correctly support that conversion
                var d1 = new Date(dateString.substr(0,4), dateString.substr(5,2), dateString.substr(8,2));
                return (d1.getMonth() +'/' + d1.getDate() + '/' + d1.getFullYear());
            };

            $scope.lastDateTouched = function(interview) {
                var lastTouched;
                if(interview.hasOwnProperty('updated')) {
                    lastTouched = formatDate(interview.updated);
                }
                else {
                    lastTouched = formatDate(interview.created);
                }

                return lastTouched;
            };

        }])

    /**
     * Payment request modal controller
     */
        .controller('paymentRequestModalCtrl', ['$scope', 'PrepUser', '$location', function ($scope, PrepUser, $location) {
            $scope.myAccount = function(){
                $location.path('/myAccount');
            };

        }])

        .controller('paymentController', ['$rootScope', '$scope','$location', 'Payment', 'PrepUser', function ($rootScope, $scope, $location, Payment, PrepUser) {
            $scope.creditCard = {};

            PrepUser.get($scope.global.connectedUser.id, function (response) {
                $scope.user = response.data;
                if (response.data.lastFourCardDigits) {
                    $scope.creditCard.number = '************'+response.data.lastFourCardDigits;
                    $scope.creditCard.type = response.data.cardType;
                }

                Payment.get($scope.user, function(data) {
                    $scope.chargeAmount = (data.amount/100).toFixed(2);
                });
            });

            $scope.submitPayment = function () {
                $scope.submitting = true;

                var makePaymentCall = function () {
                    Payment.save($scope.user,  // id is placeholder: SecurityUtil.connectedUser used on the backend instead
                        function (response) {
                            $scope.errors = undefined;
                            if (response.amount == 0) {
                                $scope.amountMessage = 'Great news! There was no need to charge your card.';
                            } else {
                                $scope.amountMessage = 'Your card was successfully charged for $' + (response.amount / 100).toFixed(2);
                            }
                            $scope.submitting = false;
                            $scope.global.connectedUser.paymentRequired = false;

                            //need to update the global subscription type
                            PrepUser.get($scope.global.connectedUser.id, function (response) {
                                $scope.global.updateConnectedUser(response.data);

                            });
                        }, function (error) {
                            $scope.errors = error;
                            $scope.submitting = false;
                        }
                    );
                };

                var newCardCallback = function (status, response) {
                    if (status == 200) {
                        $scope.creditCardErrorMsg = undefined;
                        $scope.errors = undefined;
                        $scope.user.stripeToken = response.id;
                        PrepUser.update($scope.user, function () {
                            makePaymentCall();
                        }, function (errors) {
                            $scope.errors = errors;
                            $scope.submitting = false;
                        });
                    } else {
                        $scope.creditCardErrorMsg = response.error.message;
                        $scope.errors = undefined;
                        $scope.submitting = false;
                        if(!$scope.$$phase) {
                            $scope.$apply();
                        }
                    }
                };

                if (!$scope.hideCC) {
                    Stripe.setPublishableKey(window.Conf.stripeApiKey);
                    Stripe.createToken($('#paymentForm'), newCardCallback); // Stripe expects a DOM element
                } else {
                    //eligible for 100% off
                    makePaymentCall();
                }

            };

            $scope.applyCoupon = function () {
                Payment.update($scope.user , function (data) {
                    $scope.errors = undefined;
                    $scope.chargeAmount = (data.amount/100).toFixed(2);
                    $scope.hideCC = $scope.chargeAmount == 0;
                }, function (error) {
                    $scope.errors = error;
                });
            };
        }])


    /**
     * New Subscriptions
     */
        .controller('subscribeController', ['$rootScope', '$scope','$location', 'Card', 'Subscription','PrepUser',
            function ($rootScope, $scope, $location, Card, Subscription, PrepUser) {

            init();

            function init(){
                $scope.coupon = undefined;
                $scope.subscriptionCost = 2500;
                $scope.creditCard = {};
                $scope.submitting = false;
                $scope.errors = undefined;
                $scope.creditCardErrorMsg = undefined;

                PrepUser.get($scope.global.connectedUser.id, function (response) {
                    $scope.user = response.data;
                });

                if($scope.cardCreateForm){
                    $('#cardCreateForm').find('input:text').val('');
                    //$scope.cardCreateForm.$setPristine();
                }
            };

            $scope.$on('discountCreated',
                function(event, discount){
                    $scope.coupon = discount.coupon;
                    $scope.subscriptionCost = discount.resultingPrice;
                }
            );

            function createdToken(status, response){
                if (status == 200) {
                    var params = {id:$scope.user.id, stripeToken:response.id};
                    if($scope.coupon){
                        params.coupon = $scope.coupon;
                    }
                    Subscription.save(params,
                        function(){
                            $scope.submitting = false;
                            $scope.global.updateSubscriptionType("RECURRING");
                            $location.url('/myAccount');
                        },
                        function(errors){
                            $scope.errors = errors;
                            $scope.submitting = false;
                        });




                } else {

                    $scope.$$phase || $scope.$apply(
                        function(){
                            $scope.creditCardErrorMsg = response.error.message;
                            $scope.errors = undefined;
                            $scope.submitting = false;
                        }
                    );

                }
            }

            $scope.subscribe = function(){
                $scope.submitting = true;
                $scope.created = false;
                Stripe.setPublishableKey(window.Conf.stripeApiKey);
                Stripe.createToken($('#subscribeForm'), createdToken); // Stripe expects a DOM element
            }

        }])

    /**
     * Credit Card Updates
     */
        .controller('cardController', ['$rootScope', '$scope','$location', 'Card', 'PrepUser', function ($rootScope, $scope, $location, Card, PrepUser) {

            init();

            function init(updated){
                $scope.creditCard = {};
                $scope.submitting = false;
                $scope.errors = undefined;
                $scope.creditCardErrorMsg = undefined;
                $scope.updated = updated;

                PrepUser.get($scope.global.connectedUser.id, function (response) {
                    $scope.user = response.data;
                    if (response.data.lastFourCardDigits) {
                        $scope.creditCard.number = '************'+response.data.lastFourCardDigits;
                        $scope.creditCard.type = response.data.cardType;
                    }
                });

                if($scope.cardUpdateForm){
                    $('#cardUpdateForm').find('input:text').val('');
                    //$scope.cardUpdateForm.$setPristine();
                }
            };

            function createdToken(status, response){
                if (status == 200) {
                    Card.update({id:$scope.user.id, stripeToken:response.id},
                        function(){
                            init(true);
                        },
                        function(err){
                            $scope.errors = errors;
                            $scope.submitting = false;
                        }
                    );

                } else {
                    $scope.creditCardErrorMsg = response.error.message;
                    $scope.errors = undefined;
                    $scope.submitting = false;
                    if(!$scope.$$phase) {
                        $scope.$apply();
                    }
                }
            }

            $scope.updateCard = function(){
                $scope.submitting = true;
                $scope.updated = false;
                Stripe.setPublishableKey(window.Conf.stripeApiKey);
                Stripe.createToken($('#cardUpdateForm'), createdToken); // Stripe expects a DOM element
            }

        }])

    ;

})(window, window.app);
