define(function() {
	return function($scope, jquery, require, orchestration) {
		// dummy implementation.
		$scope.statusText = "请登录";

		$scope.openLoginModal = function() {
			orchestration.invoke("statuscontent", "openLoginModal");
		};
	};
});