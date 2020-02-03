(function (window, app, undefined) {
    'use strict';

    /**
     * Factory for Facebook Login/Registration
     */
    app.factory('Facebook', ['PrepUser', function (PrepUser) {

        var service = {};

        /**
         * Creates a new user associated with a Facebook account
         * @param authResponse the authResponse from a call to Facebook's getLoginStatus or login functions
         * @param successCallback callback function to run upon successful registration
         */
        service.registerFacebookUser = function (authResponse, successCallback, errorCallback) {
            FB.api('/me?fields=first_name,email', function (response) {
                var user = {
                    externalAuthProviderId: authResponse.userID,
                    externalAuthProvider: 'FACEBOOK',
                    profilePictureUrl: 'http://graph.facebook.com/' + authResponse.userID + '/picture?type=large',
                    email: response.email,
                    firstName: response.first_name
                };
                //registration needs to happen inside this callback so that it waits on the email value
                PrepUser.register(user, successCallback, errorCallback);
            });
        };

        /**
         * Logs in a user associated with a Facebook account
         * @param authResponse the authResponse from a call to Facebook's getLoginStatus or login functions
         * @param successCallback callback function to run upon successful login
         */
        service.loginFacebookUser = function (authResponse, successCallback, errorCallback) {
            FB.api('/me?fields=email', function (apiResponse) {
                PrepUser.login({externalAuthProviderId: authResponse.userID, externalAuthProvider: 'FACEBOOK',
                    externalAuthProviderAccessToken:authResponse.accessToken,email: apiResponse.email},
                    successCallback, errorCallback);
            });
        };

        /**
         * Checks to see if a user is already logged into Facebook and initiates the Facebook login dialog if not
         * @param callback Callback function to run once we've confirmed that the user is logged in to facebook
         */
        service.loginIfNeeded = function (callback) {
            if(!window.fbAuth){
                FB.login(function (response) {
                    if (response.authResponse) {
                        window.fbAuth = response.authResponse;
                        callback(response.authResponse);
                    }
                });
            } else {
                callback(window.fbAuth);
            }
        };

        return service;
    }])


    /**
     * Factory for Google Login/Registration
     */
        .factory('Google', ['PrepUser', function (PrepUser) {
            var service = {};

            service.registerGoogleUser = function (successCallback, errorCallback) {
                gapi.auth.authorize(apiConfig.google, function () {

                    $.getJSON('https://www.googleapis.com/oauth2/v1/userinfo?access_token=' + gapi.auth.getToken().access_token,
                        function (data) {
                            var user = {
                                externalAuthProviderId: data.id,
                                externalAuthProvider: 'GOOGLE',
                                email: data.email,
                                firstName: data.given_name,
                                profilePictureUrl: data.picture
                            };
                            PrepUser.register(user, successCallback, errorCallback);
                        }
                    );
                });
            };

            service.loginGoogleUser = function (successCallback, errorCallback) {
                gapi.auth.authorize(apiConfig.google, function () {
                    var token = gapi.auth.getToken().access_token;
                    $.getJSON('https://www.googleapis.com/oauth2/v1/userinfo?access_token=' + token,
                        function (data) {
                            PrepUser.login({externalAuthProviderId: data.id, externalAuthProvider: 'GOOGLE', externalAuthProviderAccessToken: token, email: data.email},
                                successCallback,
                                errorCallback);
                        }
                    );
                });
            };

            return service;
        }])

    /**
     * Recording service
     */
        .factory('RecordingService', ['$timeout', function ($timeout) {
            var service = {};




            /**
             *
             * @param videoElement - the dom video element
             *
             * @param options:
             *
             * recordTime - Will record for this many seconds and then stop automatically
             * timerCallback - Called once per second with formatted elapsed time
             * countdownCallback - Called once per second with formatted countdown time
             * expiredCallback - Called after the recorder has been forced to stop because time expired
             * errorCallback - Called on error - usually when the user denies access to the web cam.
             */
            service.createRecorder = function (videoElement, options) {

                    var WebCam = function(videoElement, options){
                        this.videoElement = videoElement;
                        this.options = options;

                        var self = this;

                        function getNewMediaStream(callback, errorCallback){
                            navigator.getMedia = ( navigator.getUserMedia ||
                                navigator.webkitGetUserMedia ||
                                navigator.mozGetUserMedia ||
                                navigator.msGetUserMedia);

                            navigator.getMedia(
                                //constraints
                                {audio: true, video: false},

                                //success callback
                                function (stream) {
                                    if(callback)callback(stream);

                                },

                                //error callback
                                function (err) {
                                    if(errorCallback)errorCallback(err);
                                }
                            );
                        }

                        WebCam.prototype.isOpen = function(){
                            return self.stream != undefined;
                        }

                        WebCam.prototype.open = function(successCallback, errorCallback){
                            getNewMediaStream(
                                function(stream){
                                    //self.videoElement.src = URL.createObjectURL(stream);
                                    //self.videoElement.width = self.options.video.width;
                                    //self.videoElement.height = self.options.video.height;
                                    self.stream = stream;

                                    if(successCallback){
                                        successCallback(self);
                                    }
                                },
                                function(err){
                                    if(errorCallback){
                                        errorCallback(err);
                                    }
                                }
                            );
                        }

                        WebCam.prototype.close = function(){
                            if(self.isOpen()){
                                //self.videoElement.pause();
                                self.stream.stop();
                                self.stream = undefined;

                                var parent = self.videoElement.parentNode;
                                parent.removeChild(self.videoElement);
                                parent.appendChild(self.videoElement);
                            }
                        }
                    };




                    var Recorder = function(videoElement, options){

                        var webCam = new WebCam(videoElement,options.videoOptions);

                        var timerCallback = options.timerCallback;
                        var expiredCallback = options.expiredCallback;
                        var countdownCallback = options.countdownCallback;
                        var blobUrls = [];

                        var recordTimeLimit = options.recordTime || 300;
                        var recordedTime = 0;
                        var prettyRecordedTime = '';

                        syncPrettyRecordedTime();

                        this.video = [];
                        this.audio = [];

                        //In trusted mode the user won't be asked as often for access to the web cam but
                        //the web cam stays on and the light stays on even while not recording
                        this.trusted = false;


                        this.states = {
                            COUNTDOWN : "COUNTDOWN",
                            RECORDING : "RECORDING",
                            PAUSED : "PAUSED",
                            STOPPED : "STOPPED"
                        }

                        this.state = this.states.STOPPED;

                        var self = this;



                        function incrementRecordedTime(){
                            recordedTime++;
                            syncPrettyRecordedTime();
                            if(timerCallback) timerCallback(prettyRecordedTime);
                            if(self.state.localeCompare(self.states.RECORDING) === 0){
                                if(recordedTime < recordTimeLimit){
                                    $timeout(incrementRecordedTime,1000);
                                } else {
                                    self.stopRecording(expiredCallback)
                                }
                            }
                        }

                        function syncPrettyRecordedTime(){
                            var min = Math.floor((recordTimeLimit-recordedTime)/60);
                            var sec = (recordTimeLimit-recordedTime) - (min*60);
                            prettyRecordedTime = min + ":" + ([1e2]+sec).slice(-2); //format seconds
                        }

                        function getBlob(url, callback) {
                            var xhr = new XMLHttpRequest();
                            xhr.open('GET', url , true);
                            xhr.responseType = 'blob';
                            xhr.onreadystatechange = function(e) {
                                if (xhr.readyState == 4 ) {
                                    callback(null, {url:url, blob:this.response});
                                }
                            }
                            xhr.send();
                        }

                        Recorder.prototype.reset = function(){

                            var reset = function(){
                                recordedTime = 0;
                                syncPrettyRecordedTime();

                                for(var i = 0; i < blobUrls.length; ++i){
                                    URL.revokeObjectURL(blobUrls[i]);
                                }
                                blobUrls = [];
                                // don't need this until video goes live
                                //self.video = [];
                                self.audio = [];
                                self.state = self.states.STOPPED;
                            }

                            if(self.state.localeCompare(self.states.RECORDING) == 0){
                                self.stopRecording(reset);
                            } else {
                                reset();
                            }

                        }

                        Recorder.prototype.startRecording = function(delay, callback, errorCallback){
                            var start = function(){
                                self.recorder.startRecording();
                                self.state = self.states.RECORDING;
                                incrementRecordedTime();
                                if(callback) callback();
                            };

                            function startAfterDelay(){
                                if(delay > 0){
                                    self.state = self.states.COUNTDOWN;
                                    if(countdownCallback) countdownCallback(delay);
                                    delay--;
                                    $timeout(startAfterDelay,1000);
                                } else {
                                    start();
                                }
                            }

                            if(!webCam.isOpen()){
                                webCam.open(
                                    function(webCam){
                                        self.recorder = new RecordRTC(webCam.stream);
                                        startAfterDelay(delay);
                                }, function(err){
                                    if(errorCallback)errorCallback(err);
                                });
                            } else {
                                startAfterDelay(delay);
                            }
                        }

                        Recorder.prototype.stopRecording = function(callback){
                            self.recorder.stopRecording();

                            self.recorder.getDataURL(function (dataURL) {
                                // commenting out for now with the static image
                                //self.video.push(new Blob([blobs.video], {type: 'video/webm'}));
                                //blobUrls.push(dataURL.video);

                                self.audio.push(self.recorder.getBlob());
                                blobUrls.push(dataURL.audio);

                                if (!self.trusted) {
                                    webCam.close();
                                    self.recorder = undefined;
                                }

                                self.state = self.states.PAUSED;

                                if (callback) callback(self.state);
                            });
                        }

                    };



                    options.videoOptions = options.videoOptions ||
                    {
                        type: 'video',
                        video: {
                            width: 320,
                            height: 240
                        },
                        canvas: {
                            width: 320,
                            height: 240
                        }
                    };

                    return new Recorder(videoElement, options);
            };



            return service;
        }]);

})(window, window.app);
