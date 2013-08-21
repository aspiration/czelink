// could only be called by main.js
define([ 'angular', 'require', 'uuid' ], function(angular, require, uuid) {

	var orchestrationNode = document.getElementById("orchestrationNode");

	var orchestrationModule = angular.module("orchestrationNode", []);

	var orchestrationManager = {};

	orchestrationModule.controller('orchestrationCtrl', function($scope) {

		$scope.registery = {};
		$scope.lookupMap = {};

		orchestrationManager.register = function(appName, appScope) {
			if ($scope.registery[appName] === undefined) {
				var index = uuid();
				$scope.registery[appName] = appScope;
				$scope.lookupMap[appName] = index;
				$scope.registery[appName][index] = {};
			}
		};

		orchestrationManager.invoke = function(targetAppName, targetMethodName,
				params, callbackFunc) {
			var targetAppScope = $scope.registery[targetAppName];
			var targetIndex = $scope.lookupMap[targetAppName];
			var result = targetAppScope[targetIndex][targetMethodName](params);

			if (callbackFunc !== null && callbackFunc !== undefined) {
				callbackFunc(result);
			}
			return result;
		};

		orchestrationManager.expose = function(targetAppName, targetMethodName,
				targetMethodFunc) {
			var targetAppScope = $scope.registery[targetAppName];
			var targetIndex = $scope.lookupMap[targetAppName];
			targetAppScope[targetIndex][targetMethodName] = targetMethodFunc;
		};

	});

	angular.bootstrap(orchestrationNode, [ 'orchestrationNode' ]);

	return function(appName, appScope) {
		var orchestration = {};
		// register to orchestration.
		orchestrationManager.register(appName, appScope);
		orchestration.invoke = function(targetAppName, targetMethodName,
				params, callbackFunc) {
			orchestrationManager.invoke(targetAppName, targetMethodName,
					params, callbackFunc);
		};
		orchestration.expose = function(targetMethodName, targetMethodFunc) {
			orchestrationManager.expose(appName, targetMethodName,
					targetMethodFunc);
		};
		return orchestration;
	};
});