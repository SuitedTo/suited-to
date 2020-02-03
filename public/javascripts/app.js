/**
 * SuitedTo Application Global JS
 *
 * Author: Prashant Katwa
 * http://app.suitedto.com
 * Copyright (c) 2012 Sparc LLC
 *
 */

var Application = window.Application || {};



/**
 * Application.Config - Global Config Settings
 *
 * Contain all global SuitedTo settings
 *
 */
Application.Config = Application.Config || (function() {
    var config = {
        path : {
            templates : '/public/templates/',
            reports : '/tables/getreport'
        }
    };
    return config;
})();



/**
 * Application.Global - Global Functions
 *
 * Contain all global SuitedTo functions
 *
 */
Application.Global = (function() {
    var fn = {};

    fn.init = function() {

        $(window).resize(function() {
            if(this.resizeTO) clearTimeout(this.resizeTO);
            this.resizeTO = setTimeout(function() {
                $(this).trigger('resizeEnd');
            }, 350);
        });

        $(document).ready(function(){

            // open all external links in new window
            $('a[rel*=external]').click(function(){
                window.open($(this).attr('href'));
                return false;
            });

            // Fix for bootstrap 2.1.1
            $('body').on('touchstart.dropdown', '.dropdown-menu', function (e) { e.stopPropagation(); });
            $('.navbar-togglebtn a').click(function(){
                $('.utilityNav').slideToggle('fast');
            });

            // Responsive Modals
            $('.modal').modalResponsiveFix({ spacing: 20/*, debug: true */});
            $('.modal').on('shown',function(){ $(window).trigger('resize.mrf'); });

            // Datables pagination
            $('body').on('click', 'div.dataTables_paginate a', function() {
                var targetOffset = $('table.dataTable').offset().top;
                $('html,body').animate({scrollTop: targetOffset}, 500);
                return false;
            } );

            // user badge popover
            $('.userBadgeIcon[rel="popover"]').popover({trigger:'hover'}).click(function(e){
                e.preventDefault();
                $(this).popover('toggle');
            });

            // load placeholder polyfill
            $('input, textarea').placeholder();

        });

        $(window).load(function(){

            // help icon popovers
            $('.help[rel="popover"]')
                .popover({trigger:'hover'})
                .click(function(e){
                    e.preventDefault();
                    $(this).popover('toggle');
                });
            // help icon popovers - dismiss on touch
            $('body').on('touchstart.popover', '.popover', function (e) {
                $(this).fadeOut('fast',function(){
                    $(this).remove();
                    $('.help[rel="popover"]').popover('destroy');
                });
            });

        });

        //CheckUserData available globally
        Application.CheckUserData.init();
    };

    return fn;

})();



/**
 * Application.Helpers - Helper Functions
 *
 * Contain global helper functions
 *
 */
