define(function() {
	return function($scope, secureDataRetriever, require, orchestration) {

		orchestration.invoke("navigation", "getFlashObject", "articleId",
				function(articleId) {

					secureDataRetriever.setData({
						articleId : articleId
					});
					secureDataRetriever.onSuccess(function(data) {
						$scope.content = data;

						if (!$scope.$$phase) {
							$scope.$apply();
						}
					});
					secureDataRetriever.post('infomgmt/searchById');

				});
	};
});