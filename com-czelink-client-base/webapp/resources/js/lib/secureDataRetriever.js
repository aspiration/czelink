define([ 'jquery', 'angular' ], function(jquery, angular) {

	return function(widgetElement) {

		var instance = {};

		instance.setHeader = function(pHeaders) {
			instance.headers = pHeaders;
		};

		instance.setData = function(pData) {
			instance.data = pData;
		};

		instance.onSuccess = function(onSuccessCallback) {
			instance.success = onSuccessCallback;
		};

		instance.onFailure = function(onFailureCallback) {
			instance.failure = onFailureCallback;
		};

		var hiddenWidgetContainer = function() {
			var target = $(widgetElement);
			while (target[0] !== undefined) {
				if (target.is("[class^='span']")) {
					target.attr("style", "display:none");
					break;
				} else {
					target = target.parent();
				}
			}
		};

		var doSuccess = function(data, textStatus, jqXHR) {
			// TODO: to add

			if (!data.status) {
				hiddenWidgetContainer();
			}

			if ((instance.onSuccess !== undefined)
					&& instance.onSuccess instanceof Function) {
				instance.onSuccess(data);
			}
		};

		var doFailure = function(jqXHR, textStatus, errorThrown) {
			// TODO: to add

			console.log(jqXHR);
			window.jqXHR = jqXHR;
			console.log(textStatus);
			window.textStatus = textStatus;
			console.log(errorThrown);
			window.errorThrown = errorThrown;

			hiddenWidgetContainer();

			if ((instance.onFailure !== undefined)
					&& instance.onFailure instanceof Function) {
				// TODO: to define
			}
		};

		instance.post = function(url) {
			var configuration = {
				type : "post",
				dataType : "json",
				url : url,
				success : doSuccess,
				error : doFailure,
			};

			if (instance.data !== undefined) {
				configuration.data = instance.data;
			}

			if (instance.headers !== undefined) {
				configuration.headers = instance.headers;
			}

			jquery.ajax(configuration);
		};

		instance.get = function(url) {
			var configuration = {
				type : "get",
				dataType : "json",
				url : url,
				success : doSuccess,
				error : doFailure,
			};

			if (instance.data !== undefined) {
				configuration.data = instance.data;
			}

			if (instance.headers !== undefined) {
				configuration.beforeSend = function(request) {
					angular.forEach(instance.headers, function(value, key) {
						request.setRequestHeader(key, value);
					});
				};
			}

			jquery.ajax(configuration);
		};

		return instance;
	};
});