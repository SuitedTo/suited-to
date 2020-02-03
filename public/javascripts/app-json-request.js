

function Request(url){

    this.url=url;
    this.serverMode="${play.Play.mode.name()}";

    this.getJSON = function (requestData, successCallback, _async){
        if (typeof _async === 'undefined' ||  _async === null) {
            _async = true;
        }
        $.ajax({
            url: this.url,
            dataType: 'json',
            data: requestData,
            success: successCallback,
            async: _async
        });
    }

    this.postJSON = function(requestData, successCallback){
        $.ajax({
	    type: 'POST',
            url: this.url,
            dataType: 'json',
            data: requestData,
            success: successCallback
        });
    };
}
