
//This javascript is used by/with the quickViewer.html to implement an integrated image/pdf viewer.
//Rendering PDFs is non-trivial.  When making any changes to the PDF rendering code below, be sure
//to test throughly with the two PDFs located in the test/pdf directory of the application.
//These two PDFs provide thorough coverage of the PDF-rendering gotchas.

//At the time of writing this comment, the code has been verified to work in the following browsers:
//Google Chrome on Linux, Vista, OS X, Windows 7
//Safari on Vista (v5), OS X (v6), iPad
//Firefox on Vista (v8), Windows 7 (v14), OS X (v14)
//Opera on Vista (v10 & v12), OS X (v12) (works if plugin installed, the js parser will miss some fonts)
//Internet Explorer 9 32-bit on Vista and Windows 7
//Internet Explorer 9 64-bit on Vista and Windows 7

//unverified:  Android tablets (any browser)
var QuickViewer = (function() {

    var self = this;
    
    var $dialog = null,
        $modalBody = null,
        width = "100%",
        height = 370,
        pdfBodyClass = 'quick-view-pdf-body',
        pdfJsBodyClass = 'quick-view-pdf-js-body',
        htmlBodyClass = 'quick-view-html-body',
        imgBodyClass = 'quick-view-img-body',
        pdfLoadingClass = 'quick-view-pdf-loader',
        imageLoadingClass = 'quick-view-image-loader',
        mobileClient = navigator.userAgent.toLowerCase().indexOf('mobile') > -1,
        androidClient = navigator.userAgent.toLowerCase().indexOf("android") > -1;
    
    var browserHasNormalPdfSupport = function () {
        
        var regx = /Adobe Reader|Adobe PDF|Acrobat/gi;
        
        //return true if adobe pdf plugin found
        for(var i = 0; i < navigator.plugins.length; i++) {
            if (regx.test(navigator.plugins[i].name)) {
                return true;
            }
        }
        
        //return true if generic pdf plugin found
        var plugin = navigator.mimeTypes["application/pdf"];
        return (plugin && plugin.enabledPlugin);
    
    }();
    
    //this seems to just break in some versions of ie, but not other browsers, or course...
    var browserHasActiveXPdfSupport = function (){

        var activeX = null;
        
        if (window.ActiveXObject) {
            
            try { //try/catch is a hack for ie9 64-bit on vista (not sure about other windows versions)
                
                activeX = new ActiveXObject("AcroPDF.PDF");
                
                //If "AcroPDF.PDF" didn't work, try "PDF.PdfCtrl"
                if(!activeX){
                    activeX = new ActiveXObject("PDF.PdfCtrl");
                }
                
                //If either "AcroPDF.PDF" or "PDF.PdfCtrl" are found, return true
                if (activeX !== null) {
                    return true;
                }
                
            } catch (e) {
                return false;
            }
    
        }
        
        //If you got to this point, there's no ActiveXObject for PDFs
        return false;
        
    }();
    
    var usingPdfJs = function () {
        return !(browserHasNormalPdfSupport || browserHasActiveXPdfSupport || (mobileClient && !androidClient));
    }();
    
    
    
    var isImage = function(type) {
        return type.lastIndexOf("image", 0) === 0;
    };
    
    var isPdf = function(type) {
        return type == "application/pdf";
    };
    
    var isHtml = function(type) {
        return type == "text/html";
    };
    
    var renderImage = function(url) {
        
        //just setting the pre-rendering size to look better
        $modalBody.height(250);
        $modalBody.width('100%');
        
        $modalBody.addClass(imageLoadingClass);
        
        var image = new Image();
        $(image).hide();
        
        $(image).load(function () {
            //$modalBody.height(image.height);
            //$modalBody.width(image.width);
            $modalBody.height(height);
            $modalBody.width('100%');
            $modalBody.html($(this));
            $modalBody.removeClass(imageLoadingClass);
            $modalBody.addClass(imgBodyClass);
            $dialog.on('hidden', function removeImgClass() {
                $modalBody.removeClass(imgBodyClass);
            });
            $(this).show();
        }).error(function() {
            $modalBody.html("Image could not be loaded");
        });
        
        image.src = url;
        
        $dialog.modal({ remote: false, show: true });
        
    };
    
    var renderHtml = function(url) {

        $.ajax({
                url: url,
                success: function(data){
                $modalBody.html(data);
                $modalBody.width(width);
                $modalBody.height(height);
                $modalBody.addClass(htmlBodyClass);
                $dialog.modal({ remote: false, show: true });
                $dialog.on('hidden', function removeHtmlClass() {
                    $modalBody.removeClass(htmlBodyClass);
                });
            },
                error: function(e){
                $modalBody.html("Unable to load document.");
                $modalBody.width('auto');
                $dialog.modal({ remote: false, show: true });
            }
        });
        
        
    };
    
    var renderPdf = function(url) {
        $modalBody.addClass(pdfBodyClass);
        $modalBody.addClass(pdfLoadingClass);

        $dialog.on('hidden', function removePdfClass() {
            $modalBody.removeClass(pdfBodyClass);
        });
        if (!usingPdfJs) {
            renderPdfAsObject(url);
        } else {
            renderPdfAsImage(url);
        }
    };
    
    var renderPdfAsObject = function(url) {
        var object = '<object style="margin-left: auto; margin-right: auto;" data="' + url + '" type="application/pdf" width="' + width + '" height="' + height + '">Your browser does not support viewing of PDF files.</object>';
        $(object).hide();
        $modalBody.html(object);
        $modalBody.width('auto');
        $dialog.modal({ remote: false, show: true });
        $(object).show();
        $modalBody.removeClass(pdfLoadingClass);
    };
    
    var renderPdfAsImage = function(url) {
        $modalBody.addClass(pdfJsBodyClass);
        var scale = 1.325;
        PdfRenderer.renderDocument(url, $modalBody[0], scale, function afterParsing(scrollTop) {
            $modalBody.removeClass(pdfLoadingClass);
            $dialog.bind('dialogopen', function restoreScroll() {
                $modalBody.scrollTop(scrollTop);
                $dialog.unbind('dialogopen', restoreScroll);
            });
        });
        $modalBody.width(width);
        $modalBody.height(height);
        $dialog.modal({ remote: false, show: true });
        $dialog.on('hidden', function removePdfJsClass() {
            PdfRenderer.saveScroll($modalBody.scrollTop());
            $modalBody.removeClass(pdfJsBodyClass);
            $dialog.unbind('viewerclose', removePdfJsClass);
        });
    };
    
    var kickOffViewer = function ($trigger) {

        $modalBody.empty();
        
        var supportedType = true;
        var type = $trigger.attr('type');
        var url = $trigger.attr('href').replace('&inline=false','').replace('&inline=true','');
        $trigger.attr('href', url + "&inline=false");
        $('#quick-view-download-anchor').attr('href', url + "&inline=false");
        url = url + "&inline=true";
        
        if (isImage(type)) {
            renderImage(url);
        } else if (isPdf(type)){
            renderPdf(url);
        } else if (isHtml(type)){
            renderHtml(url);
        } else {
            supportedType = false;
        }
        
        return supportedType;

    };
    
    var close = function() {
        $dialog.modal('hide');
    };
    
    return {
        
        initialize : function() {
            
            $modalBody = $('#quick-view-body');
            
            $dialog = $('#quickViewFeedback');

            //allows click event to be bound to dynamically loaded content/html elements
            $('body').on('click', 'a.quick-view-trigger', function(event) {

                var success = false;
                if($(window).width() > 767){
                    event.preventDefault();
                    success = kickOffViewer($(this));
                }
            });

            $('a.quick-view-trigger').each(function(){
                var url = $(this).attr('href').replace('&inline=false','').replace('&inline=true','');
                $(this).attr('href', url + "&inline=false");
            });

            var scriptDir = "/public/javascripts/";

            var pdfRenderLoaded = false;

            if (usingPdfJs) {
                //only use if it initializes successfully
                if ($(window).width() > 767){
                    PdfRenderer.initialize(scriptDir, function(success) {
                        usingPdfJs = success;
                        pdfRenderLoaded = true;
                    });
                }
            }
            $(window).resize(function() {
                if (usingPdfJs && pdfRenderLoaded === false && $(window).width() > 767){
                    PdfRenderer.initialize(scriptDir, function(success) {
                        usingPdfJs = success;
                        pdfRenderLoaded = true;
                    });
                }
            });

            $('body').on('click', '.quick-view-img-body img', function(e) {
                if (($(this).css('max-width') == '100%') || ($(this).is('[style*="max-width: 100%"]') === true)){
                    $(this).css('max-width','inherit');
                } else {
                    $(this).css('max-width','100%');
                }
            });

            
        }
        
    };
    
})();
    
$(document).ready(function() {
    QuickViewer.initialize();
});
