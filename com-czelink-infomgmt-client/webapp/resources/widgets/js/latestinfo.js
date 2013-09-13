define(function() {
	return function($scope, secureDataRetriever, require, orchestration) {

		secureDataRetriever.onSuccess(function(data) {
			$scope.contents = data.contents;

			if (!$scope.$$phase) {
				$scope.$apply();
			}
		});

		secureDataRetriever.get('infomgmt/latestInfo');

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