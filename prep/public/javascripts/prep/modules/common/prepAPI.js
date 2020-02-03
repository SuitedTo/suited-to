/**
 * The PrepAPI module represents the "API Layer". This layer sits on top of
 * the "Resources Layer" to provide a level of abstraction above the restful
 * prep services.
 */

(function (window, angular, undefined) {
    'use strict';


    angular.module('PrepAPI', ['PrepServer', 'Utils'])


    /**
     * Prep User API
     */
        .factory('PrepUser', ['AuthenticationManager', 'Resources', function (AuthenticationManager, Resources) {

        function authenticate(user) {
            AuthenticationManager.update(user);
        }

        function clearSession() {
            AuthenticationManager.clear();
        }

        return (function () {
            var user = {};
            var u = Resources.get('user');
            var s = Resources.get('session');
            var subscription = Resources.get('subscription');

            user.login = function (credentials, successCallback, errorCallback) {
                s.save(
                    credentials,
                    function (response) {
                        authenticate(response.data);
                        if (successCallback) {
                            successCallback(response.data);
                        }
                    }, errorCallback
                );

            };

            user.update = function (data, successCallback, errorCallback) {
                u.update (
                    data,
                    function (response) {
                        if (successCallback) {
                            successCallback(response.data);
                        }
                    }, errorCallback
                );
            };

            user.logout = function (successCallback, errorCallback) {

                //console.log(successCallback);
                s.remove();
                clearSession();

                if (successCallback) {
                    successCallback();
                }

            };

            user.register = function (credentials, successCallback, errorCallback) {
                u.save(
                    credentials,
                    function (response) {
                        authenticate(response.data);
                        if (successCallback) {
                            successCallback(response.data);
                        }
                    }, errorCallback
                );

            };
            user.get = function (id, successCallback, errorCallback) {
                u.get({id:id}, successCallback, errorCallback);
            };

            user.updateSubscriptionType = function (id, type, successCallback, errorCallback) {
                subscription.update(
                    {id:id, type:type},
                    function (response) {
                        if (successCallback) {
                            successCallback(response.data);
                        }
                    }, errorCallback
                );

            };
            return user;

        })();

    }])/* End Prep User*/

        .factory('Subscription', ['Resources', function (Resources) {
            return (function () {
                var subscription = {};
                var s = Resources.get('subscription');

                subscription.save = function (data, successCallback, errorCallback) {
                    s.save(
                        data,
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    );
                };

                subscription.get = function (data, successCallback, errorCallback) {
                    s.get(
                        data,
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    )
                };

                subscription.update = function (data, successCallback, errorCallback) {
                    s.update(
                        data,
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    )
                };

                return subscription;
            })();

        }])


        .factory('Card', ['Resources', function (Resources) {
            return (function () {
                var card = {};
                var c = Resources.get('card');

                card.save = function (data, successCallback, errorCallback) {
                    c.save(
                        data,
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    );
                };

                card.get = function (data, successCallback, errorCallback) {
                    c.get(
                        data,
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    )
                };

                card.update = function (data, successCallback, errorCallback) {
                    c.update(
                        data,
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    )
                };

                return card;
            })();

        }])


        .factory('Discount', ['Resources', function (Resources) {
            return (function () {
                var discount = {};
                var d = Resources.get('discount');

                discount.save = function (data, successCallback, errorCallback) {
                    d.save(
                        data,
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    );
                };

                discount.get = function (data, successCallback, errorCallback) {
                    d.get(
                        data,
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    )
                };

                discount.update = function (data, successCallback, errorCallback) {
                    d.update(
                        data,
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    )
                };

                return discount;
            })();

        }])

    /**
     * Interview Builder API
     */
        .factory('InterviewBuilder', ['Resources', '$rootScope', function (Resources, scope) {


        return (function () {
            var interviewBuild = Resources.get('interviewbuild');

            var categoryList = Resources.get('interviewcategorylist');
            var categoryListBuild = Resources.get('interviewcategorylistbuild');

            var instance = {};


            instance.getCategoryList = function (id, successCallback, errorCallback) {
                categoryList.get({id:id}, function (response) {
                    successCallback(response.data);
                }, errorCallback);
            };

            instance.getCategoryListBuild = function (id, successCallback, errorCallback) {
                categoryListBuild.get({id:id}, function (response) {
                    successCallback(response.data);
                }, errorCallback);
            };


            instance.createInterviewCategoryList = function (data, successCallback, errorCallback) {
                categoryListBuild.save(data, function (response) {
                    successCallback(response.data);
                }, errorCallback);
            };

            /**
             * Get the category list with the given id, ask the interview builder
             * to build an interview with the contents of that list, and wait for the
             * interview builder to finish.
             *
             * @param categoryListId The id of the category list that is used as
             * input to the interview builder.
             * @param successCallback Called after the build has finished with the
             * resulting build id.
             * @param errorCallback
             */
            instance.createInterviewFromCategoryList = function(categoryListId,
                                                                successCallback,
                                                                errorCallback){
                instance.getCategoryList(categoryListId,
                    function(categoryList){
                        var buildRequest = {};
                        buildRequest.name = categoryList.name;
                        buildRequest.categories = categoryList.categories;
                        interviewBuild.save(buildRequest, function(response){
                                instance.safeGetInterviewBuildResults(response.data.id,
                                    function(build){
                                        successCallback(build);
                                    },
                                    function(error){
                                        errorCallback(error);
                                    }
                                );
                            },
                            function(error){
                                errorCallback(error);
                            });
                    },
                    function(error){
                        errorCallback(error);
                    });
            };


            instance.getInterviewBuildResults = function (key, successCallback, errorCallback) {
                interviewBuild.get({id:key}, function (response) {
                    successCallback(response.data);
                }, errorCallback);
            };

            var getInterviewBuildN = function (key, N, successCallback, errorCallback) {
                if (N === 20) {//500 X 12 => 10 seconds
                    /*
                     * Generate a synthetic timeout error
                     */
                    var timeout = {};
                    timeout.code = 1;
                    timeout.message = "Timed out after 10 seconds";
                    errorCallback(timeout);
                } else {
                    setTimeout(
                        function () {
                            interviewBuild.get({id:key}, function (response) {
                                var build = response.data;
                                if (!build.interviewId) {
                                    getInterviewBuildN(key, N + 1, successCallback, errorCallback);
                                } else {
                                    successCallback(build);
                                }
                            }, errorCallback);
                        },
                        500);
                }


            };

            instance.safeGetInterviewBuildResults = function (key, successCallback, errorCallback) {
                getInterviewBuildN(key, 0, successCallback, errorCallback);
            };

            var getInterviewCategoryListBuildN = function (key, N, successCallback, errorCallback) {
                if (N === 12) {//500 X 12 => 6 seconds
                    /*
                     * Generate a synthetic timeout error
                     */
                    var timeout = {};
                    timeout.code = 1;
                    timeout.message = "Timed out after 6 seconds";
                    errorCallback(timeout);
                } else {
                    setTimeout(
                        function () {
                            categoryListBuild.get({id:key}, function (response) {
                                var build = response.data;
                                if (!build.categoryListId) {
                                    getInterviewCategoryListBuildN(key, N + 1, successCallback, errorCallback);
                                } else {
                                    successCallback(build);
                                }
                            }, errorCallback);
                        },
                        500);
                }


            };

            instance.safeGetInterviewCategoryListBuildResults = function (key, successCallback, errorCallback) {
                getInterviewCategoryListBuildN(key, 0, successCallback, errorCallback);
            };

            return instance;
        })();

    }])/* End InterviewBuilder*/


    /**
     * Interview API
     */
        .factory('Interview', ['Resources', function (Resources) {

        return (function () {
            var interview = Resources.get('interview');
            var interviewForReviewer = Resources.get('reviewer-interview');
            var instance = {};

            instance.list = function (params, successCallback, errorCallback) {
                interview.get(params, function (response) {
                    successCallback(response.data);
                }, errorCallback);
            };

            instance.get = function (id, successCallback, errorCallback) {
                interview.get({id:id}, function (response) {
                    successCallback(response.data);
                }, errorCallback);
            };

            instance.getForReview = function (id, reviewKey, successCallback, errorCallback) {
                interviewForReviewer.get({id:id, reviewKey: reviewKey}, function (response) {
                    successCallback(response.data);
                }, errorCallback);
            };

            instance.update = function (data, successCallback, errorCallback) {
                interview.update(
                    data,
                    function (response) {
                        if (successCallback) {
                            successCallback(response.data);
                        }
                    }, errorCallback
                );
            };


            return instance;
        })();

    }])/* End Interview*/


    /**
     * Interview Review API
     */
        .factory('InterviewReview', ['Resources', '$http', function (Resources, $http) {

            return (function () {
                var svc = Resources.get('interview-review');
                var instance = {};

                instance.list = function (params, successCallback, errorCallback) {
                    svc.get(params, function (response) {
                        successCallback(response.data);
                    }, errorCallback);
                };

                instance.get = function (id, reviewKey, successCallback, errorCallback) {
                    svc.get({id:id, reviewKey: reviewKey}, function (response) {
                        successCallback(response.data);
                    }, errorCallback);
                };

                instance.save = function (data, successCallback, errorCallback) {
                    svc.save (
                        data,
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    );
                };

                instance.update = function (data, reviewKey, successCallback, errorCallback) {
                    svc.update(
                        $.extend({}, data, {reviewKey: reviewKey}),
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    );
                };

                instance.remove = function(id, reviewKey, successCallback, errorCallback) {
                    svc.remove({id:id, reviewKey:reviewKey}, function(response) {
                        successCallback(response.data);
                    }, errorCallback);
                };

                // Re-send the review request email
                instance.resendEmail = function(id, successCallback, errorCallback) {
                    var url = "/interview/review/" + id + "/resend";
                    $http.get(url);
                }


                return instance;
            })();

        }])/* End Interview Review*/

    /**
     * Question Review API
     */
        .factory('QuestionReview', ['Resources', function (Resources) {

            return (function () {
                var svc = Resources.get('questionreview');
                var instance = {};
                instance.update = function (data, reviewKey, successCallback, errorCallback) {
                    svc.update(
                        $.extend({}, data, {reviewKey: reviewKey}),
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    );
                };

                instance.save = function (data, reviewKey, successCallback, errorCallback) {
                    svc.save(
                        $.extend({}, data, {reviewKey: reviewKey}),
                        function (response) {
                            if (successCallback) {
                                successCallback(response.data);
                            }
                        }, errorCallback
                    );
                };


                return instance;
            })();

        }])/* End Interview Review*/



    /**
     * Question API
     */
        .factory('Question', ['Resources', '$rootScope', function (Resources, scope) {

        return (function () {
            var question = {};
            var q = Resources.get('question');
            var qv = Resources.get('questionVideo');

            question.update = function (data, successCallback, errorCallback) {
                q.update(
                    data,
                    function (response) {
                        if (successCallback) {
                            successCallback(response.data);
                        }
                    }, errorCallback
                );
            };

            question.waitForVideo = function (questionId, successCallback, errorCallback) {
                qv.get({id:questionId},
                    function (response) {
                        if (successCallback) {
                            successCallback(response.data);
                        }
                    }, errorCallback
                );
            };

            question.removeVideo = function (questionId, successCallback, errorCallback) {
                qv.remove({id:questionId},
                    function (response) {
                        if (successCallback) {
                            successCallback(response.data);
                        }
                    }, errorCallback
                );
            };

            return question;
        })();

    }])/* End Question*/

    /**
     * Forgot Password API
     */
        .factory('ForgotPasswordRequest', ['Resources', '$rootScope', function( Resources, scope) {
        return (function() {
            var password = {};
            var p = Resources.get('forgotpasswordrequest');

            password.resetPassword = function (email, successCallback, errorCallback) {
                p.update(
                    {email:email},
                    function (response){
                        if (successCallback) {
                            successCallback(response.data);
                        }
                    }, errorCallback
                );
            };
            return password;
        })();

    }]) /*End ForgotPassword*/

    /**
     * Forgot Password API
     */
        .factory('ForgotPassword', ['Resources', '$rootScope', function( Resources, scope){
        return (function() {
            var pass = {};
            var p = Resources.get('forgotpassword');

            pass.saveAccount = function (user, successCallback, errorCallback) {
                p.update(
                    {email:user.email, temp:user.temp, password:user.password, confirm:user.confirm},
                    function (response) {
                        if (successCallback) {
                            successCallback(response.data);
                        }
                    }, errorCallback
                );
            };
            return pass;
        })();
     }])


    /**
     * JobMatch API
     */
        .factory('JobMatch', ['Resources', '$rootScope', function (Resources, scope) {


        return (function (id) {
            var m = Resources.get('jobmatch');
            var match = {};

            match.findBySearchTerm = function (searchTerm, successCallback, errorCallback) {
                m.get({searchTerm:searchTerm}, function (response) {
                    successCallback(response.data);
                }, errorCallback);
            };

            return match;
        })();

    }])/* End JobMatch*/

    ;

})(window, window.angular);
