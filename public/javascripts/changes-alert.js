
/*
 * This plugin is Copyright 2012 SPARC, LLC.  All rights reserved.
 *
 * This script selects all the inputs on the page and makes it so that once 
 * they are changed, the user will be prompted if she tries to leave the page.
 * Any element marked with the 'no-change-check' class will ignore these checks
 * (so that a submit or cancel button can allow navigation away, e.g.) and also
 * mark the current configuration of inputs as as "unchanged" (so that a "save
 * changes" button can indicate that the form is no longer dirty.)  Any input
 * marked with 'no-change-check' will not be checked for changes.
 * 
 * Changealert can be put into "changed" or "clean" mode programmatically by
 * calling $.fn.changeAlert('setChanged', hasChanges), where hasChanges is a
 * boolean.
 * 
 * If new links or buttons are added that have the no-change-check property,
 * use the recheckSubmitters() function to reinstrument the page.
 */

;(function ($) {
    
    var _submitters;
    var _inputs;
    var oldValues;
    var cleanMessage;
    
    function inputs() {
        if (!_inputs) {
            _inputs = $(':input').not('.no-change-check');
        }
        
        return _inputs;
    }
    
    function submitters() {
        if (!_submitters) {
            _submitters = $('.no-change-check').not('input:file');
        }
        
        return _submitters;
    }
    
    function unloadMessage(e) {
        e = e || window.event;
        
        var message = 'You have unsaved changes on this page.  If you ' +
            'navigate away, this data will be lost.';
        
        if (e) {
            e.returnValue = message;
        }
        
        return message;
    }
    
    function setConfirmUnload(on) {
        window.onbeforeunload = (on) ? unloadMessage : cleanMessage;
    }
    
    function checkForChanges() {
        newValues = values(inputs());
        
        var changed = !arrayEq(oldValues, newValues);
        
        if (changed) {
            markDirty();
        }
        
        return changed;
    }
    
    function isDirty() {
        return (window.onbeforeunload === unloadMessage);
    }
    
    function pollForChanges() {
        
        if (!isDirty()) {
            var changed = checkForChanges();

            if (!changed) {
                setTimeout(pollForChanges, 250);
            }
        }
    }
    
    function arrayEq(a1, a2) {
        var length = a1.length;
        var result = (length === a2.length);
        
        var index = 0;
        while (result && index < length) {
            result = (a1[index] === a2[index]);
            index++;
        }
        
        return result;
    }
    
    function values(elements) {
        var result = [];
        
        elements.each(function (i) {
            var value;
            
            jqThis = $(this);
            
            if (jqThis.is(':checkbox') || jqThis.is(':radio')) {
                value = (jqThis.is(':checked'));
            }
            else {
                value = this.value;
            }
            
            result.push(value)
        });
        
        return result;
    }
    
    function markClean() {
        oldValues = values(inputs());
        setConfirmUnload(false);
        pollForChanges();
    }
    
    function markDirty() {
        setConfirmUnload(true);
    }
    
    function setChanged(dirty) {
        if (dirty) {
            markDirty();
        }
        else {
            markClean();
        }
    }
    
    var publicMethods = {
        reenable : function( options ) { 
            pollForChanges();
        },
        markChanged : function( ) {
            setChanged(true);
        },
        setChanged : function(isDirty) {
            setChanged(isDirty);
        },
        recheckSubmitters : function() {
            _submitters = null;
            
            $(submitters()).click(function () {
                markClean();
            });
        }
    };

    $.fn.changeAlert = function( method ) {

        // Method calling logic
        if (publicMethods[method]) {
            return publicMethods[method].apply(
                this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || !method ) {
            return methods.init.apply( this, arguments );
        } else {
            $.error( 'Method ' +  method + 
                ' does not exist on jQuery.changeAlert' );
            return null;
        }    
    };
    
    $(document).ready(function() {
        
        oldValues = values(inputs());
        
        inputs().change(function() { checkForChanges(); });
        
        $(submitters()).click(function () {
            markClean();
        });
        
        //We stash the default onbeforeunload for later comparison/restoration
        cleanMessage = window.onbeforeunload;
        
        pollForChanges();
    });
})(jQuery);