Application.Helper = (function(){
    var fn = {};

    // http://ejohn.org/blog/javascript-pretty-date/
    fn.prettyDate = function(time){
        //var date = new Date((time || "").replace(/-/g,"/").replace(/[TZ]/g," ")),
        var date = new Date(time),
            diff = (((new Date()).getTime() - date.getTime()) / 1000),
            day_diff = Math.floor(diff / 86400);
                
        if ( isNaN(day_diff) || day_diff < 0 || day_diff >= 31 )
            return;
                
        return day_diff === 0 && (
                diff < 60 && "just now" ||
                diff < 120 && "1 minute ago" ||
                diff < 3600 && Math.floor( diff / 60 ) + " minutes ago" ||
                diff < 7200 && "1 hour ago" ||
                diff < 86400 && Math.floor( diff / 3600 ) + " hours ago") ||
            day_diff == 1 && "Yesterday" ||
            day_diff < 7 && day_diff + " days ago" ||
            day_diff < 14 && "1 week ago" ||
            day_diff < 31 && Math.ceil( day_diff / 7 ) + " weeks ago";
    };

    fn.setCookie = function(name,value,days) {
        if (days) {
            var date = new Date();
            date.setTime(date.getTime()+(days*24*60*60*1000));
            var expires = "; expires="+date.toGMTString();
        }
        else var expires = "";
        document.cookie = name+"="+value+expires+"; path=/";
    };

    fn.getCookie = function(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for(var i=0;i < ca.length;i++) {
            var c = ca[i];
            while (c.charAt(0)==' ') c = c.substring(1,c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
        }
        return null;
    };

    fn.deleteCookie = function(name) {
        fn.setCookie(name,"",-1);
    };

    fn.makeSafe = function(text) {
        return text.replace(/[&<>"'`]/g, function (chr) {
            return '&#' + chr.charCodeAt(0) + ';';
        });
    };

    return fn;

})();



/**
 * Application.Debug - Javascript Debugging
 *
 * Script to assist JavaScript debuging. Modifies console.log() to add datestamp and store to
 * window.log.history. Allows for disabling of console logging (ie. for Prod)
 *
 */
Application.Debug = (function() {
    var fn = {};
    var oldConsoleLog = null;
    var logActive = false;

    fn.init = function () {

        // http://paulirish.com/2009/log-a-lightweight-wrapper-for-consolelog/
        /*window.log = function(){
          log.history = log.history || [];   // store logs to an array for reference
          log.history.push(arguments);
          if(this.console){
            console.log( Array.prototype.slice.call(arguments) );
          }
        };*/

        // http://zsitro.com/custom-console-log-w-timestamps/
        /*window.log = function(){
            var now = new Date();
            var timestamp = now.getHours()+":"+now.getMinutes()+":"+now.getSeconds();
         
            log.history = log.history || [];   // store logs to an array for reference
            log.history.push(arguments);
            if(this.console){
                console.log(timestamp.toString()+' ', Array.prototype.slice.call(arguments) );
            }
        };*/

        // http://grenzgenial.com/post/2080765415/javascript-console-log-revamped-with-stacktrace
        if (typeof console !== "undefined") {
          console.logJack = console.log;
          window.log = {};
          window.log.history = window.log.history || {};   // store logs to a global history for reference
          console.log = function(){
            var timestamp = (new Date()); //create a timestamp of the log
            var millis = timestamp.getTime();
            var readableString = timestamp.toUTCString();
            var stack = new Error().stack || ''; //save a stacktrace for Google Chrome
            var array = Array.prototype.slice.call(arguments);
            var args = {argument:array, stackTrace:stack};
            log.history[millis] = args; //save everything to the log
            console.logJack(readableString, arguments, millis); //call the original log function with a timestamp
            if(logActive === true){
                //$.each(log.history[millis], fn.addToLogDisplay);
                fn.addToLogDisplay(millis,args);
            }
          };
        }

    };

    fn.addToLogDisplay = function (index,value) {
        var html = '';
        var trace = '';
        var tracehtml = '';
        var arghtml = '';

        // print_r function
        // original source: http://stackoverflow.com/questions/1766795/javascript-write-console-debug-output-to-browser
        // modified by Prashant
        function print_r(theObj){
            if(theObj.constructor == Array || theObj.constructor == Object){
                arghtml += "<ul>";
                for(var p in theObj){
                    if(theObj[p].constructor == Array || theObj[p].constructor == Object){
                        if (theObj[p] instanceof jQuery){
                            arghtml += "<li>["+p+"] => jQuery "+typeof(theObj)+"</li>";
                        } else {
                            arghtml += "<li>["+p+"] => "+typeof(theObj)+"</li>";
                        }
                        arghtml += "<ul>";
                        print_r(theObj[p]);
                        arghtml += "</ul>";
                    } else {
                        if (theObj[p] instanceof jQuery){
                            arghtml += "<li>["+p+"] => jQuery "+typeof(theObj)+" ["+theObj[p].selector+"]</li>";
                        } else {
                            arghtml += "<li>["+p+"] => "+theObj[p]+"</li>";
                        }
                    }
                }
                arghtml += "</ul>";
            }
        }
        print_r(value.argument);

        if (typeof value.stackTrace !== 'undefined' && value.stackTrace !== ''){ //if null, browser not supported
            trace = value.stackTrace.toString().replace(/\n\r?/g, '<br />');
            if (typeof value.stackTrace !== 'undefined' && value.stackTrace !== ''){
                trace = trace.replace(/\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;');
            }
            tracehtml = '<a class="debugTraceLink"><small>Show Stacktrace</small></a>' +
                '<div class="debugStacktrace" style="display:none;"><strong>Stacktrace:</strong> ' + trace + '</div>';
        }
        html += '<div class="alert debugLogggerItem">' +
                '<div class="debugTime"><strong>Time:</strong> ' + new Date(parseInt(index,10)).toUTCString() + '</div>' +
                '<div class="debugArgs"><strong>Arguments:</strong> ' + arghtml + '</div>' + tracehtml + '</div>';
        $('#debugLogger').append(html);
    };

    fn.showlog = function () {

        logActive = true;

        $('body').append('<div class="container"><br /><div id="debugLogger" class="row-fluid"></div></div>');

        $.each(log.history, fn.addToLogDisplay);

        $('body').on('click','#debugLogger .debugTraceLink', function(){
            $(this).hide().next().show();
        });

    };

    fn.enable = function () {
        if(oldConsoleLog == null){
            return;
        }

        window['console']['log'] = oldConsoleLog;
    };
            
    fn.disable = function () {
        oldConsoleLog = console.log;
        window['console']['log'] = function() {};
    };

    return {
        'init'      : fn.init,
        'showLog'   : fn.showlog,
        'enable'    : fn.enable,
        'disable'   : fn.disable
    };

})();

//Application.Debug.init();



/**
 * Application.Tmpl - Handlebars.js Templating for Sparc
 *
 * Script that enables easy use of Handlebars.js for templating. Some code for expanding
 * handlebars easy of use taken from ICanHandlebarz.js.
 *
 */
Application.Tmpl = (function(){
    /*
    * Ported from ICanHandlebarz.js
    * https://github.com/atomicobject/ICanHandlebarz.js
    * http://spin.atomicobject.com/2011/07/14/introducing-icanhandlebarz-js-icanhaz-js-meet-handlebars-js/
    */

    var fn = {},
        VERSION = "0.1",
        templates = {};

    fn.get = {};
    
    // public function for adding templates
    // We're enforcing uniqueness to avoid accidental template overwrites.
    // If you want a different template, it should have a different name.
    fn.addTemplate = function (name, templateString) {
        //console.log(fn.get[name]);
        if (templates[name]) throw "Template \" + name + \" exists";
        
        templates[name] = Handlebars.compile(templateString);
        fn.get[name] = function (data, raw) {
            data = data || {};
            var result = templates[name](data);
            return raw? result: $(result);
        };
    };
    
    // public function for adding partials
    fn.addPartial = function (name, templateString) {
        if (Handlebars.partials[name]) throw "Partial \" + name + \" exists";
        Handlebars.registerPartial(name, templateString);
    };

    fn.addHelper = function (name, func, args) {
        if (Handlebars.helpers[name]) throw "Helper \" + name + \" exists";
        if (typeof func === 'function') {
            Handlebars.registerHelper(name, func);
        } else {
            Handlebars.registerHelper(name, new Function(args, func));
        }
    };

    fn.helpers = function () {

        // http://doginthehat.com.au/2012/02/comparison-block-helper-for-handlebars-templates/
        Application.Tmpl.addHelper('compare', function(lvalue, rvalue, options) {

            if (arguments.length < 3)
                throw new Error("Handlerbars Helper 'compare' needs 2 parameters");

            operator = options.hash.operator || "==";

            var operators = {
                '==':       function(l,r) { return l == r; },
                '===':      function(l,r) { return l === r; },
                '!=':       function(l,r) { return l != r; },
                '<':        function(l,r) { return l < r; },
                '>':        function(l,r) { return l > r; },
                '<=':       function(l,r) { return l <= r; },
                '>=':       function(l,r) { return l >= r; },
                'typeof':   function(l,r) { return typeof l == r; }
            };

            if (!operators[operator])
                throw new Error("Handlerbars Helper 'compare' doesn't know the operator "+operator);

            var result = operators[operator](lvalue,rvalue);

            if( result ) {
                return options.fn(this);
            } else {
                return options.inverse(this);
            }

        });

    };
    
    // grabs templates from the DOM and caches them.
    // Loop through and add templates.
    // Whitespace at beginning and end of all templates inside <script> tags will
    // be trimmed. If you want whitespace around a partial, add it in the parent,
    // not the partial. Or do it explicitly using <br/> or &nbsp;
    fn.grabTemplates = function (data) {

        $('script[type="text/html"],script[type="text/x-handlebars-template"]').each(function (a, b) {
            var script = $((typeof a === 'number') ? b : a), // Zepto doesn't bind this
                text = (''.trim) ? script.html().trim() : $.trim(script.html());
            
            if (script.hasClass('partial')) {
                fn.addPartial(script.attr('id'), text);
            } else if (script.hasClass('helper')) {
                // Does this even work?
                fn.addHelper(script.attr('id'), text, script.attr('data-args'));
            } else {
                fn.addTemplate(script.attr('id'), text);
            }
            script.remove();
        });

        fn.helpers();

    };

    fn.loadTemplate = function (file, callback) {

       $.ajax({
            url: Application.Config.path.templates+file,
            type: "GET",
            dataType: "html",
            success: function(data, textStatus, jqXHR){
                $('body').append($(data));
                fn.grabTemplates();
                callback(data, textStatus, jqXHR);
            }
        });

    };
    
    // clears all retrieval functions and empties caches
    fn.clearAll = function () {
        for (var key in templates) {
            delete templates[key];
        }
        templates = {};
        Handlebars.partials = {};
    };
    
    fn.refresh = function () {
        fn.clearAll();
        fn.grabTemplates();
    };

    return fn;

})();



/**
 * Application.DataDisplay - Data handling and display alternative for DataTables
 *
 * Script that provides an alternate frontend framework allowing for custom views an data
 * handling while maintaining the same url and params bases structure of DataTables
 *
 * Dependencies:
 *  #{set 'moreScripts'}
 *      #{script 'handlebars-1.0.rc.1.min.js' /}
 *  #{/set}
 *
 */
Application.DataDisplay = (function(){

    var fn = {},
        opts = {}, pendingSearch;

    opts.prefs = {

        showLength      : true,
        showFilters     : true,
        showSortBy      : true,
        showSearch      : true,
        showPagination  : true,
        showDisplayType : true,
        wrapControls    : null,
        wrapData        : null

    };

    opts.vals = {

        url             : Application.Config.path.reports,
        id              : 'dataDisplayContainer',
        callback        : null,
        matchingCount   : 0

    };

    opts.params = {
        reportName: "",
        filters: "",
        sortColumn: 0,
        ascending: true,
        startIndex: 0,
        runLength: 12,
        searchString: ""
    };
    
    fn.init = function (userOptions){

        $.extend(opts.prefs, userOptions);

    };

    fn.ajax = function (callback) {

        $.ajax({
            url: opts.vals.url,
            data: opts.params,
            type: "GET",
            success: function(data, textStatus, jqXHR){
                if (typeof data.matchingCount !== 'undefined'){
                    opts.vals.matchingCount = parseInt(data.matchingCount,10);
                }
                if (typeof callback !== 'undefined' && callback !== null){
                    callback(data);
                } else {
                    opts.vals.callback(data);
                }

                if (opts.prefs.showPagination === true){
                    fn.paging();
                }
            }
        });

    };

    fn.load = function (url, params, id, callback) {

        if (url !== null){
            opts.vals.url = url;
        }
        if (params !== null){
            $.extend(opts.params, params);
        }
        if (id !== null){
            opts.vals.id = id;
        }
        if (callback !== null){
            opts.vals.callback = callback;
        }

        fn.controls(opts.vals.id);

        fn.ajax(callback);

    };

    fn.reload = function (params, callback) {

        if (params !== null){

            if(params instanceof Array){
                /**
                    Not using a for each loop because of IE compatability issues
                **/
                for(i = params.length - 1; i >= 0; i--) {
                    $.extend(opts.params, params[i]);
                }
            }
            else{
                $.extend(opts.params, params);
            }
        }

        //console.log('dataDisplay.reload', opts.params);

        fn.ajax(callback);
 
    };
    
    var addPendingSearch = function(that){
    
    	pendingSearch = setTimeout(function(){
    		var val = $(that).val();

        	//console.log("DataDisplay Control - Search", val);

        	fn.reload({'searchString':val,'startIndex':0});
        }, 500);
    }

    fn.controls = function (elm){
        //console.log('dataDisplay control element', elm);

        $elm = $(elm);

        var addElm;

        if(opts.prefs.wrapControls !== null){
            addElm = opts.prefs.wrapControls;
        } else {
            elm.prepend('<div class="dataDisplayControls"></div>');
            addElm = $('.dataDisplayControls',elm);
        }

        var createElm = function(id, markup) {

            if ($('.'+id,elm).is(':empty')) {
               $('.'+id,elm).append(markup);
            } else if ($('.'+id,elm).length === 0){
                addElm.append('<div class="'+id+'">' + markup + '</div>');
                //console.log('createElm','1')
            }
        };

        //
        //  Results Control: Display
        //
        if (opts.prefs.showDisplayType === true) {

            var ctl_display = 'dataDisplayCtrl_display';
            var ctl_display_elm = '<div class="btn-toolbar"><div class="btn-group">' +
                '<a class="btn displayGrid" href="#"><i class="icon-th"></i></a>' +
                '<a class="btn displayList" href="#"><i class="icon-th-list"></i></a>' +
                '</div></div>';

            createElm(ctl_display,ctl_display_elm);

            $('.'+ctl_display+' .displayList',elm).click(function(e){
                e.preventDefault();
                $('.dataDisplayResults',elm).removeClass('grid');
                $('.dataDisplayResults',elm).addClass('list');
                $('.'+ctl_display+' a',elm).removeClass('active');
                $(this).addClass('active');
            });
            $('.'+ctl_display+' .displayGrid',elm).click(function(e){
                e.preventDefault();
                $('.dataDisplayResults',elm).removeClass('list');
                $('.dataDisplayResults',elm).addClass('grid');
                $('.'+ctl_display+' a',elm).removeClass('active');
                $(this).addClass('active');
            });

            if ($('.dataDisplayResults',elm).hasClass('list')){
                $('.'+ctl_display+' .displayList',elm).addClass('active');
            } else {
                $('.'+ctl_display+' .displayGrid',elm).addClass('active');
            }

        }


        //
        //  Results Control: Length
        //
        if (opts.prefs.showLength === true){

            var ctl_length = 'dataDisplayCtrl_length';
            var ctl_length_elm = '<label>Show <select size="1" name="'+ctl_length+'_select" id="'+ctl_length+'_select" aria-controls="'+elm+'" class="span1 inline">' +
                '<option value="12" selected="selected">12</option>' +
                '<option value="24">24</option>' +
                '<option value="48">48</option>' +
                '<option value="96">96</option>' +
                '</select> entries</label>';

            createElm(ctl_length,ctl_length_elm);

            $('#'+ctl_length+'_select',elm).change(function(){
                var val = parseInt($(this).val(),10);

                var id = $(this).attr('id');
                //console.log("DataDisplay Control - Length",id, val);

                fn.reload({'runLength':val,'startIndex':0});
            });

        }


        //
        //  Results Control: Search
        //
        if (opts.prefs.showSearch === true){

            var ctl_search = 'dataDisplayCtrl_search';
            var ctl_search_input = 'dataDisplayCtrl_search_input';
            var ctl_search_elm = '<label for="'+ctl_search+'_input" class="hide">Search:</label> <input type="text" class="'+ctl_search_input+' span4" aria-controls="'+elm+'" placeholder="Search..."> <a href="#" class="clearSearch"><i class="icon-remove-sign"></i></a>';

            createElm(ctl_search,ctl_search_elm);

            $('.'+ctl_search_input,elm).keyup(function(){
            
            	if(pendingSearch){
            		clearTimeout(pendingSearch);
            	}
                addPendingSearch(this);
            });

            $('.clearSearch',elm).click(function(e){
                e.preventDefault();
                if (!!$('.'+ctl_search_input,elm).val()){
                    $('.'+ctl_search_input,elm).val('').keyup();
                }
            });

        }


        //
        // Results Control: Pagination
        //
        if (opts.prefs.showPagination === true){

            var ctl_paginate = 'dataDisplayCtrl_paginate';
            var ctl_paginate_elm = '<div class="">' +
                '<div class="paginationLeft pull-left"><span class="goto"></span> <span class="pageNumber"></span></div>' +
                '<div class="pagination pull-right"><ul><li class="prev disabled"><a href="#">&larr; <span>Prev</span></a></li><li class="next disabled"><a href="#"><span>Next</span> &rarr;</a></li></ul></div>' +
                '</div>';

            createElm(ctl_paginate,ctl_paginate_elm);

            $('.dataDisplayCtrl_paginate .next a',elm).click(function(e){
                e.preventDefault();
                if(!$(this).parent().hasClass('disabled')){
                    var newVal = parseInt(opts.params.startIndex,10)+parseInt(opts.params.runLength,10);
                    fn.reload({'startIndex':newVal});
                }
                
            });
            $('.dataDisplayCtrl_paginate .prev a',elm).click(function(e){
                e.preventDefault();
                if(!$(this).parent().hasClass('disabled')){
                    var newVal = parseInt(opts.params.startIndex,10)-parseInt(opts.params.runLength,10);
                    fn.reload({'startIndex':newVal});
                }
            });

            var select = '<select class="goto_select"><option value="">Go to</option></select>';
            $('.dataDisplayCtrl_paginate .goto',opts.vals.id).append(select);

            $('.dataDisplayCtrl_paginate .goto_select',elm).change(function(){
                var val = $(this).val();
                var newVal = Math.ceil( (parseInt(val,10)*parseInt(opts.params.runLength,10))-parseInt(opts.params.runLength,10) );
                fn.reload({'startIndex':newVal});
            });

        }

    };

    fn.paging = function (){

        //console.log(opts.vals.matchingCount, 0, opts.params.startIndex, opts.params.runLength);

        opts.vals.currPage = Math.ceil( (opts.params.startIndex+opts.params.runLength)/opts.params.runLength );
        opts.vals.totalPages = Math.ceil( opts.vals.matchingCount / opts.params.runLength );

        //console.log(opts.vals.currPage,opts.vals.totalPages);

        if (opts.vals.matchingCount > 0 && opts.vals.matchingCount > opts.params.runLength) {
            $('.dataDisplayCtrl_paginate .next',opts.vals.id).removeClass('disabled');
            $('.dataDisplayCtrl_paginate .prev',opts.vals.id).removeClass('disabled');
        }

        if (opts.vals.currPage == 1){
            $('.dataDisplayCtrl_paginate .prev',opts.vals.id).addClass('disabled');
        }
        if (opts.vals.currPage == opts.vals.totalPages){
            $('.dataDisplayCtrl_paginate .next',opts.vals.id).addClass('disabled');
        }

        $('.dataDisplayCtrl_paginate .pageNumber',opts.vals.id).html('Page '+opts.vals.currPage+' of '+opts.vals.totalPages);

        var options = '<option value="">Go to</option>';
        for (var i=0;i<opts.vals.totalPages;i++){
            options += '<option value="'+(i+1)+'">'+(i+1)+'</option>';
        }

        $('.dataDisplayCtrl_paginate .goto_select',opts.vals.id).html(options);

    };

    return {
        'init'      : fn.init,
        'load'      : fn.load,
        'reload'    : fn.reload,
        'opts'      : opts
    };


})();


/**
 * Handle the collection of additional required user data when user clicks an element with checkUserData class.
 * Initialized via Application.Global but if no elements with checkUserData class are present in the DOM no eventHandlers
 * will be registered.
 *
 * Dependencies:
 * #{set 'moreScripts'}
 *      #{script 'jquery.form.js'/}
 *      #{script 'jquery.validate.min.js'/}
 *  #{/set}
 *
 */
Application.CheckUserData = (function(){
    var fn = {};

    var handlers = {
        /**
         * When dom elements with checkUserData class are clicked check to see if additional user data is required before
         * continuing with the default action
         * @param event The Click event
         */
        handleCheckUserDataClicks: function (event) {
            event.preventDefault();
            var action = $(this).data('action');
            $.ajax({
                url: Application.Config.path.actions.action_checkUserData({intendedAction: action}),
                context: event,
                success: handlers.checkUserDataCallback
            });
        },

        /**
         * Handle the response from checkUserData. If the response contains a non-empty data array of fieldNames then show
         * the modal to collect additional user data, otherwise just submit the original form.
         * @param data AJAX response expected {data: []}
         */
        checkUserDataCallback: function (responseData) {
            //this assumes that the checkUserData was the result of something being clicked to submit a form
            var originalForm = $(this.currentTarget).closest('form');
            var fields = responseData.data.fields;
            if (fields.length > 0) {
                //show the form fields
                for(var i=0; i < fields.length; i++){
                    $(('.control-group.' + fields[i])).show();
                }
                $('#submitUserDataBtn').data('original-form', originalForm.attr('id'));
                var intendedAction = responseData.data.action;
                if(intendedAction === 'question'){
                    $('#checkUserDataHeader').html('Add a Question');
                    $('#checkUserDataMessage').html('Add a display name to earn Street Cred for your question!');
                } else if(intendedAction === 'invitation'){
                    $('#checkUserDataHeader').html('Invite New Users');
                    $('#checkUserDataMessage').html('Who is sending the invitation?');
                }

                $('#checkUserDataModal').modal('show');
            } else {
                originalForm.submit();
            }
        },

        /**
         * Handle submission of the userDataForm via ajax if the client-side validation passes
         * @param form the userDataForm
         */
        userDataFormSubmitHandler: function (form) {
            $(form).ajaxSubmit({
                success: function (responseText) {
                    var errors = responseText.errors;
                    if(errors){
                        var errorElement = $('#userDataErrors')
                        for(var i = 0; i < errors.length; i++){
                            errorElement.append('<p>' + errors[i] + '</p>');
                        }
                        errorElement.show();
                    } else {
                        $(('#' + $('#submitUserDataBtn').data('original-form'))).submit();
                    }
                }
            })
        }
    };





    fn.init = function () {


        /**
         * Initialize all the user data related elements
         */
        $(document).ready(function(){
            var checkUserDataElements = $('.checkUserData');
            if(checkUserDataElements.length > 0){
                //check for additional user data
                checkUserDataElements.click(handlers.handleCheckUserDataClicks)

                $('#userDataForm').validate({
                    submitHandler: handlers.userDataFormSubmitHandler
                });
            }
        })
    }

    return fn;
})();



/**
 * Application.Community - Data display of Community Users List
 *
 * Page: Community/community
 *
 */
Application.Community = (function(){

    var fn = {};

    fn.init = function(){
        /**
           I've hardcoded in includeByDisplayNameExistence as a default filter.
           I'll talk to prashant asap about implementing method of
           specifying default filters in app.DataDisplay.
        **/

        fn.getData();

        $('#communityList #communityListCompanyOnly').click(function(){
            var compid = $(this).attr('data-compid');
            var id = $(this).attr('id');
            var params = {'filters':'includeByDisplayNameExistence:$true'};

            if ($(this).is(':checked')){
                params = {'filters':'includeByCompany:$myCompany,includeByDisplayNameExistence:$true'};
                //console.log("DataDisplay Control - CompanyOnly",id, params);
                Application.DataDisplay.reload(params);
            } else {
                //console.log("DataDisplay Control - CompanyOnly",id, params);
                Application.DataDisplay.reload(params);
            }
        });

        $('#communityList #dataDisplayCtrl_filter_select').change(function(){
            var val = $(this).val();

            var id = $(this).attr('id');
            //console.log("DataDisplay Control - Filter",id, val);

            var paramsArray = [];
            paramsArray.push({'sortColumn':val});

            // Ascending sort order for displayName
            if(val == 1) {
                paramsArray.push({'ascending': true});
            }
            else {
                 paramsArray.push({'ascending': false});
            }

            Application.DataDisplay.reload(paramsArray);
        });
    };

    fn.getData = function(){

        Application.Tmpl.loadTemplate('templates.html',function(data, textStatus, jqXHR){
            
            var requestData = {
                reportName: "user.PublicUserTable",
                filters: "includeByDisplayNameExistence:$true",
                sortColumn: 2,
                ascending: false,
                startIndex: 0,
                runLength: 12,
                searchString: ""
            };

            Application.DataDisplay.init({
                showLength      : true,
                showFilters     : true,
                showSortBy      : true,
                showSearch      : true,
                showPagination  : true,
                wrapControls    : $('#dataDisplayControls'),
                wrapData        : null
            });
            // Load DataDisplay (@sourceUrl, @params, @wrapperSelector, @callbackFn)
            //Application.DataDisplay.load("@{Tables.getReport()}", requestData, '#communityList', Application.Community.templatize);
            Application.DataDisplay.load(null, requestData, '#communityList', Application.Community.templatize);

        });
        
    };

    fn.templatize = function(data){
        if (data.matchingCount === 0){
            $('.dataDisplayResults').html('<div class="span12"><h4>Hmm... No matching results.</h4></div>');
        } else {
            var result = {};
            result.data = $.extend({}, data.data);
            //console.log(result);
            $('.dataDisplayResults').html(Application.Tmpl.get.tmpl_userCard(result));
        }
    };

    return {
        'init'          : fn.init,
        'templatize'    : fn.templatize
    };

})();



/**
 * Application.Dashboard - User Dashboard scripts
 *
 * Page: application/home
 *
 */
Application.Dashboard = (function(){

    /*
    *   Module Variables
    */
    var fn = {};

    /*
    *   Initialize Dashboard
    */
    fn.init = function(actions){

        fn.events(actions);
        fn.getActivity();
        //Application.Tmpl.grabTemplates();

    };

    /*
    *   Dashboard events - triggers, clicks
    */
    fn.events = function(actions){

        // Accordion click function
        var mainH2click = function(e){
            var div = $(this).next();
            //console.log(div.is(':visible'));
            if (div.is(':visible')){
                div.slideUp(function(){
                    $('body').trigger('columnResize');
                });
                $('i',this).addClass('icon-chevron-left').removeClass('icon-chevron-down');
            } else {
                div.slideDown(function(){
                    $('body').trigger('columnResize');
                });
                $('i',this).addClass('icon-chevron-down').removeClass('icon-chevron-left');
            }
        };
        
        // Accordions for Mobile
        if ($(window).width() < 768){
            $('.mainColWrap .block h2').prepend('<i class="icon-chevron-down icon-white pull-right" style="cursor:pointer;"></i>').addClass('mainH2');
            $('.mainH2').addClass('accordion').css('cursor','pointer');
            $('.mainH2').next().addClass('hide');
            $('.mainH2 i').addClass('icon-chevron-left').removeClass('icon-chevron-down');
            $('.mainH2').on('click',mainH2click);
        }

        // Trigger for dashboard column resize
        $('body').on('columnResize', function(e) {
            $('.leftCol, .midColWrap, .rightCol').removeAttr('style');
            var colHeight;
            if ($(window).width() > 979){
                colHeight = Math.max.apply(null, $('.leftCol, .midColWrap').map(function() {
                    return $(this).height();
                }).get());
                $('.leftCol, .midColWrap').css('min-height',colHeight);
                $('.rightCol').css('height',colHeight);
            } else if ($(window).width() > 767) {
                colHeight = Math.max.apply(null, $('.midColWrap').map(function() {
                    return $(this).height();
                }).get());
                $('.midColWrap').css('min-height',colHeight);
                $('.rightCol').css('height',colHeight);
            } else {
                $('.leftCol, .midColWrap, .rightCol').removeAttr('style');
            }
        });

        // On Window Load
        $(window).load(function(){
            $('body').trigger('columnResize');
        });

        // On Window Resize
        $(window).resize(function(){
            $('body').trigger('columnResize');
        });

        // Dismiss Notifications
        $('.notificationsList .close').click(function() {
            if (typeof actions.action_notifications_delete !== 'undefined'){
                var action = actions.action_notifications_delete;
                var element = $(this);
                $.ajax({
                    url: action({id: parseInt(this.id.substring(7),10)}),
                    success : function(data) {
                        element.parents("li").hide("slow");
                        if(parseInt(data,10) === 0){
                            $("#noNotifications").removeClass("hide");
                        }
                    }
                });
            }
            
        });

        // Hide more than 4 notifications
        if ($('.notificationsList li').length > 4) {
            $('.notificationsList li:gt(3):not("#noNotifications")').addClass('hide');
            $('.notificationsList').after('<div class="moreNotifications"><a href="#" class="showMoreNotifications">- Show More -</a><a href="#" class="hideNotifications hide">- Hide -</a></div>');
            $('.showMoreNotifications').on('click', function(e){
                e.preventDefault();
                $('.notificationsList li:not("#noNotifications")').removeClass('hide');
                $(this).addClass('hide');
                $('.hideNotifications').removeClass('hide');
            });
            $('.hideNotifications').on('click', function(e){
                e.preventDefault();
                $('.notificationsList li:gt(3):not("#noNotifications")').addClass('hide');
                $(this).addClass('hide');
                $('.showMoreNotifications').removeClass('hide');
            });
        }

        // On Document ready
        $(document).ready(function(){

            $('.awaitingCompletionsQuestion .item:not(":first")').hide();
            if ($('.awaitingCompletionsQuestion .questionItem').length < 2){
                $('.awaitingCompletionNext').remove();
            }
            $('.awaitingCompletionsQuestion .questionItem:eq(1)').addClass('next');
            $('.awaitingCompletionNext').click(function(e){
                e.preventDefault();
                if ($(this).closest('.questionItem').is(':last-child')){

                    $('.awaitingCompletionsQuestion .questionItem:first-child').delay(50).queue(function () {
                        $(this).addClass('next').removeClass('hide');
                        $(this).dequeue();
                    });

                    $(this).closest('.questionItem').addClass('left');
                    $('.awaitingCompletionsQuestion .questionItem:first-child').addClass('left');

                    $(this).closest('.questionItem').delay(600).queue(function () {
                        $(this).addClass('hide').removeClass('active').removeClass('left');
                        $(this).dequeue();
                    });
                    $('.awaitingCompletionsQuestion .questionItem:first-child').delay(600).queue(function () {
                        $(this).addClass('active').removeClass('next').removeClass('left');
                        $(this).dequeue();
                        if($(this).next().length > 0){
                            $(this).next().addClass('next');
                        } else {
                            $('.awaitingCompletionsQuestion .questionItem:first-child').addClass('next');
                        }
                    });

                } else {
                    $(this).closest('.questionItem').next().delay(50).queue(function () {
                        $(this).addClass('next').removeClass('hide');
                        $(this).dequeue();
                    });
                    
                    $(this).closest('.questionItem').addClass('left').next().addClass('left');

                    $(this).closest('.questionItem').delay(600).queue(function () {
                        $(this).addClass('hide').removeClass('active').removeClass('left');
                        $(this).dequeue();
                    });
                    $(this).closest('.questionItem').next().delay(600).queue(function () {
                        $(this).addClass('active').removeClass('next').removeClass('left');
                        $(this).dequeue();
                        if($(this).next().length > 0){
                            $(this).next().addClass('next');
                        } else {
                            $('.awaitingCompletionsQuestion .questionItem:first-child').addClass('next');
                        }
                    });
                    //$(this).closest('.questionItem').next().addClass('next').removeClass('hide');

                }
            });

            //initialize the introduction tour if necessary
            var tourOutlineElement = $('#tourOutline');
            var forceTour = tourOutlineElement.data('forcetour');
            if(tourOutlineElement && forceTour){
                var action = actions.action_update_hasViewedIntroduction;
                tourOutlineElement.joyride({
                    //mark the introduction as viewed once complete or exited
                    postRideCallback: function(){
                        tourOutlineElement.data('forceTour', '');
                        $.ajax({
                            url: action({})
                        });
                    },
                    postStepCallback: function(){
                        //if the my account section needs to be shown the previous step in the tour will include an
                        //element containing a data attribute of showMyAccountNext. After finding this element determine
                        //if the outer wrapper div is shown and trigger the my account dropdown accordingly.  note that
                        //this callback is only available after a step is shown so data attributes need to be included
                        //on the step BEFORE the one where we actually want the dropdown to be toggled.
                        if($('.joyride-content-wrapper > [data-showAccountDropdownNext="true"]').parent().parent().is(':visible')){
                            $('#accountDropdown').dropdown('toggle');
                        }

                    }
                })
            }
        });
       
    };

    var activityParams = {
        page : 1,
        length : 10
    };

    fn.getActivity = function(){

        var callActivityFeed = function(isFirstCall){
            var url = $('#feedItemList').data('url');
            $.ajax({
                url: url+'/'+activityParams.page+'/'+activityParams.length,
                type: "GET",
                async: true,
                success: function(data, textStatus, jqXHR){
                    //console.log(data.data);
                    if (!isFirstCall){
                        if (data.data.length > 0){
                            $('#feedItemList').append(Application.Tmpl.get.tmpl_FeedItem(data));
                            $('#loadingMoreFeed').addClass('hide');
                            $('#showMoreFeed').removeClass('hide');
                        } else {
                            $('#loadingMoreFeed').addClass('hide');
                        }
                    } else {
                        if (data.data.length > 0){
                            $('#feedItemList').append(Application.Tmpl.get.tmpl_FeedItem(data));
                            $('#loadingMoreFeed').addClass('hide');
                            $('#showMoreFeed').removeClass('hide');
                        } else {
                            $('#loadingMoreFeed').addClass('hide');
                            $('#loadingMoreFeed').after('<div class="noNewsFeedItems">There is no recent activity.</div>');
                        }
                    }
                    
                }
            });
        };

        $('body').trigger('columnResize');
        $('#loadingMoreFeed').removeClass('hide');

        $(document).on('click','.feedItem .feedShare',function(e){
            e.preventDefault();
            $(this).hide();
            $(this).prev('.shareOptions').show();
        });
        $(document).on('click','#showMoreFeed',function(e){
            e.preventDefault();
            $(this).addClass('hide');
            //$(this).parent().hide();
            $('#loadingMoreFeed').removeClass('hide');
            activityParams.page = activityParams.page + 1;
            callActivityFeed();
        });

        Application.Tmpl.loadTemplate('templates.html',function(data, textStatus, jqXHR){
            callActivityFeed(true);
        });

        // Twitter API Script
        //!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="https://platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");

    };

    /*
    *   Feed templates
    */
    fn.template = function(){

    };

    /*
    *   Module Return
    */
    return {
        init: fn.init,
        template: fn.template
    };

})();



/**
 * Application.Profile - User Profile scripts
 *
 * Page: Community/profile
 *
 */
Application.Profile = (function(){

    /*
    *   Module Variables
    */
    var fn = {};

    /*
    *   Initialize Dashboard
    */
    fn.init = function(actions){

        fn.events(actions);

        $(document).ready(function(){
            fn.getQuestionsAndActivity();
            fn.getCategories();
        });
    };

    /*
    *   PublicUser events - triggers, clicks
    */
    fn.events = function(){

        // Trigger for Photo Resizing
        $('.userPhoto img').on('photoResize',function(){
            var imgClass = (this.width/this.height > 1) ? 'wide' : 'tall';
            $(this).addClass(imgClass).parent().addClass(imgClass);

            var margLeft = '-'+this.width/2+'px';
            var margTop = '-'+this.height/2+'px';
            if ($(this).hasClass('wide')){
                //console.log('1',margLeft);
                $(this).css({
                    'left':'50%',
                    'margin-left': margLeft.toString()
                });
            } /*else if ($(this).hasClass('tall')){
                console.log('1');
                $(this).css({
                    'top':'50%',
                    'margin-top': margTop.toString()
                });
            }*/
        });

        // On image load trigger photo resizing
        $('.userPhoto img').load(function(){
            $(this).trigger('photoResize');
        });

        // On window resize trigger photo resizing
        $(window).bind('resizeEnd',function(){
            $('.userPhoto img').trigger('photoResize');
        });

        // Load tab state
        $(window).bind('popstate', function(event) {
            // if the event has our history data on it, load the page fragment with AJAX
            var state = event.originalEvent.state;
            //console.log(state);
            if (state) {
                //$(document).ready(function(){
                    $('[href="'+state+'"]').tab('show');
                //});
            } else {
                //$(document).ready(function(){
                    $('a[data-toggle="tab"]:first').tab('show');
                //});
            }
        });

        $(document).ready(function(){

            // Tab click load
            $('a[data-toggle="tab"]').click(function(e){
                var tab = $(this).attr('href'),
                    loc = window.location.pathname + tab;
                    //console.log(e);
                window.history.pushState(tab, document.title, loc);
            });

            // Load tab state
            if (window.location.hash.length > 0) {
                //$('ul.nav-tabs > li > a[href="' + window.location.hash + '"]').tab('show');
                var activeTab = $('[href=' + window.location.hash + ']');
                if (activeTab) {
                    setTimeout(function(){activeTab.tab('show');},500);
                }
                window.history.replaceState(window.location.hash, document.title, window.location.pathname+window.location.hash);

            }
            $('a[data-toggle="tab"]').on('shown', function(e) {
                //window.location.hash = '/' + $(this).attr('href').replace('#', '');
            });

            // If num badges is greated than 6, hide rest and add show all btn
            if($('.earnedBadges .userBadge').length > 6){
                $('.earnedBadges .userBadge:gt(5)').hide();
                $('.earnedBadges').after('<div class="moreBadges"><a href="#" id="showAllBadges">- Show All -</a></div>');
                $('#showAllBadges').click(function(e){
                    e.preventDefault();
                    $(this).hide();
                    $('.earnedBadges .userBadge:gt(5)').show();
                });
            }

            // If right col is empty, remove border
            if ($('.rightCol').html().trim() === ''){
                $('.rightCol').css('border','none');
            }

        });

    };

    var activityParams = {
        page : 1,
        length : 10
    };

    fn.getQuestionsAndActivity = function(){

        var uid = $('#userDisplayName').data('userid');

        var callAcceptedQuestionFeed = function(isFirstCall){
            var url = $('#tabQuestions').data('url');
            var data = {
                userId : uid,
                page : 1,
                runLength : 15
            };
            if (url !== undefined && url !== ''){
                $.ajax({
                    url: url,
                    data: data,
                    type: "GET",
                    success: function(data, textStatus, jqXHR){
                        //console.log(data);
                        if (data instanceof Array && data.length > 0){
                            $.each(data,function(index,value){
                                //console.log(index, value);
                                $('#tabQuestions .questionList').append(Application.Tmpl.get.tmpl_QuestionItem(value));
                            });
                        } else if (data instanceof Array) {
                            $('#tabQuestions .questionList').append('<div class="noQuestions">This user has no public questions.</div>');
                        } else {
                            $('#tabQuestions .questionList').append('<div class="noQuestions">There was an error loading questions.</div>');
                        }
                    }
                });
            }
        };
        var callReviewerQuestionFeed = function(isFirstCall){
            var url = $('#tabReviewedQuestions').data('url');
            var data = {
                userId : uid,
                page : 1,
                runLength : 15
            };
            if (url !== undefined && url !== ''){
                $.ajax({
                    url: url,
                    data: data,
                    type: "GET",
                    success: function(data, textStatus, jqXHR){
                        //console.log(data);
                        if (data instanceof Array && data.length > 0){
                            $.each(data,function(index,value){
                                //console.log(index, value);
                                $('#tabReviewedQuestions .questionList').append(Application.Tmpl.get.tmpl_QuestionItem(value));
                            });
                        } else if (data instanceof Array) {
                            $('#tabReviewedQuestions .questionList').append('<div class="noQuestions">This user has not reviewed any questions.</div>');
                        } else {
                            $('#tabReviewedQuestions .questionList').append('<div class="noQuestions">There was an error loading questions.</div>');
                        }
                    }
                });
            }
        };
        var callActivityFeed = function(isFirstCall){
            var url = $('#tabActivity').data('url');
            if (url !== undefined && url !== ''){
                $.ajax({
                    url: url+'/'+uid+'/'+activityParams.page+'/'+activityParams.length,
                    type: "GET",
                    async: true,
                    success: function(data, textStatus, jqXHR){
                        //console.log(data.data);
                        if (!isFirstCall){
                            if (data.data.length > 0){
                                $('#tabActivity .feedItemList').append(Application.Tmpl.get.tmpl_FeedItem(data));
                                $('#loadingMoreFeed').addClass('hide');
                                $('#showMoreFeed').removeClass('hide');
                            } else {
                                $('#loadingMoreFeed').addClass('hide');
                            }
                        } else {
                            if (data.data.length > 0){
                                $('#tabActivity .feedItemList').append(Application.Tmpl.get.tmpl_FeedItem(data));
                                $('#loadingMoreFeed').addClass('hide');
                                $('#showMoreFeed').removeClass('hide');
                            } else {
                                $('#loadingMoreFeed').addClass('hide');
                                $('#loadingMoreFeed').after('<div class="noNewsFeedItems">There is no recent activity.</div>');
                            }
                        }
                        
                    }
                });
            }
        };

        $(document).on('click','.feedItem .feedShare',function(e){
            e.preventDefault();
            $(this).hide();
            $(this).prev('.shareOptions').show();
        });
        $(document).on('click','#showMoreFeed',function(e){
            e.preventDefault();
            $(this).addClass('hide');
            //$(this).parent().hide();
            $('#loadingMoreFeed').removeClass('hide');
            activityParams.page = activityParams.page + 1;
            callActivityFeed();
        });

        Application.Tmpl.loadTemplate('templates.html',function(data, textStatus, jqXHR){
            callAcceptedQuestionFeed(true);
            callReviewerQuestionFeed(true);
            callActivityFeed(true);
        });
    };


    fn.getCategories = function(){

        var uid = $('#userDisplayName').data('userid');
        var categoryDisplayLimit = 9;

        var callTopCategories = function(isFirstCall){
            var url = $('.userStats .categories').data('url');
            var max = 99999;
            if ($('.userStats .categories').data('maxvalue') !== undefined){
                max = $('.userStats .categories').data('maxvalue');
            }
            var data = {
                userId : uid,
                max : max
            };
            if (url !== undefined && url !== ''){
                $.ajax({
                    url: url,
                    data: data,
                    type: "GET",
                    success: function(data, textStatus, jqXHR){
                        //console.log(data);
                        var txt = '';

                        if (data.length === 0){
                            txt += '<span class="badge">No categories yet.</span>';
                        } else {
                            $.each(data,function(index,value){
                                //console.log(value);
                                var hideClass = '';
                                if(index > categoryDisplayLimit) { hideClass = ' hide'; }
                                if(value.length > 18) {
                                    txt += '<span class="badge badge-Application'+hideClass+'" rel="tooltip" data-placement="top" title="'+Application.Helper.makeSafe(Application.Helper.makeSafe(value))+'">'+Application.Helper.makeSafe(value.substring(0,18))+'&hellip;</span> ';
                                } else {
                                    txt += '<span class="badge badge-Application'+hideClass+'">'+Application.Helper.makeSafe(value)+'</span> ';
                                }
                            });
                        }
                        $('.userStats .categories .vals').html(txt);
                        $('.userStats .categories .badge[rel="tooltip"]').tooltip();
                        if (data.length > categoryDisplayLimit){
                            $('.userStats .categories .vals').append('<div class="moreCategories"><a href="#" id="showAllCategories">- Show All -</a></div>');
                        }
                    }
                });
            }
        };

        var callTopReviewCategories = function(isFirstCall){
            var url = $('.reviewCategories').data('url');
            var max = 99999;
            if ($('.reviewCategories').data('maxvalue') !== undefined){
                max = $('.reviewCategories').data('maxvalue');
            }
            var data = {
                userId : uid,
                max : max
            };
            if (url !== undefined && url !== ''){
                $.ajax({
                    url: url,
                    data: data,
                    type: "GET",
                    success: function(data, textStatus, jqXHR){
                        //console.log(data);
                        var txt = '';
                        if (data.length === 0){
                            txt += '<span class="badge">No review categories yet.</span>';
                        } else {
                            $.each(data,function(index,value){
                                //console.log(value);
                                var hideClass = '';
                                if(index > categoryDisplayLimit) { hideClass = ' hide'; }
                                txt += '<span class="badge badge-Application'+hideClass+'">'+Application.Helper.makeSafe(value)+'</span> ';
                            });
                        }
                        $('.reviewCategories .vals').html(txt);
                        if (data.length > categoryDisplayLimit){
                            $('.reviewCategories .vals').append('<div class="moreReviewCategories"><a href="#" id="showAllReviewCategories">- Show All -</a></div>');
                        }
                    }
                });
            }
        };

        callTopCategories(true);
        if($('.reviewerStatus').length > 0){
            callTopReviewCategories(true);
        }

        $(document).on('click','#showAllCategories',function(e){
            e.preventDefault();
            $(this).parent().hide();
            $('.userStats .categories .badge.hide').removeClass('hide');
        });
        $(document).on('click','#showAllReviewCategories',function(e){
            e.preventDefault();
            $(this).parent().hide();
            $('.reviewCategories .badge.hide').removeClass('hide');
        });


    };

    /*
    *   Module Return
    */
    return {
        init: fn.init
    };


})();



/**
 * Application.QuestionsList - Question List scripts
 *
 * Page: Questions/list, Questions/review
 *
 */
Application.Questions = (function(){

    /*
     *   Module Variables
     */
    var fn = {};
    var opts = {
        tabNameKey: "",
        filtersKey: "",
        reportNameKey: ""
    };

    /*
     *   Initialize
     */
    fn.init = function(options){

        $.extend(opts, options);

        $(document).ready(function(){

            // If Questions View/List Page
            if(opts.tabNameKey === "questions.tab"){

                // Filter - Type links
                $('#filterListType a').click(function(e){
                    e.preventDefault();
                    $('#filterListType a').removeClass('active');
                    $(this).addClass('active');
                    var name = $(this).data('name');
                    //console.log(name);
                    fn.runQuestionFilter(name);
                });
                // Filter - Status links
                $('#filterListStatus a').click(function(e){
                    e.preventDefault();
                    $('#filterListStatus a').removeClass('active');
                    $(this).addClass('active');
                    var name = $(this).data('name');
                    //console.log(name);
                    fn.runQuestionFilter(name);
                });
                // Load Filter state from cookie
                fn.loadTierFilterState();

            }

            // If Questions Review Page
            if(opts.tabNameKey === "questionsReview.tab"){

                $('.filterListItem a').click(function(e){
                    e.preventDefault();
                    $('.filterListItem a').removeClass('active');
                    $(this).addClass('active');
                    var name = $(this).data('name'),
                        filters = $(this).data('source'),
                        restoring = false;
                    if(name){
                        var source = filters;
                        refreshTable(filters, restoring);
                        fn.setActiveFilter(name);
                        fn.recordFilterState(name, source);
                        return;
                    }else{
                        refreshTable(filters, restoring);
                    }
                });

                fn.loadFilterState();

            }

            $(".alert").alert();

        });

    };

    /*
     *   SelectTierFilter
     *   - This sets the actual filters used by datatables
     *   @param name string - name of filter
     *   @param setTabs boolean - if true, force selects appropriate tabs for given filter
     */
    fn.selectTierFilter = function(name, initial){

        var filters = 'excludeByActiveStatus:false',
            restoring = false;

        if (!name){
            name = "All Questions";
        }

        switch(name){
            case "All Questions":
                filters = 'excludeByActiveStatus:false';
                break;
            case "My Questions":
                filters = 'includeByCreator:$me,excludeByActiveStatus:false';
                break;
            case "Your Company's Private Questions":
                filters = 'includeByCreator:$myCompany,includeByStatus:PRIVATE,excludeByActiveStatus:false';
                break;
            case "Out For Review (You)":
                filters = 'includeByCreator:$me,includeByStatus:OUT_FOR_REVIEW,excludeByActiveStatus:false';
                break;
            case "Out For Review (Company)":
                filters = 'includeByCreator:$myCompany,includeByStatus:OUT_FOR_REVIEW,excludeByActiveStatus:false';
                break;
            case "Recently Used":
                filters = 'RecentlyUsed';
                break;
            default:
                filters = 'excludeByActiveStatus:false';
                break;
        }

        switch(name){
            case "All Questions":
                fn.selectTypeFilter('All Questions');
                fn.selectStatusFilter('Any');
                break;
            case "My Questions":
                fn.selectTypeFilter('My Questions');
                fn.selectStatusFilter('Any');
                break;
            case "Your Company's Private Questions":
                fn.selectTypeFilter('Company\'s Private Questions');
                fn.selectStatusFilter('Any');
                break;
            case "Out For Review (You)":
                fn.selectTypeFilter('My Questions');
                fn.selectStatusFilter('Out For Review');
                break;
            case "Out For Review (Company)":
                fn.selectTypeFilter('Company\'s Private Questions');
                fn.selectStatusFilter('Out For Review');
                break;
            case "Recently Used":
                fn.selectTypeFilter('My Questions');
                fn.selectStatusFilter('Recently Used');
                break;
            default:
                fn.selectTypeFilter('All Questions');
                fn.selectStatusFilter('Any');
                break;
        }


        if (!initial){
            refreshTable(filters, restoring);
        }
        fn.recordFilterState(name, filters);

    };

    /*
     *   SelectTypeFilter
     *   - sets the appropriate Type filter
     */
    fn.selectTypeFilter = function(name){
        $('#filterListType a').removeClass('active');
        $('#filterListType a[data-name="'+name+'"]').addClass('active');
        console.log(name)
        switch(name)
        {
            case "All Questions":
                $('#filterListStatus a[data-name="Any"]').show();
                $('#filterListStatus a[data-name="Out For Review"]').hide();
                $('#filterListStatus a[data-name="Recently Used"]').hide();
                break;
            case "My Questions":
                $('#filterListStatus a[data-name="Any"]').show();
                $('#filterListStatus a[data-name="Out For Review"]').show();
                $('#filterListStatus a[data-name="Recently Used"]').show();
                break;
            case "Company's Private Questions":
                $('#filterListStatus a[data-name="Any"]').show();
                $('#filterListStatus a[data-name="Out For Review"]').show();
                $('#filterListStatus a[data-name="Recently Used"]').hide();
                break;
        }
    };

    /*
     *   SelectStatusFilter
     *   - sets the appropriate Status filter
     */
    fn.selectStatusFilter = function(name){
        $('#filterListStatus a').removeClass('active');
        $('#filterListStatus a[data-name="'+name+'"]').addClass('active');
    };

    /*
     *   LoadTierFilterState
     *   - loads tier filter stored in cookies
     */
    fn.loadTierFilterState = function(){
        var tabName = Application.Helper.getCookie(opts.tabNameKey);
        var datasource = Application.Helper.getCookie(opts.filtersKey);

        if(tabName && datasource){
            fn.selectTierFilter(tabName, true);
            fn.recordFilterState(tabName, datasource);
            return true;
        } else {
            fn.selectTierFilter(null, true);
        }
        return false;
    };

    /*
     *   RunQuestionFilter
     *   - this runs on each filter button click. it loads the appropriate filter
     *   and determines if any button select states need to change
     */
    fn.runQuestionFilter = function(name){

        var typeElm = $('#filterListType a.active').data('name'),
            statusElm = $('#filterListStatus a.active').data('name');

        switch(name){
            case "All Questions":
                fn.selectTierFilter('All Questions');
                break;
            case "My Questions":
                switch(statusElm){
                    case "Any":
                        fn.selectTierFilter('My Questions');
                        break;
                    case "Out For Review":
                        fn.selectTierFilter('Out For Review (You)');
                        break;
                    case "Recently Used":
                        fn.selectTierFilter('Recently Used');
                        break;
                    default:
                        fn.selectTierFilter('My Questions');
                        break;
                }
                break;
            case "Company's Private Questions":
                switch(statusElm){
                    case "Any":
                        fn.selectTierFilter('Your Company\'s Private Questions');
                        break;
                    case "Out For Review":
                        fn.selectTierFilter('Out For Review (Company)');
                        break;
                    default:
                        fn.selectTierFilter('Your Company\'s Private Questions');
                        break;
                }
                break;
            case "Any":
                switch (typeElm){
                    case "All Questions":
                        fn.selectTierFilter('All Questions');
                        break;
                    case "My Questions":
                        fn.selectTierFilter('My Questions');
                        break;
                    case "Company's Private Questions":
                        fn.selectTierFilter('Your Company\'s Private Questions');
                        break;
                    default:
                        fn.selectTierFilter('All Questions');
                        break;
                }
                break;
            case "Out For Review":
                switch (typeElm){
                    case "My Questions":
                        fn.selectTierFilter('Out For Review (You)');
                        break;
                    case "Company's Private Questions":
                        fn.selectTierFilter('Out For Review (Company)');
                        break;
                    default:
                        fn.selectTierFilter('Out For Review (You)');
                        break;
                }
                break;
            case "Recently Used":
                fn.selectTierFilter('Recently Used');
                break;
        }

    };

    /*
     *   SetActiveFilter
     *   - on non-tier filter, sets appropriate active state
     */
    fn.setActiveFilter = function(name){
        if (name){
            var tab = $('.filterListItem a[data-name="'+name+'"]');
            $('.filterListItem a').removeClass('active');
            tab.addClass('active');
        } else {
            $('.filterListItem a:first').addClass('active');
        }
    };

    /*
     *   RecordFilterState
     *   - stores filter state in cookies
     */
    fn.recordFilterState = function(tabName, datasource){
        Application.Helper.setCookie(opts.tabNameKey, tabName);
        Application.Helper.setCookie(opts.filtersKey, datasource);
    };

    /*
     *   ClearRecordedFilterState
     *   - clears cookie that stories filter state
     */
    fn.clearRecordedFilterState = function(){
        Application.Helper.deleteCookie(opts.tabNameKey);
        Application.Helper.deleteCookie(opts.reportNameKey);
        Application.Helper.deleteCookie(opts.filtersKey);
    };

    /*
     *   LoadFilterState
     *   - loads non-tier filter stored in cookies
     */
    fn.loadFilterState = function(){
        var tabName = Application.Helper.getCookie(opts.tabNameKey);
        var datasource = Application.Helper.getCookie(opts.filtersKey);

        if(tabName && datasource){
            fn.setActiveFilter(tabName);
            fn.recordFilterState(tabName, datasource);
            return true;
        } else {
            fn.setActiveFilter();
        }
        return false;
    };

    /*
     *   Module Return
     */
    return {
        init: fn.init,
        opts: opts
    };

})();


/**
 * Application.TabbedFilterManager - Controller class that binds a tabbedFilterList to anything to
 *                               which filters can be applied.
 *
 * Pages: interviews/list, interviews/candidates
 *
 */
Application.TabbedFilterManager = (function(){

    /*
     *   Module Variables
     */
    var fn = {};
    var opts = {
        tabNameKey: "",
        applyFilters: function(){}
    };

    /*
     *   Initialize
     */
    fn.init = function(options){

        $.extend(opts, options);

        $(document).ready(function(){
        	$('.filterListItem a').click(function(e){
                    e.preventDefault();
                    var name = $(this).data('name');
                    //console.log(name);
                    fn.selectTab(name);
        	});
        	
           	fn.selectTab();
        });

    };
    
    /*
     *   selectTab
     *   - selects the given tab or the default tab if none is given
     */
    fn.selectTab = function(tabName){
    	var selectedTab = tabName;
		if(!selectedTab){
			selectedTab = Application.Helper.getCookie(opts.tabNameKey);
			if(!selectedTab){
				selectedTab = $('.filterListItem a:first').attr("data-name");
			}
        }
        
        var activeFilters = fn.getFiltersForTab(selectedTab);
        
        fn.setFilterState(activeFilters);
        
        fn.setTabState(selectedTab);
        
        fn.recordSelectionState(selectedTab);
     }
    
    /*
     *   GetFiltersForTab
     *   - returns the set of filters that are assigned to the given tab
     */
    fn.getFiltersForTab = function(tabName){
    
        return $('.filterListItem a[data-name="'+tabName+'"]').attr("data-source");
        
     }

    /*
     *   SetTabState
     *   - sets tab state
     */
    fn.setTabState = function(tabName){
        var tab = $('.filterListItem a[data-name="'+tabName+'"]');
        $('.filterListItem a').removeClass('active');
        tab.addClass('active');
    };
    
    /*
     *   SetFilterState
     *   - sets filter state
     */
    fn.setFilterState = function(filters){
    
    	applyFilters(filters);
        
    };
    
    /*
     *   RecordSelectionState
     *   - records the selected tab state
     */
    fn.recordSelectionState = function(selectedTabName){
    
        Application.Helper.setCookie(opts.tabNameKey, selectedTabName);
        
    };

    /*
     *   Module Return
     */
    return {
        init: fn.init,
        opts: opts
    };

})();

/**
 * Application.DataTable - Defines a default datatable and wraps it.
 *
 * Pages: interviews/list, interviews/candidates
 *
 */
Application.DataTable = (function () {

	var instance = {};
	
	instance.tableId = "";
	
	instance.filters = "";
	
	instance.ajaxSource = "";
	
	instance.sourceReport = "";
	
	
	function buildDataObject(array) {
        	var result = {};
    
        	for (var attr in array) {
            	result[array[attr].name] = array[attr].value;
        	}
        
        	return result;
    }
    
	var opts = {
        	sPaginationType: "bootstrap",
        	bProcessing: true,
        	bServerSide: true,
        	sDom: "<'row'<'span6'l><'span6'f>><'tableWorkingSpace'rt><'row'" +
                "<'span6'i><'span6'p>>",
        	fnServerData: function (sSource, aoData, fnCallback) {
            	ooData = buildDataObject(aoData);

                // get a list of column server names
                var colNames = [];
                $('#' + instance.tableId + ' th[sWidth]').each(function() {
                    colNames.push($(this).attr('sName'));
                });

                // build search string
                for(var i=0; i<ooData.iColumns; i++) {
                    var searchTerm = ooData["sSearch_"+i];
                    if(searchTerm !== "") {
                        if(ooData.sSearch != "") {
                            ooData.sSearch += "&&";
                        }
                        ooData.sSearch += searchTerm + ":" + colNames[i];
                    }
                }

            	var transformer = function (response) {

                	if (response.error) {
                    	console.log(response.error);
                	}
            
                	var datatableData = {
                    	aaData: response.data,
                    	iTotalDisplayRecords: response.matchingCount,
                    	iTotalRecords: response.matchingCount,
                    	sEcho: ooData.sEcho
                	};
                
                	return fnCallback(datatableData);
            	};
            
            	var requestData = {
                	reportName: instance.sourceReport,
                	filters: instance.filters,
                	sortColumn: ooData.iSortCol_0,
                	ascending: (ooData.sSortDir_0 == "asc"),
                	startIndex: ooData.iDisplayStart,
                	runLength: ooData.iDisplayLength,
                	searchString: ooData.sSearch
            	};
            	//console.log('Loading table...');
            	$.ajax({
            		url: sSource,
            		dataType: 'json',
            		data: requestData,
            		success: transformer
        		});
        	}
   	};
	
	instance.init = function(tableId, ajaxSource, sourceReport, options){
		instance.ajaxSource = ajaxSource;
		instance.sourceReport = sourceReport;
	    instance.tableId = tableId;
	    
		
		$.extend(opts,
		    {
		        sAjaxSource: ajaxSource
        	}
        );
		
		$.extend(opts, options);
		
		var aoColumns = {};
		var arr = [];
        $('#' + tableId + ' th[sWidth]').each(function(){
        	var column = {};
        	column['sWidth'] = $(this).attr('sWidth');
        	column['sClass'] = $(this).attr('sClass');
        	arr.push(column);
        });
        aoColumns['aoColumns']=arr;
		
		$.extend(opts, aoColumns);
    	
    
    	$.fn.dataTableExt.oApi.fnReloadAjax = 
            function (oSettings, sNewSource, fnCallback, bStandingRedraw) {
                if ( typeof sNewSource != 'undefined' && sNewSource != null ){
                    oSettings.sAjaxSource = sNewSource;
                }
                
                this.oApi._fnProcessingDisplay( oSettings, true );
                var that = this;
                var iStart = oSettings._iDisplayStart;
                var aData = [];

                this.oApi._fnServerParams( oSettings, aData );

                /* Clear the old information from the table */
                that.oApi._fnClearTable( oSettings );

                for ( var i=0 ; i<aData.length ; i++ )
                {
                    that.oApi._fnAddData( oSettings, aData[i] );
                }

                oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();
                that.fnDraw();

                if ( typeof bStandingRedraw != 'undefined' && bStandingRedraw === true )
                {
                    oSettings._iDisplayStart = iStart;
                    that.fnDraw( false );
                }

                /* Callback user function - for event handlers etc */
                if ( typeof fnCallback == 'function' && fnCallback != null )
                {
                    fnCallback( oSettings );
                }
            };

    	
    
		$.extend($.fn.dataTableExt.oStdClasses, {
    		"sSortAsc":"header headerSortDown",
    		"sSortDesc":"header headerSortUp",
    		"sSortable":"header sorting",
    		"sWrapper": "dataTables_wrapper form-inline"
		});
		
		instance.opts = opts;

        $(window).resize(function(){
            instance.table.css('width','')._fnAdjustColumnSizing();
        });

	}

	instance.load = function(filters, callback){
		instance.filters = filters;
		if(!instance.table){
			instance.table = $('#' + instance.tableId).dataTable(instance.opts);
		} else {
			$('#' + instance.tableId).dataTable().fnReloadAjax(null, callback);
		}
	}
    

    return instance;
})();

/**
 *  Load Application.Global.init()
 */
Application.Global.init();
