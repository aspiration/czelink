define(function() {
	return function($scope, secureDataRetriever, require, orchestration) {
		
		$scope.statusText = "请登录";
		
		$scope.openLoginModal = function() {
			orchestration.invoke("statuscontent", "openLoginModal");
		};
	};
});