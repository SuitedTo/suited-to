(function (window, app, undefined) {
    'use strict';

    /**
     * Directive for Typeahead.js
     */
    app.directive('twTypeahead', [ function () {

        var link = function(scope, element, attrs) {

            //$(element).typeahead(scope.$eval(attrs.twTypeahead));
            $(element).on('change', function (item) {
                var val = $(this).val();
                console.log($(this).val());
                //var data = $('.tt-is-under-cursor').children().filter(function (index) {
                //    return $(this).attr("name") == "data";
                //}).val();
                //var data = $('.createInterviewSearch .tt-suggestion[data-value="'+$(this).val()+'"]').find('.hiddenData').val();
                var data = $('.createInterviewSearch .tt-suggestion[data-value]').filter(function(index){
                    return item = $(this).data('value').toLowerCase() == val.toLowerCase();
                }).find('.hiddenData').val();
                if(data){
                    scope[attrs.typeaheadcallback](data);
                }
            });
            $(element).on('blur',function(item){
                $(this).change();
            });

            //$('.createInterviewSearch .tt-suggestion').on('click',function(){
            //    $(element).val($(this).data('value'));
            //});

            $(element).typeahead([
                {
                    name: 'repos',
                    //remote: '../data/films/queries/%QUERY.json',
                    //remote: '/public/partials/prep/repos.json?query=%QUERY'
                    remote: scope[attrs.typeaheadsrc],
                    limit: 5,
                    template: '<span class="resultType"><%= type %></span><span class="resultName" data-val="<%= data %>"><%= name %> <span class="resultHint"><%= hint %></span></span><input class="hiddenData" name="data" type="hidden" value="<%= data %>"/>',
                    engine: {
                        compile: function (template) {
                            var tmplcache = {};
                            return {
                                render: function tmpl(data) {
                                    // Template by John Resig: http://ejohn.org/blog/javascript-micro-templating/
                                    // Figure out if we're getting a template, or if we need to
                                    // load the template - and be sure to cache the result.
                                    var fn = !/\W/.test(template) ?
                                        tmplcache[template] = tmplcache[template] ||
                                            tmpl(document.getElementById(template).innerHTML) :

                                        // Generate a reusable function that will serve as a template
                                        // generator (and which will be cached).
                                        new Function("obj",
                                            "var p=[],print=function(){p.push.apply(p,arguments);};" +

                                                // Introduce the data as local variables using with(){}
                                                "with(obj){p.push('" +

                                                // Convert the template into pure JavaScript
                                                template
                                                    .replace(/[\r\t\n]/g, " ")
                                                    .split("<%").join("\t")
                                                    .replace(/((^|%>)[^\t]*)'/g, "$1\r")
                                                    .replace(/\t=(.*?)%>/g, "',$1,'")
                                                    .split("\t").join("');")
                                                    .split("%>").join("p.push('")
                                                    .split("\r").join("\\'")
                                                + "');}return p.join('');");

                                    // Provide some basic currying to the user
                                    return data ? fn(data) : fn;
                                }
                            };
                        }
                    } // end engine

                }
            ]); // end typeahead
        };

        return {
            // Restrict it to be an attribute in this case
            restrict: 'A',
            // responsible for registering DOM listeners as well as updating the DOM
            link: link
        };
    }])


    /**
     * Directive for show/hide using jQuery SlideUp and SlideDown
     */
        .directive('ppShowSlide', [ function() {

            // I allow an instance of the directive to be hooked
            // into the user-interaction model outside of the
            // AngularJS context.

            function link($scope, element, attributes) {

                // I am the TRUTHY expression to watch.
                var expression = attributes.ppShowSlide;

                // I am the optional slide duration.
                var duration = (attributes.slideShowDuration || "fast");


                // I check to see the default display of the
                // element based on the link-time value of the
                // model we are watching.
                if (!$scope.$eval(expression)) {

                    element.hide();

                }


                // I watch the expression in $scope context to
                // see when it changes - and adjust the visibility
                // of the element accordingly.
                $scope.$watch(
                    expression,

                    function(newValue, oldValue) {

                        // Ignore first-run values since we've
                        // already defaulted the element state.
                        if (newValue === oldValue) {

                            return;

                        }

                        // Show element.
                        if (newValue) {

                            element.stop(true, true)
                                .slideDown(duration);

                            // Hide element.
                        } else {

                            element.stop(true, true)
                                .slideUp(duration);

                        }

                    });

            }


            // Return the directive configuration.
            return ({
                link: link,
                restrict: "A"
            });

        }])

        .directive('bsLoadingText', [ function() {
            // I allow an instance of the directive to be hooked
            // into the user-interaction model outside of the
            // AngularJS context.

            function link($scope, element, attributes) {
                // I am the TRUTHY expression to watch.
                var expression = attributes.bsLoadingText;

                // I check to see the default display of the
                // element based on the link-time value of the
                // model we are watching.
                if (!$scope.$eval(expression)) {
                    element.button('reset');
                }


                // I watch the expression in $scope context to
                // see when it changes - and adjust the visibility
                // of the element accordingly.
                $scope.$watch(
                    expression,
                    function(newValue, oldValue) {

                        // Ignore first-run values since we've
                        // already defaulted the element state.
                        if (newValue === oldValue) {
                            return;
                        }

                        // Show element.
                        if (newValue) {
                            element.stop(true, true).button('loading');
                            // Hide element.
                        } else {
                            element.stop(true, true).button('reset');
                        }
                    });
            }

            // Return the directive configuration.
            return ({
                link: link,
                restrict: "A"
            });

        }])

    /**
     * Ex: <error name="ACCESS_ERROR"/>
     */
        .directive('error', ['$http', '$templateCache', '$compile', function ($http, $templateCache, $compile) {
            return {
                compile:function (element, attr) {
                    return function (scope, element, attr) {
                        var url = "/public/partials/prep/errors/" + attr.name + ".html";
                        $http.get(url, {cache:$templateCache}).success(function (html) {
                            var oldTag = element.context;

                            //need to wrap the new tag because we'll want to compile
                            //the new tag's parent contents and if two errors share
                            //the same parent then that same section gets compiled
                            //twice and bad things happen that I don't fully understand.
                            //So the wrapper just gives us an isolated piece of dom
                            // so that we don't compile the same piece twice.
                            var newTagWrapper = document.createElement("div");

                            var newTag = document.createElement("div");
                            newTag.setAttribute('ng-show','error.' + attr.name);
                            newTag.innerHTML = html;
                            newTagWrapper.appendChild(newTag);
                            oldTag.parentNode.replaceChild(newTagWrapper,oldTag);
                            $compile($(newTagWrapper).contents())(scope.$new());
                        })
                    }
                },
                restrict:"E"
            };

        }])

        .directive('dirtyCheck', function() {
            return {
                link: function($scope, elem, attrs) {

                    var msg = "Changes have been made. Are you sure you want to navigate away from this page?";
                    window.onbeforeunload = function(){
                        if ($scope[attrs.dirtyCheck]()) {
                            return msg;
                        }
                    }
                    $scope.$on('$locationChangeStart', function(event, next, current) {
                        if ($scope[attrs.dirtyCheck]()) {
                            if(!confirm(msg)) {
                                event.preventDefault();
                            }
                        }
                    });
                },
                restrict: "A"
            };
        })



    ;

})(window, window.app);