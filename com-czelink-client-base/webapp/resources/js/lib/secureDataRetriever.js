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
			if ((instance.success !== undefined)
					&& instance.success instanceof Function) {
				var result = instance.success(data);
				if (result !== false) {
					if (!data.status) {
						hiddenWidgetContainer();
					}
				}
			} else if (instance.success === false) {
				// no operation - suspend onSuccess.
			} else {
				if (!data.status) {
					hiddenWidgetContainer();
				}
			}
		};

		var doFailure = function(jqXHR, textStatus, errorThrown) {
			if ((instance.failure !== undefined)
					&& instance.failure instanceof Function) {
				var result = instance.failure(jqXHR, textStatus, errorThrown);
				if (result !== false) {
					hiddenWidgetContainer();
				}
			} else if (instance.failure === false) {
				// do nothing. suspend on failure
			} else {
				hiddenWidgetContainer();
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