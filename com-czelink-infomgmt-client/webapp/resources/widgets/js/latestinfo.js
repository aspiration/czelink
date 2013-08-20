define(function() {
	return function($scope, jquery, require, orchestration) {

		$.getJSON('infomgmt/simple', function(data) {
			$scope.contents = data.contents;

			if (!$scope.$$phase) {
				$scope.$apply();
			}
		});

		$scope.showDetails = function(infoParam) {
			var options = {
				location : 'information.html',
				flashObjs : {
					infoParam : infoParam
				}
			};
			orchestration.invoke("navigation", "navigateTo", options);
		};
	};
});