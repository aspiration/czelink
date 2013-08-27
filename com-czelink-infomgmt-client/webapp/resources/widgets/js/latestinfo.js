define(function() {
	return function($scope, jquery, require, orchestration) {

		$.getJSON('infomgmt/latestInfo', function(data) {
			$scope.contents = data.contents;

			if (!$scope.$$phase) {
				$scope.$apply();
			}
		});

		$scope.showDetails = function(articleId) {
			var options = {
				location : 'information.html',
				flashObjs : {
					articleId : articleId,
					info_details : true
				}
			};
			orchestration.invoke("navigation", "navigateTo", options);
		};
	};
});